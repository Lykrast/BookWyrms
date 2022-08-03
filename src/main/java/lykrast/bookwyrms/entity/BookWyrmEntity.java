package lykrast.bookwyrms.entity;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import lykrast.bookwyrms.BookWyrms;
import lykrast.bookwyrms.registry.ModEntities;
import lykrast.bookwyrms.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
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

	public BookWyrmEntity(EntityType<? extends BookWyrmEntity> type, Level world) {
		super(type, world);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new PanicGoal(this, 2));
		goalSelector.addGoal(2, new BreedGoal(this, 1));
		goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.BOOK, Items.ENCHANTED_BOOK), false));
		goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6f));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 12).add(Attributes.MOVEMENT_SPEED, 0.25);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) {
			if (!isBaby() && (stack.is(Items.ENCHANTED_BOOK) || stack.is(ModItems.chadBolus.get()))) return InteractionResult.CONSUME;
			else return InteractionResult.PASS;
		}
		else {
			if (!isBaby() && (stack.is(Items.ENCHANTED_BOOK) || stack.is(ModItems.chadBolus.get()))) {
				// Eat enchanted book
				toDigest += getBookValue(stack);
				if (digestTimer <= 0) digestTimer = digestSpeed;
				setDigesting(true);

				if (!player.getAbilities().instabuild) stack.shrink(1);
				playSound(SoundEvents.GENERIC_EAT, 1, 1);
			}
		}
		return super.mobInteract(player, hand);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!level.isClientSide && toDigest > 0) {
			digestTimer--;
			if (digestTimer <= 0) {
				digested++;
				toDigest--;
				digestTimer = digestSpeed;
				if (digested >= enchLevel) {
					digested -= enchLevel;
					makeBook();
				}
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
			stack = new ItemStack(ModItems.chadBolus.get(), random.nextIntBetweenInclusive(1, enchLevel));
			// TODO a blergh sound
			playSound(SoundEvents.CHICKEN_EGG, 1, 1);
		}
		else {
			//Based on EnchantmentHelper.enchantItem
			@SuppressWarnings("unchecked")
			List<EnchantmentInstance> ench = selectEnchantments(random, (TagKey<Item>) POOLS[getWyrmType()], enchLevel, isTreasure());
			if (ench.isEmpty()) {
				//No valid enchant, give the error message
				stack = new ItemStack(ModItems.chadBolusSus.get(), enchLevel);
				//TODO a blergh sound
				playSound(SoundEvents.CHICKEN_EGG, 1, 1);
			}
			else {
				stack = new ItemStack(Items.ENCHANTED_BOOK);
				for (EnchantmentInstance e : ench) EnchantedBookItem.addEnchantment(stack, e);
				playSound(SoundEvents.CHICKEN_EGG, 1, 1);
			}

		}
		spawnAtLocation(stack);
		gameEvent(GameEvent.ENTITY_PLACE);
	}

	// Rewritten EnchantmentHelper.selectEnchantment for our needs
	private static List<EnchantmentInstance> selectEnchantments(RandomSource rand, @Nullable TagKey<Item> testers, int enchantability, boolean treasure) {
		List<EnchantmentInstance> list = Lists.newArrayList();
		List<EnchantmentInstance> enchants = getValidEnchantments(getTestersFromTag(testers), enchantability, treasure);
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
			// For now I'll take the choice of not allowing Soul Speed and Swift Sneak,
			// might change
			if (ench.isDiscoverable() && ench.isAllowedOnBooks() && (treasure || !ench.isTreasureOnly()) && compatibleTesters(ench, testers)) {
				for (int i = ench.getMaxLevel(); i > ench.getMinLevel() - 1; --i) {
					if (enchatability >= ench.getMinCost(i) && enchatability <= ench.getMaxCost(i)) {
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

		// The deprecation is not for our use
		for (Item item : ForgeRegistries.ITEMS.tags().getTag(tag)) {
			list.add(new ItemStack(item));
		}

		return list;
	}

	public static int getBookValue(ItemStack stack) {
		// Bolus is 1
		if (stack.is(ModItems.chadBolus.get())) return 1;
		// Sums the value of all enchants on the item, assuming it is a book
		int total = 0;

		for (var e : EnchantmentHelper.getEnchantments(stack).entrySet()) {
			// TODO Apotheosis check
			total += e.getKey().getMinCost(e.getValue());
		}

		return total;
	}

	@Override
	public AgeableMob getBreedOffspring(ServerLevel world, AgeableMob mate) {
		BookWyrmEntity child = ModEntities.bookWyrm.get().create(world);
		// If somehow the other breeder isn't a book wyrm, take the sole wyrm parent's
		// genes
		mixGenes(this, mate instanceof BookWyrmEntity ? (BookWyrmEntity) mate : this, child, random);
		return child;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData group,
			@Nullable CompoundTag compound) {
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
		// Level, wild is 3-7 on a bellish curve, 8-12 for outstanding
		wyrm.enchLevel = 3 + rand.nextIntBetweenInclusive(0, 2) + rand.nextIntBetweenInclusive(0, 2);
		if (rand.nextInt(3) == 0) wyrm.enchLevel += 5;
		// Digesting speed, 200-300, 133-200 for outstanding
		wyrm.digestSpeed = 200 + rand.nextIntBetweenInclusive(0, 50) + rand.nextIntBetweenInclusive(0, 50);
		if (rand.nextInt(3) == 0) wyrm.digestSpeed = (wyrm.digestSpeed * 2) / 3;
		// Indigestion chance is 1-9%, outstanding 50-70
		if (rand.nextInt(3) == 0) wyrm.indigestChance = 0.5 + rand.nextDouble() * 0.1 + rand.nextDouble() * 0.1;
		else wyrm.indigestChance = 0.01 + rand.nextDouble() * 0.04 + rand.nextDouble() * 0.04;
	}

	// TODO Config?
	public static final int MIN_LEVEL = 3, MAX_LEVEL = 50, MIN_SPEED = 1, MAX_SPEED = 600;

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

		// Normal genes, take a random point between the 2 parents
		// Then add a random mutation, then clamp to the caps
		// However, for sanity if 2 parents at the "desirable" caps breed then the child
		// inherits it
		// Level
		int min = Math.min(a.enchLevel, b.enchLevel);
		int max = Math.max(a.enchLevel, b.enchLevel);
		if (min == MAX_LEVEL && max == MAX_LEVEL) child.enchLevel = MAX_LEVEL;
		else child.enchLevel = Mth.clamp(rand.nextIntBetweenInclusive(min, max) + rand.nextIntBetweenInclusive(0, 6) - 3, MIN_LEVEL, MAX_LEVEL);
		// Speed
		min = Math.min(a.digestSpeed, b.digestSpeed);
		max = Math.max(a.digestSpeed, b.digestSpeed);
		if (min == MIN_SPEED && max == MIN_SPEED) child.digestSpeed = MIN_SPEED;
		else child.digestSpeed = Mth.clamp(rand.nextIntBetweenInclusive(min, max) + rand.nextIntBetweenInclusive(0, 40) - 20, MIN_SPEED, MAX_SPEED);
		// Digestion, cap on both ways cause maybe someone wants 100% to farm chad I
		// won't judge
		double min2 = Math.min(a.indigestChance, b.indigestChance);
		double max2 = Math.max(a.indigestChance, b.indigestChance);
		if (min2 < 0.01 && max2 < 0.01) child.indigestChance = 0;
		else if (min2 > 0.99 && max2 > 0.99) child.indigestChance = 1;
		else child.indigestChance = Mth.clamp(min2 + rand.nextDouble() * (max2 - min2) + rand.nextDouble() * 0.06 - 0.03, 0, 1);
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
		return SoundEvents.COW_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.COW_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.COW_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		playSound(SoundEvents.COW_STEP, 0.15F, 1.0F);
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
	public Packet<?> getAddEntityPacket() {
		// I don't think I need it for this mob, but y'know just in case
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
