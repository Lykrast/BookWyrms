package lykrast.bookwyrms.config;

import net.minecraftforge.fml.config.ModConfig;

public class ConfigValues {
	// Copying Alex's Mobs and deriving the needed values here from the config
	public static int MIN_LEVEL = 3, MAX_LEVEL = 50, MIN_SPEED = 1, MAX_SPEED = 600;
	public static double MIN_INDIGEST = 0, MAX_INDIGEST = 1;
	
	public static int WILD_LEVEL_BASE = 3, WILD_LEVEL_INC = 2, WILDRARE_LEVEL_BASE = 8, WILDRARE_LEVEL_INC = 2;
	public static int WILD_SPEED_BASE = 200, WILD_SPEED_INC = 50, WILDRARE_SPEED_BASE = 140, WILDRARE_SPEED_INC = 30;
	public static double WILD_INDIGEST_BASE = 0.01, WILD_INDIGEST_INC = 0.04, WILDRARE_INDIGEST_BASE = 0.5, WILDRARE_INDIGEST_INC = 0.1;
	
	public static int VARIANCE_LEVEL = 3, VARIANCE_SPEED = 20;
	public static double VARIANCE_INDIGEST = 0.03;
	
	public static int MUTAGEN_LEVEL = 8, MUTAGEN_SPEED = 60;
	public static double MUTAGEN_INDIGEST = 0.1;
	
	public static boolean ALLOW_UNDISCOVERABLE = false, DISABLE_SUS_WARNING = false, DISABLE_COLOR = false;
	
	public static void refresh(ModConfig config) {
		//I'm probably supposed to actually impose constraints here, but eeeeh
		MIN_LEVEL = Math.min(ConfigHolder.COMMON.enchLvlMin.get(), ConfigHolder.COMMON.enchLvlMax.get());
		MAX_LEVEL = Math.max(ConfigHolder.COMMON.enchLvlMin.get(), ConfigHolder.COMMON.enchLvlMax.get());
		MIN_SPEED = Math.min(ConfigHolder.COMMON.digestSpeedMin.get(), ConfigHolder.COMMON.digestSpeedMax.get());
		MAX_SPEED = Math.max(ConfigHolder.COMMON.digestSpeedMin.get(), ConfigHolder.COMMON.digestSpeedMax.get());
		MIN_INDIGEST = Math.min(ConfigHolder.COMMON.indigestMin.get(), ConfigHolder.COMMON.indigestMax.get());
		MAX_INDIGEST = Math.max(ConfigHolder.COMMON.indigestMin.get(), ConfigHolder.COMMON.indigestMax.get());
		{
			//Stuff gets clamped anyway so no need to compare to the actual hard caps
			int min = Math.min(ConfigHolder.COMMON.enchLvlWildMin.get(), ConfigHolder.COMMON.enchLvlWildMax.get());
			int max = Math.max(ConfigHolder.COMMON.enchLvlWildMin.get(), ConfigHolder.COMMON.enchLvlWildMax.get());
			WILD_LEVEL_BASE = min;
			WILD_LEVEL_INC = (max - min) / 2;
			min = Math.min(ConfigHolder.COMMON.enchLvlWildRareMin.get(), ConfigHolder.COMMON.enchLvlWildRareMax.get());
			max = Math.max(ConfigHolder.COMMON.enchLvlWildRareMin.get(), ConfigHolder.COMMON.enchLvlWildRareMax.get());
			WILDRARE_LEVEL_BASE = min;
			WILDRARE_LEVEL_INC = (max - min) / 2;
			min = Math.min(ConfigHolder.COMMON.digestSpeedWildMin.get(), ConfigHolder.COMMON.digestSpeedWildMax.get());
			max = Math.max(ConfigHolder.COMMON.digestSpeedWildMin.get(), ConfigHolder.COMMON.digestSpeedWildMax.get());
			WILD_SPEED_BASE = min;
			WILD_SPEED_INC = (max - min) / 2;
			min = Math.min(ConfigHolder.COMMON.digestSpeedWildRareMin.get(), ConfigHolder.COMMON.digestSpeedWildRareMax.get());
			max = Math.max(ConfigHolder.COMMON.digestSpeedWildRareMin.get(), ConfigHolder.COMMON.digestSpeedWildRareMax.get());
			WILDRARE_SPEED_BASE = min;
			WILDRARE_SPEED_INC = (max - min) / 2;
		}
		{
			//Stuff gets clamped anyway so no need to compare to the actual hard caps
			double min = Math.min(ConfigHolder.COMMON.indigestWildMin.get(), ConfigHolder.COMMON.indigestWildMax.get());
			double max = Math.max(ConfigHolder.COMMON.indigestWildMin.get(), ConfigHolder.COMMON.indigestWildMax.get());
			WILD_INDIGEST_BASE = min;
			WILD_INDIGEST_INC = (max - min) / 2.0;
			min = Math.min(ConfigHolder.COMMON.indigestWildRareMin.get(), ConfigHolder.COMMON.indigestWildRareMax.get());
			max = Math.max(ConfigHolder.COMMON.indigestWildRareMin.get(), ConfigHolder.COMMON.indigestWildRareMax.get());
			WILDRARE_INDIGEST_BASE = min;
			WILDRARE_INDIGEST_INC = (max - min) / 2.0;
		}
		VARIANCE_LEVEL = ConfigHolder.COMMON.enchLvlBreedVariance.get();
		VARIANCE_SPEED = ConfigHolder.COMMON.digestBreedVariance.get();
		VARIANCE_INDIGEST = ConfigHolder.COMMON.indigestBreedVariance.get();
		MUTAGEN_LEVEL = ConfigHolder.COMMON.enchLvlMutagen.get();
		MUTAGEN_SPEED = ConfigHolder.COMMON.digestMutagen.get();
		MUTAGEN_INDIGEST = ConfigHolder.COMMON.indigestMutagen.get();
		ALLOW_UNDISCOVERABLE = ConfigHolder.COMMON.allowUndiscoverable.get();
		DISABLE_SUS_WARNING = ConfigHolder.COMMON.disableSusWarning.get();
		DISABLE_COLOR = ConfigHolder.COMMON.disableColoredPools.get();
	}
}
