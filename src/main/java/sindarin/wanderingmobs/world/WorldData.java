package sindarin.wanderingmobs.world;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import java.util.UUID;

public class WorldData extends WorldSavedData {
    private static final String
            name = "wanderingmobs",
            delayNbt = "MobSpawnDelay",
            chanceNbt = "MobSpawnChance",
            idNbt = "MobID";

    private World world;
    private int tickCounter;
    private int mobSpawnDelay;
    private int mobSpawnChance;
    private UUID mobID;

    public WorldData() {
        super(name);
    }

    public static WorldData get(World world) {
        if (world instanceof ServerWorld && world.getServer() != null) {
            ServerWorld overworld = world.getServer().func_71218_a(DimensionType.OVERWORLD);

            DimensionSavedDataManager storage = overworld.getSavedData();
            WorldData data = storage.getOrCreate(WorldData::new, name);

            data.world = world;
            data.markDirty();

            return data;
        }
        return null;
    }

    public int getMobSpawnDelay() {
        return mobSpawnDelay;
    }

    public int getMobSpawnChance() {
        return mobSpawnChance;
    }

    public UUID getMobID() {
        return mobID;
    }

    public void setMobSpawnDelay(int mobSpawnDelay) {
        this.mobSpawnDelay = mobSpawnDelay;
    }

    public void setMobSpawnChance(int mobSpawnChance) {
        this.mobSpawnChance = mobSpawnChance;
    }

    public void setMobID(UUID mobID) {
        this.mobID = mobID;
    }

    public void tick() {
        tickCounter++;
    }

    @Override
    public void read(CompoundNBT nbt) {
        if (nbt.contains(delayNbt, 99)) {
            mobSpawnDelay = nbt.getInt(delayNbt);
        }
        if (nbt.contains(chanceNbt, 99)) {
            mobSpawnChance = nbt.getInt(chanceNbt);
        }
        if (nbt.contains(idNbt, 8)) {
            mobID = UUID.fromString(nbt.getString(idNbt));
        }
    }

    @MethodsReturnNonnullByDefault
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt(delayNbt, this.mobSpawnDelay);
        compound.putInt(chanceNbt, this.mobSpawnChance);
        if (this.mobID != null) {
            compound.putString(idNbt, this.mobID.toString());
        }
        return compound;
    }


}
