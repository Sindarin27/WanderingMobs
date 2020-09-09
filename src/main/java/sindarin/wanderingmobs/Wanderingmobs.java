package sindarin.wanderingmobs;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Wanderingmobs.MOD_ID)
public class Wanderingmobs {

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "wanderingmobs";


    public Wanderingmobs() {

    }
}
