package lykrast.bookwyrms.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lykrast.bookwyrms.BookWyrms;
import lykrast.bookwyrms.registry.ModEntities;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;

public class BookWyrmEntity extends Animal {
	// Types 0-6 : grey, red, orange, green, blue, teal, purple
	public static final int GREY = 0, RED = 1, ORANGE = 2, GREEN = 3, BLUE = 4, TEAL = 5, PURPLE = 6;
	// Well how clever can I be to cram EVERYTHING into 1 byte?
	// 0xDG000TTT with Digesting, "Golden", Type (6 types so 3 bits enough)
	private static final EntityDataAccessor<Byte> DATA = SynchedEntityData.defineId(BookWyrmEntity.class, EntityDataSerializers.BYTE);
	private static final int DIGESTING_MASK = 0b10000000;
	private static final int TREASURE_MASK = 0b01000000;
	private static final int TYPE_MASK = 0b00111111;
	//Stats
	private int level, speed;
	private double indigestChance;
	//Digestion
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
	public AgeableMob getBreedOffspring(ServerLevel world, AgeableMob mate) {
		BookWyrmEntity child = ModEntities.bookWyrm.get().create(world);
		//If somehow the other breeder isn't a book wyrm, take the sole wyrm parent's genes
		mixGenes(this, mate instanceof BookWyrmEntity ? (BookWyrmEntity)mate : this, child, random);
		return child;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData group, @Nullable CompoundTag compound) {
		wildGenes(this, world.getRandom());
		return super.finalizeSpawn(world, difficulty, spawnType, group, compound);
	}
	
	public static void wildGenes(BookWyrmEntity wyrm, RandomSource rand) {
		//50% for grey in the wild, other are equally probable
		if (rand.nextBoolean()) wyrm.setWyrmType(GREY);
		else wyrm.setWyrmType(rand.nextIntBetweenInclusive(1, 6));
		//1% for wild treasure
		wyrm.setTreasure(rand.nextInt(100) == 0);
		
		//Normal genes have 33% to be "outstanding"
		//Level, wild is 3-7 on a bellish curve, 8-12 for outstanding
		wyrm.level = 3 + rand.nextIntBetweenInclusive(0, 2) + rand.nextIntBetweenInclusive(0, 2);
		if (rand.nextInt(3) == 0) wyrm.level += 5;
		//Digesting speed, 200-300, 133-200 for outstanding
		wyrm.speed = 200 + rand.nextIntBetweenInclusive(0, 50) + rand.nextIntBetweenInclusive(0, 50);
		if (rand.nextInt(3) == 0) wyrm.speed = (wyrm.speed * 2) / 3;
		//Indigestion chance is 1-9%, outstanding 40-60
		if (rand.nextInt(3) == 0) wyrm.indigestChance = 0.4 + rand.nextDouble()*0.1 + rand.nextDouble()*0.1;
		else wyrm.indigestChance = 0.01 + rand.nextDouble()*0.04 + rand.nextDouble()*0.04;
	}
	
	public static void mixGenes(BookWyrmEntity a, BookWyrmEntity b, BookWyrmEntity child, RandomSource rand) {
		//Type
		int chartType = a.getWyrmType();
		//Grey + something else = take the other
		if (chartType ==  GREY) chartType = b.getWyrmType();
		//Otherwise take a random parent
		else if (b.getWyrmType() != GREY && rand.nextBoolean()) chartType = b.getWyrmType();
		child.setWyrmType(offspringWyrmType(chartType, rand));
		
		//Treasure, 10% if both parents, 5% if 1 parent, 1% if 0
		int treasureChance = 100;
		if (a.isTreasure() || b.isTreasure()) {
			if (a.isTreasure() && b.isTreasure()) treasureChance = 10;
			else treasureChance = 20;
		}
		child.setTreasure(rand.nextInt(treasureChance) == 0);
		
		//Normal genes, take a random point between the 2 parents
		//Then add a random mutation, then clamp to the caps
		//Level
		int min = Math.min(a.level, b.level);
		int max = Math.max(a.level, b.level);
		child.level = Mth.clamp(rand.nextIntBetweenInclusive(min, max) + rand.nextIntBetweenInclusive(0, 6) - 3, 3, 50);
		//Speed
		min = Math.min(a.speed, b.speed);
		max = Math.max(a.speed, b.speed);
		child.speed = Mth.clamp(rand.nextIntBetweenInclusive(min, max) + rand.nextIntBetweenInclusive(0, 40) - 20, 1, 600);
		//Digestion, for everyone sanity if both parents are at 0% then keep it at 0%
		double min2 = Math.min(a.indigestChance, b.indigestChance);
		double max2 = Math.max(a.indigestChance, b.indigestChance);
		if (min2 < 0.01 && max2 < 0.01) child.indigestChance = 0;
		else child.indigestChance = Mth.clamp(min2 + rand.nextDouble() * (max2-min2) + rand.nextDouble()*0.06 - 0.03, 0, 1);
	}
	
	public static int offspringWyrmType(int type, RandomSource rand) {
		//All have 50% chance to match parent
		if (rand.nextBoolean()) return type;
		
		//Grey is equiprobable on the rest
		if (type == GREY) return rand.nextIntBetweenInclusive(1, 6);
		//All others have 20% chance to make a grey (so 40% after passing the 50% check)
		if (rand.nextInt(5) < 2) return GREY;
		
		//Purple is equiprobable on the remainer
		if (type == PURPLE) return rand.nextIntBetweenInclusive(1, 5);
		//All remaining have 5% to make a purple (so 1/6 of the remaining 30%)
		if (rand.nextInt(6) == 0) return PURPLE;
		
		//Blue and teal are equiprobable on the remaining 4
		if (type == BLUE || type == TEAL) {
			int i = rand.nextIntBetweenInclusive(1, 4);
			if (i >= type) i++;
			return i;
		}
		
		//Remaining cases: red orange and green
		switch (type) {
			case RED:
				//Equiprobable between orange and blue
				return rand.nextBoolean() ? ORANGE : BLUE;
			case ORANGE:
				//Equiprobable between red and blue
				return rand.nextBoolean() ? RED : BLUE;
			case GREEN:
				//Equiprobable between blue and teal
				return rand.nextBoolean() ? BLUE : TEAL;
		}
		
		//Fallback
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
		return level;
	}
	
	public int getDigestingSpeed() {
		return speed;
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
		//7 variants
		return Mth.clamp(entityData.get(DATA) & TYPE_MASK, 0, 6);
	}
	
	public void setWyrmType(int type) {
		byte b = entityData.get(DATA);
		entityData.set(DATA, (byte)((b & (~TYPE_MASK) | (type & TYPE_MASK))));
	}
	
	public boolean isDigesting() {
		return (entityData.get(DATA) & DIGESTING_MASK) > 0;
	}
	
	public void setDigesting(boolean digest) {
		byte b = (byte)(entityData.get(DATA) & (~DIGESTING_MASK));
		if (digest) b = (byte)(b | DIGESTING_MASK);
		entityData.set(DATA, b);
	}
	
	public boolean isTreasure() {
		return (entityData.get(DATA) & TREASURE_MASK) > 0;
	}
	
	public void setTreasure(boolean treasure) {
		byte b = (byte)(entityData.get(DATA) & (~TREASURE_MASK));
		if (treasure) b = (byte)(b | TREASURE_MASK);
		entityData.set(DATA, b);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		setWyrmType(compound.getInt("WyrmType"));
		setTreasure(compound.getBoolean("Treasure"));
		level = compound.getInt("BWLevel");
		speed = compound.getInt("BWSpeed");
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
		compound.putInt("BWLevel", level);
		compound.putInt("BWSpeed", speed);
		compound.putDouble("BWIndigestion", indigestChance);
		compound.putInt("Digested", digested);
		compound.putInt("ToDigest", toDigest);
		compound.putInt("DigestTimer", digestTimer);
	}
	
	private static final ResourceLocation[] LOOT_TABLES = {
			BookWyrms.rl("entities/book_wyrm_grey"),
			BookWyrms.rl("entities/book_wyrm_red"),
			BookWyrms.rl("entities/book_wyrm_orange"),
			BookWyrms.rl("entities/book_wyrm_green"),
			BookWyrms.rl("entities/book_wyrm_blue"),
			BookWyrms.rl("entities/book_wyrm_teal"),
			BookWyrms.rl("entities/book_wyrm_purple")
	};

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
