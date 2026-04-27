package io.github.cbtc_59.bdir.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "betterdroppeditemsrenewed")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean debugMode = false;
}