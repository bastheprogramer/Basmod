package com.vsdguzman.gamerules;

import com.vsdguzman.Basmod;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;

public class CustomGameRules {

    private static final CustomGameRuleCategory TNT = new CustomGameRuleCategory(
            Identifier.of(Basmod.MOD_ID, "tnt"),
            Text.literal("TNT").formatted(Formatting.YELLOW,Formatting.BOLD) // Display name for the category
    );

    // Registers a gamerule "tntdups" under the EXPLOSION category with a default value of false.
    public static final GameRules.Key<GameRules.IntRule> TNTDUPS = GameRuleRegistry.register(
            "tntdups", TNT, GameRuleFactory.createIntRule(0,0)

    );

    public static final GameRules.Key<BooleanRule> TNTARROWTRAIL = GameRuleRegistry.register(
            "tntarrowtrail", TNT, GameRuleFactory.createBooleanRule(false)
    );
    public static final GameRules.Key<BooleanRule> Boucing_Arrows =
            GameRuleRegistry.register("BoucingArrows",TNT, GameRuleFactory.createBooleanRule(false));

    public static final GameRules.Key<BooleanRule> Relistic_tnt =
            GameRuleRegistry.register("Relistictnt",TNT, GameRuleFactory.createBooleanRule(false));

    public static void register() {
        // This method can be called from your mod's initialization code if needed.
    }
}
