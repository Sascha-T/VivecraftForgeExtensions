package com.techjar.vivecraftforge;

import java.util.function.BiPredicate;

import com.techjar.vivecraftforge.eventhandler.EventHandlerServer;
import com.techjar.vivecraftforge.network.ChannelHandler;
import com.techjar.vivecraftforge.util.LogHelper;
import com.techjar.vivecraftforge.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.network.NetworkConstants;

@Mod("vivecraftforgeextensions")
public class VivecraftForge {
    public static IModInfo MOD_INFO;

    public VivecraftForge() {
        MOD_INFO = ModLoadingContext.get().getActiveContainer().getModInfo();

        // Fixed it lol, read the documentation - 989onan
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (BiPredicate<String, Boolean>) (a, b) -> true));
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // I dunno how to make "safe" not crash, it doesn't make any sense and gives unhelpful errors
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> serverInit(eventBus));
    }

    private void serverInit(IEventBus eventBus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.config);
        eventBus.addListener(this::onSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        MinecraftForge.EVENT_BUS.register(new EventHandlerServer());
    }

    private void onSetup(FMLCommonSetupEvent event) {
        ChannelHandler.init();
    }

    private void onServerStarting(FMLDedicatedServerSetupEvent event) {
        if (Config.printMoney.get())
            LogHelper.warning(Util.getMoney());
    }
}
