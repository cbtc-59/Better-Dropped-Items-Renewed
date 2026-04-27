package io.github.cbtc_59.bdir;

import io.github.cbtc_59.bdir.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterDroppedItems implements ClientModInitializer {
    public static final String MOD_ID = "betterdroppeditemsrenewed";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // 新添加的配置实例
    public static ModConfig CONFIG;

    @Override
    public void onInitializeClient() {
        // 注册配置
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        LOGGER.info("Better Dropped Items 1.21 已加载！");
    }
}