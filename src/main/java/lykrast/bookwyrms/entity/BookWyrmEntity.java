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
	// Well how clever can I be to cram EVERYTHING into 1 byte? 0xDG000TTT with
	// Digesting, "Golden", Type (6 types so 3 bits enough)
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
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 12).add(Attributes.MOVEMENT_SPEED, 0.26);
	}

	@Override
	public AgeableMob getBreedOffspring(ServerLevel world, AgeableMob mate) {
		//TODO genetics
		return ModEntities.bookWyrm.get().create(world);
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

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData group, @Nullable CompoundTag compound) {
		//50% for grey in the wild, other are equally probable
		if (world.getRandom().nextBoolean()) setWyrmType(0);
		else setWyrmType(world.getRandom().nextIntBetweenInclusive(1, 6));
		//1% for wild treasure
		setTreasure(world.getRandom().nextInt(100) == 0);
		return super.finalizeSpawn(world, difficulty, spawnType, group, compound);
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

	@Override
	protected ResourceLocation getDefaultLootTable() {
		return BookWyrms.rl("entities/book_wyrm");
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
