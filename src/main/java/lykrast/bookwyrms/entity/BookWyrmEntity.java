package lykrast.bookwyrms.entity;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import lykrast.bookwyrms.BookWyrms;
import lykrast.bookwyrms.config.ConfigValues;
import lykrast.bookwyrms.item.WyrmutagenHelper;
import lykrast.bookwyrms.registry.BWSounds;
import lykrast.bookwyrms.registry.BWEntities;
import lykrast.bookwyrms.registry.BWItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BookWyrmEntity extends Animal {
	// Types 0-6 : grey, red, orange, green, blue, teal, purple
	public static final int GREY = 0, RED = 1, ORANGE = 2, GREEN = 3, BLUE = 4, TEAL = 5, PURPLE = 6;
	// Well how clever can I be to cram EVERYTHING into 1 byte?
	// 0xDG000TTT with Digesting, "Golden", Type (6 types so 3 bits enough)
	private static final EntityDataAccessor<Byte> DATA = SynchedEntityData.defineId(BookWyrmEntity.class, EntityDataSerializers.BYTE);
	private static final int DIGESTING_MASK = 0b10000000;
	private static final int TREASURE_MASK = 0b01000000;
	private static final int TYPE_MASK = 0b00111111;
	// Stats
	private int enchLevel, digestSpeed;
	private double indigestChance;
	// Digestion
	private int digested, toDigest, digestTimer;
	//Wyrmutagen
	private int mutagenColor, mutagenStat;

	public BookWyrmEntity(EntityType<? extends BookWyrmEntity> type, Level world) {
		super(type, world);
		mutagenColor = -1;
		mutagenStat = -1;
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new PanicGoal(this, 1.3));
		goalSelector.addGoal(2, new BreedGoal(this, 1));
		goalSelector.addGoal(3, new TemptGoal(this, 1.2, Ingredient.of(Items.BOOK, Items.ENCHANTED_BOOK), false));
		goalSelector.addGoal(4, new FollowParentGoal(this, 1.2));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6f));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 12).add(Attributes.MOVEMENT_SPEED, 0.25);
	}

	@SuppressWarnings("resource")
	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!isBaby() && (stack.is(Items.ENCHANTED_BOOK) || stack.is(BWItems.chadBolus.get()))) {
			if (level().isClientSide) return InteractionResult.SUCCESS;
			// Eat enchanted book
			toDigest += getBookValue(stack);
			if (digestTimer <= 0) digestTimer = digestSpeed;
			setDigesting(true);

			if (!player.getAbilities().instabuild) stack.shrink(1);
			playSound(SoundEvents.GENERIC_EAT, 1, 1);
			return InteractionResult.CONSUME;
		}
		return super.mobInteract(player, hand);
	}

	//damn it can't generic array
	private static final RegistryObject<?>[] SCALES = { BWItems.scaleGrey, BWItems.scaleRed,
			BWItems.scaleOrange, BWItems.scaleGreen, BWItems.scaleBlue,
			BWItems.scaleTeal, BWItems.scalePurple };

	@SuppressWarnings("resource")
	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide && isDigesting()) {
			//Digesting particles
			for (int i = 0; i < 2; ++i) {
				level().addParticle(ParticleTypes.ENCHANT, getRandomX(0.5), getRandomY(), getRandomZ(0.5), (random.nextDouble() - 0.5) * 2.0, -random.nextDouble(), (random.nextDouble() - 0.5) * 2.0);
			}
		}
		if (!level().isClientSide && (toDigest > 0 || hasMutagen())) {
			digestTimer--;
			if (digestTimer <= 0) {
				if (hasMutagen()) {
					//digest the mutagen first
					if (mutagenStat >= 0) {
						mutateStats(mutagenStat);
						clampGenes();
					}
					if (mutagenColor >= 0) {
						//shed previous scales
						spawnAtLocation(new ItemStack((Item)(SCALES[getWyrmType()].get()), random.nextIntBetweenInclusive(2, 3)));
						setWyrmType(mutagenColor);
					}
					//TODO proper sound and a particle effect?
					playSound(BWSounds.wyrmBook.get(), 1, 1);
					clearMutagen();
				}
				else {
					//no mutagen, digest a level
					digested++;
					toDigest--;
					if (digested >= enchLevel) {
						digested -= enchLevel;
						makeBook();
					}
				}
				if (toDigest <= 0) setDigesting(false);
				else digestTimer = digestSpeed;
			}
		}
	}

	// It's TagKey<Item> but java can't generic arrays :(
	private static final TagKey<?>[] POOLS = { null, ItemTags.create(BookWyrms.rl("pool_red")), ItemTags.create(BookWyrms.rl("pool_orange")),
			ItemTags.create(BookWyrms.rl("pool_green")), ItemTags.create(BookWyrms.rl("pool_blue")), ItemTags.create(BookWyrms.rl("pool_teal")),
			ItemTags.create(BookWyrms.rl("pool_purple")) };

	private void makeBook() {
		ItemStack stack;
		// Indigestion
		if (random.nextDouble() < indigestChance) {
			stack = new ItemStack(BWItems.chadBolus.get(), random.nextIntBetweenInclusive(1, enchLevel));
			playSound(BWSounds.wyrmIndigestion.get(), 1, 1);
		}
		else {
			//Based on EnchantmentHelper.enchantItem
			@SuppressWarnings("unchecked")
			List<EnchantmentInstance> ench = selectEnchantments(random, ConfigValues.DISABLE_COLOR ? null : (TagKey<Item>) POOLS[getWyrmType()], enchLevel, isTreasure());
			if (ench.isEmpty()) {
				//No valid enchant, give the error message
				stack = new ItemStack(BWItems.chadBolusSus.get(), enchLevel);
				playSound(BWSounds.wyrmIndigestion.get(), 1, 1);
			}
			else {
				stack = new ItemStack(Items.ENCHANTED_BOOK);
				for (EnchantmentInstance e : ench) EnchantedBookItem.addEnchantment(stack, e);
				playSound(BWSounds.wyrmBook.get(), 1, 1);
			}

		}
		spawnAtLocation(stack);
		gameEvent(GameEvent.ENTITY_PLACE);
	}

	// Rewritten EnchantmentHelper.selectEnchantment for our needs
	private static List<EnchantmentInstance> selectEnchantments(RandomSource rand, @Nullable TagKey<Item> testers, int enchantability, boolean treasure) {
		List<EnchantmentInstance> list = Lists.newArrayList();
		List<EnchantmentInstance> enchants = getValidEnchantments(getTestersFromTag(testers), enchantability, treasure);
		if (enchants.isEmpty() && testers != null && ConfigValues.FALLBACK) enchants = getValidEnchantments(getTestersFromTag(null), enchantability, treasure);
		if (!enchants.isEmpty()) {
			EnchantmentInstance added = WeightedRandom.getRandomItem(rand, enchants).orElse(null);
			if (added == null) return list;
			list.add(added);
			//Make sure our new book isn't worth more value to the wyrms than it was fed
			int remaining = enchantability - added.enchantment.getMinCost(added.level);
			while (rand.nextInt(50) <= enchantability) {
				filterCompatibleEnchantments(enchants, added, remaining);
				if (enchants.isEmpty()) break;

				added = WeightedRandom.getRandomItem(rand, enchants).orElse(null);
				if (added == null) return list;
				list.add(added);
				remaining -= added.enchantment.getMinCost(added.level);
				enchantability /= 2;
			}
		}

		return list;
	}

	//Rewritten EnchantmentHelper.filterCompatibleEnchantments to filter stuff in one pass
	public static void filterCompatibleEnchantments(List<EnchantmentInstance> list, EnchantmentInstance ench, int cap) {
		Iterator<EnchantmentInstance> iterator = list.iterator();
		while (iterator.hasNext()) {
			EnchantmentInstance next = iterator.next();
			if (next.enchantment.getMinCost(next.level) > cap || !ench.enchantment.isCompatibleWith(next.enchantment)) iterator.remove();
		}
	}

	// Rewritten EnchantmentHelper.getAvailableEnchantmentResults for our needs
	private static List<EnchantmentInstance> getValidEnchantments(List<ItemStack> testers, int enchatability, boolean treasure) {
		List<EnchantmentInstance> list = Lists.newArrayList();

		for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
			// TODO Apotheosis when it ports
			if (ench.isAllowedOnBooks() && (treasure || !ench.isTreasureOnly()) && (ench.isDiscoverable() || (treasure && ConfigValues.ALLOW_UNDISCOVERABLE)) && compatibleTesters(ench, testers)) {
				for (int i = ench.getMaxLevel(); i > ench.getMinLevel() - 1; --i) {
					if (enchatability >= ench.getMinCost(i) && (enchatability <= ench.getMaxCost(i) || ConfigValues.IGNORE_MAX)) {
						list.add(new EnchantmentInstance(ench, i));
						break;
					}
				}
			}
		}

		return list;
	}

	private static boolean compatibleTesters(Enchantment ench, List<ItemStack> testers) {
		if (testers.isEmpty()) return true;
		for (ItemStack stack : testers) if (ench.canApplyAtEnchantingTable(stack)) return true;
		return false;
	}

	private static List<ItemStack> getTestersFromTag(@Nullable TagKey<Item> tag) {
		List<ItemStack> list = Lists.newArrayList();
		if (tag == null) return list;

		for (Item item : ForgeRegistries.ITEMS.tags().getTag(tag)) {
			list.add(new ItemStack(item));
		}

		return list;
	}

	public static int getBookValue(ItemStack stack) {
		// Bolus is 1
		if (stack.is(BWItems.chadBolus.get())) return 1;
		// Sums the value of all enchants on the item, assuming it is a book
		int total = 0;

		for (var e : EnchantmentHelper.getEnchantments(stack).entrySet()) {
			// TODO Apotheosis when it ports
			total += e.getKey().getMinCost(e.getValue());
		}

		return total;
	}

	@Override
	public AgeableMob getBreedOffspring(ServerLevel world, AgeableMob mate) {
		BookWyrmEntity child = BWEntities.bookWyrm.get().create(world);
		// If somehow the other breeder isn't a book wyrm, take the sole wyrm parent's genes
		mixGenes(this, mate instanceof BookWyrmEntity ? (BookWyrmEntity) mate : this, child, random);
		return child;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData group, @Nullable CompoundTag compound) {
		wildGenes(this, world.getRandom());
		return super.finalizeSpawn(world, difficulty, spawnType, group, compound);
	}

	public static void wildGenes(BookWyrmEntity wyrm, RandomSource rand) {
		// 50% for grey in the wild, other are equally probable
		if (rand.nextBoolean()) wyrm.setWyrmType(GREY);
		else wyrm.setWyrmType(rand.nextIntBetweenInclusive(1, 6));
		// 1% for wild treasure
		wyrm.setTreasure(rand.nextInt(100) == 0);

		// Normal genes have 33% to be "outstanding"
		// Level
		if (rand.nextInt(3) == 0) wyrm.enchLevel =  ConfigValues.WILDRARE_LEVEL_BASE + rand.nextIntBetweenInclusive(0, ConfigValues.WILDRARE_LEVEL_INC) + rand.nextIntBetweenInclusive(0, ConfigValues.WILDRARE_LEVEL_INC);
		else wyrm.enchLevel = ConfigValues.WILD_LEVEL_BASE + rand.nextIntBetweenInclusive(0, ConfigValues.WILD_LEVEL_INC) + rand.nextIntBetweenInclusive(0, ConfigValues.WILD_LEVEL_INC);
		// Digesting speed
		if (rand.nextInt(3) == 0) wyrm.digestSpeed = ConfigValues.WILDRARE_SPEED_BASE + rand.nextIntBetweenInclusive(0, ConfigValues.WILDRARE_SPEED_INC) + rand.nextIntBetweenInclusive(0, ConfigValues.WILDRARE_SPEED_INC);
		else wyrm.digestSpeed = ConfigValues.WILD_SPEED_BASE + rand.nextIntBetweenInclusive(0, ConfigValues.WILD_SPEED_INC) + rand.nextIntBetweenInclusive(0, ConfigValues.WILD_SPEED_INC);
		// Indigestion chance
		if (rand.nextInt(3) == 0) wyrm.indigestChance = ConfigValues.WILDRARE_INDIGEST_BASE + rand.nextDouble() * ConfigValues.WILDRARE_INDIGEST_INC + rand.nextDouble() * ConfigValues.WILDRARE_INDIGEST_INC;
		else wyrm.indigestChance = ConfigValues.WILD_INDIGEST_BASE + rand.nextDouble() * ConfigValues.WILD_INDIGEST_INC + rand.nextDouble() * ConfigValues.WILD_INDIGEST_INC;
		
		wyrm.clampGenes();
	}

	public static void mixGenes(BookWyrmEntity a, BookWyrmEntity b, BookWyrmEntity child, RandomSource rand) {
		// Type
		int chartType = a.getWyrmType();
		// Grey + something else = take the other
		if (chartType == GREY) chartType = b.getWyrmType();
		// Otherwise take a random parent
		else if (b.getWyrmType() != GREY && rand.nextBoolean()) chartType = b.getWyrmType();
		child.setWyrmType(offspringWyrmType(chartType, rand));

		// Treasure, 10% if both parents, 5% if 1 parent, 1% if 0
		int treasureChance = 100;
		if (a.isTreasure() || b.isTreasure()) {
			if (a.isTreasure() && b.isTreasure()) treasureChance = 10;
			else treasureChance = 20;
		}
		child.setTreasure(rand.nextInt(treasureChance) == 0);
		
		//Stasis
//		if (a.getMutagenStat() == WyrmutagenHelper.STASIS || b.getMutagenStat() == WyrmutagenHelper.STASIS) {
//			//If both have it choose at random
//			BookWyrmEntity donor;
//			if (b.getMutagenStat() != WyrmutagenHelper.STASIS) donor = a;
//			else if (a.getMutagenStat() != WyrmutagenHelper.STASIS) donor = b;
//			else donor = rand.nextBoolean() ? a : b;
//			
//			child.copyGenes(donor);
//			donor.clearMutagenStat();
//		}
//		else {
		// Normal genes, take a random point between the 2 parents then add a random mutation, then clamp to the caps
		// However, for sanity if 2 parents at the "desirable" caps breed then the child inherits it
		// Level
		int min = Math.min(a.enchLevel, b.enchLevel);
		int max = Math.max(a.enchLevel, b.enchLevel);
		if (min == ConfigValues.MAX_LEVEL && max == ConfigValues.MAX_LEVEL) child.enchLevel = ConfigValues.MAX_LEVEL;
		else child.enchLevel = rand.nextIntBetweenInclusive(min, max) + rand.nextIntBetweenInclusive(0, ConfigValues.VARIANCE_LEVEL*2) - ConfigValues.VARIANCE_LEVEL;
		// Speed
		min = Math.min(a.digestSpeed, b.digestSpeed);
		max = Math.max(a.digestSpeed, b.digestSpeed);
		if (min == ConfigValues.MIN_SPEED && max == ConfigValues.MIN_SPEED) child.digestSpeed = ConfigValues.MIN_SPEED;
		else child.digestSpeed = rand.nextIntBetweenInclusive(min, max) + rand.nextIntBetweenInclusive(0, ConfigValues.VARIANCE_SPEED*2) - ConfigValues.VARIANCE_SPEED;
		// Digestion, cap on both ways cause maybe someone wants 100% to farm chad I won't judge
		double min2 = Math.min(a.indigestChance, b.indigestChance);
		double max2 = Math.max(a.indigestChance, b.indigestChance);
		// Because we clamp stuff, MIN should be always lower than the wyrm's values
		if (min2 - ConfigValues.MIN_INDIGEST < 0.01 && max2 - ConfigValues.MIN_INDIGEST < 0.01) child.indigestChance = ConfigValues.MIN_INDIGEST;
		// Because we clamp stuff, MAX should be always higher than the wyrm's values
		else if (ConfigValues.MAX_INDIGEST - min2 < 0.01 && ConfigValues.MAX_INDIGEST - max2 < 0.01) child.indigestChance = ConfigValues.MAX_INDIGEST;
		else child.indigestChance = min2 + rand.nextDouble() * (max2 - min2) + rand.nextDouble() * ConfigValues.VARIANCE_INDIGEST*2 - ConfigValues.VARIANCE_INDIGEST;
		
		child.clampGenes();
	}
	
	private void copyGenes(BookWyrmEntity donor) {
		enchLevel = donor.enchLevel;
		digestSpeed = donor.digestSpeed;
		indigestChance = donor.indigestChance;
	}
	
	private void clampGenes() {
		enchLevel = Mth.clamp(enchLevel, ConfigValues.MIN_LEVEL, ConfigValues.MAX_LEVEL);
		digestSpeed = Mth.clamp(digestSpeed, ConfigValues.MIN_SPEED, ConfigValues.MAX_SPEED);
		indigestChance = Mth.clamp(indigestChance, ConfigValues.MIN_INDIGEST, ConfigValues.MAX_INDIGEST);
	}
	
	private boolean mutateStats(int mutagen) {
		//True if some changes were made and we need to consume it
		switch (mutagen) {
			case WyrmutagenHelper.LVL_UP:
				enchLevel += ConfigValues.MUTAGEN_LEVEL;
				return true;
			case WyrmutagenHelper.LVL_DOWN:
				enchLevel -= ConfigValues.MUTAGEN_LEVEL;
				return true;
			case WyrmutagenHelper.SPEED_UP:
				digestSpeed -= ConfigValues.MUTAGEN_SPEED;
				return true;
			case WyrmutagenHelper.SPEED_DOWN:
				digestSpeed += ConfigValues.MUTAGEN_SPEED;
				return true;
			case WyrmutagenHelper.DIGESTION_UP:
				indigestChance -= ConfigValues.MUTAGEN_INDIGEST;
				return true;
			case WyrmutagenHelper.DIGESTION_DOWN:
				indigestChance += ConfigValues.MUTAGEN_INDIGEST;
				return true;
		}
		return false;
	}

	public static int offspringWyrmType(int type, RandomSource rand) {
		// All have 50% chance to match parent
		if (rand.nextBoolean()) return type;

		// Grey is equiprobable on the rest
		if (type == GREY) return rand.nextIntBetweenInclusive(1, 6);
		// All others have 20% chance to make a grey (so 40% after passing the 50%
		// check)
		if (rand.nextInt(5) < 2) return GREY;

		// Purple is equiprobable on the remainer
		if (type == PURPLE) return rand.nextIntBetweenInclusive(1, 5);
		// All remaining have 5% to make a purple (so 1/6 of the remaining 30%)
		if (rand.nextInt(6) == 0) return PURPLE;

		// Blue and teal are equiprobable on the remaining 4
		if (type == BLUE || type == TEAL) {
			int i = rand.nextIntBetweenInclusive(1, 4);
			if (i >= type) i++;
			return i;
		}

		// Remaining cases: red orange and green
		switch (type) {
			case RED:
				// Equiprobable between orange and blue
				return rand.nextBoolean() ? ORANGE : BLUE;
			case ORANGE:
				// Equiprobable between red and blue
				return rand.nextBoolean() ? RED : BLUE;
			case GREEN:
				// Equiprobable between blue and teal
				return rand.nextBoolean() ? BLUE : TEAL;
		}

		// Fallback
		return type;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return stack.is(Items.BOOK);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return BWSounds.wyrmIdle.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return BWSounds.wyrmHurt.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return BWSounds.wyrmDeath.get();
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		//Subtitles are generic so we good on keeping this.
		playSound(SoundEvents.COW_STEP, 0.15F, 1.0F);
	}
	
	public void startDigestingMutagen() {
		digestTimer = random.nextIntBetweenInclusive(digestSpeed*5, digestSpeed*7);
		setDigesting(true);
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	public int getEnchantingLevel() {
		return enchLevel;
	}

	public int getDigestingSpeed() {
		return digestSpeed;
	}

	public double getIndigestionChance() {
		return indigestChance;
	}

	public int getDigestedLevels() {
		return digested;
	}

	public int getLevelsToDigest() {
		return toDigest;
	}
	
	public int getMutagenColor() {
		return mutagenColor;
	}
	
	public boolean hasMutagen() {
		return mutagenColor >= 0 || mutagenStat >= 0;
	}
	
	public void setMutagenColor(int color) {
		mutagenColor = color;
	}
	
	public int getMutagenStat() {
		return mutagenStat;
	}
	
	public void setMutagenStat(int stat) {
		mutagenStat = stat;
	}
	
	public void clearMutagen() {
		mutagenStat = -1;
		mutagenColor = -1;
	}

	public int getWyrmType() {
		// 7 variants
		return Mth.clamp(entityData.get(DATA) & TYPE_MASK, 0, 6);
	}

	public void setWyrmType(int type) {
		byte b = entityData.get(DATA);
		entityData.set(DATA, (byte) ((b & (~TYPE_MASK) | (type & TYPE_MASK))));
	}

	public boolean isDigesting() {
		return (entityData.get(DATA) & DIGESTING_MASK) > 0;
	}

	public void setDigesting(boolean digest) {
		byte b = (byte) (entityData.get(DATA) & (~DIGESTING_MASK));
		if (digest) b = (byte) (b | DIGESTING_MASK);
		entityData.set(DATA, b);
	}

	public boolean isTreasure() {
		return (entityData.get(DATA) & TREASURE_MASK) > 0;
	}

	public void setTreasure(boolean treasure) {
		byte b = (byte) (entityData.get(DATA) & (~TREASURE_MASK));
		if (treasure) b = (byte) (b | TREASURE_MASK);
		entityData.set(DATA, b);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		setWyrmType(compound.getInt("WyrmType"));
		setTreasure(compound.getBoolean("Treasure"));
		enchLevel = compound.getInt("BWLevel");
		digestSpeed = compound.getInt("BWSpeed");
		indigestChance = compound.getDouble("BWIndigestion");
		digested = compound.getInt("Digested");
		toDigest = compound.getInt("ToDigest");
		digestTimer = compound.getInt("DigestTimer");
		setDigesting(toDigest > 0);
		if (compound.contains("MutagenC")) mutagenColor = compound.getInt("MutagenC");
		if (compound.contains("MutagenS")) mutagenStat = compound.getInt("MutagenS");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("WyrmType", getWyrmType());
		compound.putBoolean("Treasure", isTreasure());
		compound.putInt("BWLevel", enchLevel);
		compound.putInt("BWSpeed", digestSpeed);
		compound.putDouble("BWIndigestion", indigestChance);
		compound.putInt("Digested", digested);
		compound.putInt("ToDigest", toDigest);
		compound.putInt("DigestTimer", digestTimer);
		if (mutagenColor >= 0) compound.putInt("MutagenC", mutagenColor);
		if (mutagenStat >= 0) compound.putInt("MutagenS", mutagenStat);
	}

	private static final ResourceLocation[] LOOT_TABLES = { BookWyrms.rl("entities/book_wyrm_grey"), BookWyrms.rl("entities/book_wyrm_red"),
			BookWyrms.rl("entities/book_wyrm_orange"), BookWyrms.rl("entities/book_wyrm_green"), BookWyrms.rl("entities/book_wyrm_blue"),
			BookWyrms.rl("entities/book_wyrm_teal"), BookWyrms.rl("entities/book_wyrm_purple") };

	@Override
	protected ResourceLocation getDefaultLootTable() {
		return LOOT_TABLES[getWyrmType()];
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(DATA, (byte) 0);
	}

	@Nonnull
	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		// I don't think I need it for this mob, but y'know just in case
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
