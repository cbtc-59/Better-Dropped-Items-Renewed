package io.github.cbtc_59.bdir.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "betterdroppeditemsrenewed")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 360)
    public int initialRotationAngle = 0;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 1000)
    public int rotationSpeed = 100;

    @ConfigEntry.Gui.Tooltip
    public boolean itemPhysic2DRenderMode = false;

    @ConfigEntry.Gui.Tooltip
    public boolean debugMode = false;
}