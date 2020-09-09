package sindarin.wanderingmobs.world;

import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import org.apache.logging.log4j.Level;
import sindarin.wanderingmobs.Wanderingmobs;
import sindarin.wanderingmobs.entity.goals.MoveToGoal;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;
import java.util.function.BooleanSupplier;

public class SpawnManager {
    private final int defaultSpawnDelay = 24000,
            staticSpawnChance = 10; //1 in 10 chance of actually spawning the mob on a succesful chance
    private final EntityType<? extends CreatureEntity> leader = EntityType.PIG;
    private final EntityType<? extends CreatureEntity> companion = EntityType.ZOMBIE_PIGMAN;
    private final BooleanSupplier canSpawn;

    private final Random random = new Random();
    private final ServerWorld world;

    private int attemptTimer;
    private int spawnDelay;
    private int spawnChance;

    public SpawnManager(ServerWorld world) {
        this.world = world;
        canSpawn = (() -> world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING));

        this.attemptTimer = 1200;
        WorldData worldinfo = WorldData.get(world);
        this.spawnDelay = worldinfo.getMobSpawnDelay();
        this.spawnChance = worldinfo.getMobSpawnChance();
        if (this.spawnDelay == 0 && this.spawnChance == 0) {
            this.spawnDelay = defaultSpawnDelay;
            worldinfo.setMobSpawnDelay(this.spawnDelay);
            this.spawnChance = 25;
            worldinfo.setMobSpawnChance(this.spawnChance);
        }
    }

    public void tick() {
        //This is the gamerule for wandering trader spawns
        if (this.world.getGameRules().getBoolean(GameRules.field_230128_E_) && --this.attemptTimer <= 0) {
            this.attemptTimer = 1200;
            WorldData worldinfo = WorldData.get(world);
            this.spawnDelay -= 1200;
            worldinfo.setMobSpawnDelay(this.spawnDelay);
            Wanderingmobs.LOGGER.log(Level.DEBUG, "1200 ticks passed, updating spawn delay");
            if (this.spawnDelay <= 0) {
                //TODO: make delay configurable
                this.spawnDelay = defaultSpawnDelay;
                //TODO: add more predicates
                if (canSpawn.getAsBoolean()) {
                    int currentChance = this.spawnChance;
                    Wanderingmobs.LOGGER.log(Level.DEBUG, "Attempt made");
                    //Every attempt, chance gets increased
                    //TODO: make chance step, min and max configurable
                    this.spawnChance = MathHelper.clamp(this.spawnChance + 25, 25, 75);
                    worldinfo.setMobSpawnChance(this.spawnChance);
                    //TODO: make base chance configurable
                    if (this.random.nextInt(100) <= currentChance && this.try_spawning()) {
                        this.spawnChance = 25;
                    }
                }
            }
        }
    }

    private boolean try_spawning() {
        Wanderingmobs.LOGGER.log(Level.DEBUG, "Attempt triggered, chance of spawn");
        PlayerEntity playerentity = this.world.getRandomPlayer();
        if (playerentity == null) {
            return true;
        } else if (this.random.nextInt(staticSpawnChance) != 0) {
            return false;
        } else {
            Wanderingmobs.LOGGER.log(Level.DEBUG, "Attempt succesful, trying to spawn");
            BlockPos blockpos = playerentity.getPosition();
            PointOfInterestManager pointofinterestmanager = this.world.getPointOfInterestManager();
            Optional<BlockPos> optional = pointofinterestmanager.func_219127_a(PointOfInterestType.MEETING.func_221045_c(), (p_221241_0_) -> true, blockpos, 48, PointOfInterestManager.Status.ANY);
            BlockPos blockpos1 = optional.orElse(blockpos);
            BlockPos spawnPos = this.getSpawnPos(blockpos1, 48);
            if (spawnPos != null && this.checkCollision(spawnPos)) {
                if (this.world.func_226691_t_(spawnPos) == Biomes.THE_VOID) {
                    return false;
                }

                //TODO: make mob configurable
                CreatureEntity spawnEntity = leader.spawn(this.world, null, null, null, spawnPos, SpawnReason.EVENT, false, false);
                //Todo: make companions configurable
                if (spawnEntity != null) {
                    for (int j = 0; j < 3; ++j) {
                        this.summonCompanions(spawnEntity, 4);
                    }

                    WorldData.get(world).setMobID(spawnEntity.getUniqueID());
                    //wanderingtraderentity.setDespawnDelay(48000);
                    spawnEntity.targetSelector.addGoal(0, new MoveToGoal(spawnEntity, 2.0D, 0.35D, blockpos1));
                    spawnEntity.setHomePosAndDistance(blockpos1, 16);
                    Wanderingmobs.LOGGER.log(Level.DEBUG, "Spawned at " + spawnPos.toString());
                    return true;
                }
            }

            return false;
        }
    }

    private void summonCompanions(Entity mob, int range) {
        BlockPos blockpos = this.getSpawnPos(new BlockPos(mob), range);
        if (blockpos != null) {
            CreatureEntity companionEntity = companion.spawn(this.world, null, null, null, blockpos, SpawnReason.EVENT, false, false);
            if (companionEntity != null) {
                companionEntity.setLeashHolder(mob, true);
            }
        }
    }

    @Nullable
    private BlockPos getSpawnPos(BlockPos pos, int range) {
        BlockPos blockpos = null;

        for (int attempt = 0; attempt < 10; ++attempt) {
            int j = pos.getX() + this.random.nextInt(range * 2) - range;
            int k = pos.getZ() + this.random.nextInt(range * 2) - range;
            int l = this.world.getHeight(Heightmap.Type.WORLD_SURFACE, j, k);
            BlockPos blockpos1 = new BlockPos(j, l, k);
            if (WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, this.world, blockpos1, EntityType.WANDERING_TRADER)) {
                blockpos = blockpos1;
                break;
            }
        }

        return blockpos;
    }

    private boolean checkCollision(BlockPos pos) {
        for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos, pos.add(1, 2, 1))) {
            if (!this.world.getBlockState(blockpos).getCollisionShape(this.world, blockpos).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
