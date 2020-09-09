package sindarin.wanderingmobs;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import sindarin.wanderingmobs.world.SpawnManager;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Wanderingmobs.MOD_ID)
public class ModEvents {
    //Map of the SpawnManager keeping track of spawns in every world
    private static final Map<ServerWorld, SpawnManager> SPAWNER_MANAGER_MAP = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.WorldTickEvent tick) {
        if (!tick.world.isRemote && tick.world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) tick.world;
            if (SPAWNER_MANAGER_MAP.get(serverWorld) == null) {
                SPAWNER_MANAGER_MAP.put(serverWorld, new SpawnManager(serverWorld));
            }
            SpawnManager spawner = SPAWNER_MANAGER_MAP.get(serverWorld);
            spawner.tick();
        }

    }
}
