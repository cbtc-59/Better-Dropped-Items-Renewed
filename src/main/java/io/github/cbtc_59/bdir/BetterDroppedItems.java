package io.github.cbtc_59.bdir;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterDroppedItems implements ClientModInitializer {
    public static final String MOD_ID = "betterdroppeditems";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // 调试模式开关：设置为true启用调试输出
    public static final boolean DEBUG_MODE = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Better Dropped Items 1.21 已加载!");
    }
}