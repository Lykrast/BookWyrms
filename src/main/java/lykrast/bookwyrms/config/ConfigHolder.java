package lykrast.bookwyrms.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigHolder {
	public static final ForgeConfigSpec COMMON_SPEC;
	public static final CommonConfig COMMON;

	static {
		Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
		COMMON = specPair.getLeft();
		COMMON_SPEC = specPair.getRight();
	}
}
