package lykrast.bookwyrms.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class CommonConfig {
	public final BooleanValue allowUndiscoverable, disableSusWarning, disableColoredPools;
	public final IntValue enchLvlMin, enchLvlMax, digestSpeedMin, digestSpeedMax;
	public final DoubleValue indigestMin, indigestMax;
	public final IntValue enchLvlWildMin, enchLvlWildMax, enchLvlWildRareMin, enchLvlWildRareMax, digestSpeedWildMin, digestSpeedWildMax, digestSpeedWildRareMin, digestSpeedWildRareMax;
	public final DoubleValue indigestWildMin, indigestWildMax, indigestWildRareMin, indigestWildRareMax;
	public final IntValue enchLvlBreedVariance, digestBreedVariance;
	public final DoubleValue indigestBreedVariance;
	public final IntValue enchLvlMutagen, digestMutagen;
	public final DoubleValue indigestMutagen;
	
	public CommonConfig(ForgeConfigSpec.Builder builder) {
		builder.comment("Book recycling config");
		builder.push("enchanting");
		allowUndiscoverable = boolval(builder, "allowUndiscoverable", false, "Allow gold-eyed wyrms to also produce undiscoverable treasure enchants (in vanilla it's just Soul Speed and Swift Sneak)");
		disableSusWarning = boolval(builder, "disableSusWarning", false, "Makes the warning on Suspicious Chad Bolus less intrusive and don't tell people to report it to the modpack",
				"It appears when a given color and enchantment level have zero valid enchantments, which shouldn't happen in vanilla but may happen with configured values or unlucky mod compat",
				"For example I know it happens if the Alex's Mobs Straddleboard is the only item in the purple pool and the level is below 12");
		disableColoredPools = boolval(builder, "disableColoredPools", false, "Makes all Book Wyrms have all enchantments in their pool, making color purely cosmetic", "Same behavior as emptying all the pool tags");
		builder.pop();
		builder.comment("Wyrm stats config");
		builder.push("genes");
		builder.comment("Absolute caps for the wyrm genes");
		builder.push("caps");
		enchLvlMin = intval(builder, "enchLvlMin", 3, 1, Short.MAX_VALUE, "Minimum enchanting level a wyrm can have", "Very low values can allow getting massive quantities of Sharpness/Efficiency/Power I");
		enchLvlMax = intval(builder, "enchLvlMax", 50, 1, Short.MAX_VALUE, "Maximum enchanting level a wyrm can have", "Very high values will result in no enchantments being available (unless Apotheosis for example)");
		digestSpeedMin = intval(builder, "digestSpeedMin", 1, 1, Short.MAX_VALUE, "Fastest digesting speed (in ticks per level) a wyrm can have");
		digestSpeedMax = intval(builder, "digestSpeedMax", 600, 1, Short.MAX_VALUE, "Slowest digesting speed (in ticks per level) a wyrm can have");
		indigestMin = doubleval(builder, "indigestMin", 0, 0, 1, "Lowest indigestion chance (0 = 0%, 1 = 100%) a wyrm can have");
		indigestMax = doubleval(builder, "indigestMax", 1, 0, 1, "Highest indigestion chance (0 = 0%, 1 = 100%) a wyrm can have");
		builder.pop();
		builder.comment("Genes wild wyrms can have");
		builder.push("wild");
		enchLvlWildMin = intval(builder, "enchLvlWildMin", 3, 1, Short.MAX_VALUE, "Minimum enchanting level for wild wyrms");
		enchLvlWildMax = intval(builder, "enchLvlWildMax", 7, 1, Short.MAX_VALUE, "Maximum enchanting level for wild wyrms");
		enchLvlWildRareMin = intval(builder, "enchLvlWildRareMin", 8, 1, Short.MAX_VALUE, "Minimum enchanting level for rare wild wyrms (1/3 chance)");
		enchLvlWildRareMax = intval(builder, "enchLvlWildRareMax", 12, 1, Short.MAX_VALUE, "Maximum enchanting level for rare wild wyrms (1/3 chance)");
		digestSpeedWildMin = intval(builder, "digestSpeedWildMin", 200, 1, Short.MAX_VALUE, "Fastest digesting speed for wild wyrms");
		digestSpeedWildMax = intval(builder, "digestSpeedWildMax", 300, 1, Short.MAX_VALUE, "Slowest digesting speed for wild wyrms");
		digestSpeedWildRareMin = intval(builder, "digestSpeedWildRareMin", 140, 1, Short.MAX_VALUE, "Fastest digesting speed for rare wild wyrms (1/3 chance)");
		digestSpeedWildRareMax = intval(builder, "digestSpeedWildRareMax", 200, 1, Short.MAX_VALUE, "Slowest digesting speed for rare wild wyrms (1/3 chance)");
		indigestWildMin = doubleval(builder, "indigestWildMin", 0.01, 0, 1, "Lowest indigestion chance for wild wyrms");
		indigestWildMax = doubleval(builder, "indigestWildMax", 0.09, 0, 1, "Highest indigestion chance for wild wyrms");
		indigestWildRareMin = doubleval(builder, "indigestWildRareMin", 0.5, 0, 1, "Lowest indigestion chance for rare wild wyrms (1/3 chance)", "Yes by default the rare wild for indigestion have a very high chance");
		indigestWildRareMax = doubleval(builder, "indigestWildRareMax", 0.7, 0, 1, "Highest indigestion chance for rare wild wyrms (1/3 chance)", "Yes by default the rare wild for indigestion have a very high chance");
		builder.pop();
		builder.comment("How do genes vary with breeding");
		builder.push("breeding");
		enchLvlBreedVariance = intval(builder, "enchLvlBreedVariance", 3, 0, Short.MAX_VALUE, "Maximum amount enchanting level can deviate for offsprings (up and down)");
		digestBreedVariance = intval(builder, "digestBreedVariance", 20, 0, Short.MAX_VALUE, "Maximum amount digesting speed can deviate for offsprings (up and down)");
		indigestBreedVariance = doubleval(builder, "indigestBreedVariance", 0.03, 0, 1, "Maximum amount indigestion chance can deviate for offsprings (up and down)");
		builder.pop();
		builder.comment("How much do stat wyrmutagen changes stats");
		builder.push("mutagen");
		enchLvlMutagen = intval(builder, "enchLvlMutagen", 8, 0, Short.MAX_VALUE, "How much does Level Up/Down Wyrmutagen changes enchanting level for the next offspring");
		digestMutagen = intval(builder, "digestMutagen", 60, 0, Short.MAX_VALUE, "How much does Speed Up/Down Wyrmutagen changes digesting speed for the next offspring");
		indigestMutagen = doubleval(builder, "indigestMutagen", 0.1, 0, 1, "How much does Digestion Up/Down Wyrmutagen changes indigestion chance for the next offspring");
		builder.pop();
		builder.pop();
	}
	
	private IntValue intval(ForgeConfigSpec.Builder builder, String name, int def, int min, int max, String... comments) {
		return builder.translation(name).comment(comments).defineInRange(name, def, min, max);
	}
	private DoubleValue doubleval(ForgeConfigSpec.Builder builder, String name, double def, double min, double max, String... comments) {
		return builder.translation(name).comment(comments).defineInRange(name, def, min, max);
	}
	private BooleanValue boolval(ForgeConfigSpec.Builder builder, String name, boolean def, String... comments) {
		return builder.translation(name).comment(comments).define(name, def);
	}

}
