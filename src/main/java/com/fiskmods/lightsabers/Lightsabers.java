package com.fiskmods.lightsabers;

import com.fiskmods.lightsabers.common.block.ModBlocks;
import com.fiskmods.lightsabers.common.config.ModConfig;
import com.fiskmods.lightsabers.common.command.CommandForce;
import com.fiskmods.lightsabers.common.container.ModMenus;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.ALEntityData;
import com.fiskmods.lightsabers.common.data.ALPlayerData;
import com.fiskmods.lightsabers.common.data.generator.ModDataGenerators;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.entity.ModEntities;
import com.fiskmods.lightsabers.common.event.CommonEventHandlerDL;
import com.fiskmods.lightsabers.common.event.CommonEventHandler;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerData;
import com.fiskmods.lightsabers.common.hilt.HiltManager;
import com.fiskmods.lightsabers.common.generator.worldgen.ModWorldgen;
import com.fiskmods.lightsabers.common.integration.epicfight.EpicFightIntegration;
import com.fiskmods.lightsabers.common.item.ModCreativeTabs;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.network.ALNetworkManager;
import com.fiskmods.lightsabers.common.recipe.ModRecipeSerializers;
import com.fiskmods.lightsabers.common.proxy.ClientProxy;
import com.fiskmods.lightsabers.common.proxy.CommonProxy;
import com.fiskmods.lightsabers.common.sound.ModSounds;
import com.fiskmods.lightsabers.common.tileentity.ModBlockEntities;
import com.mojang.logging.LogUtils;
import fiskfille.utils.helper.NBTHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Lightsabers.MODID)
public final class Lightsabers {
    public static final String NAME = "Advanced Lightsabers";
    public static final String MODID = "lightsabers";
    public static final String VERSION = "1.2.2";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final CommonProxy proxy = DistExecutor.unsafeRunForDist(
            () -> ClientProxy::new,
            () -> CommonProxy::new
    );

    public static Lightsabers instance;
    public static boolean isBattlegearLoaded;
    public static boolean isDynamicLightsLoaded;
    public static boolean isEpicFightLoaded;

    public Lightsabers() {
        instance = this;
        isBattlegearLoaded = false;
        isDynamicLightsLoaded = ModList.get().isLoaded(ALConstants.DYNAMIC_LIGHTS);
        isEpicFightLoaded = ModList.get().isLoaded(ALConstants.EPIC_FIGHT);

        HiltManager.register();
        Effect.register();
        ALData.init();
        NBTHelper.registerAdapter(LightsaberData.class, LightsaberData.Adapter.class);
        NBTHelper.registerAdapter(Power.class, Power.Adapter.class);
        NBTHelper.registerAdapter(PowerData.class, PowerData.Adapter.class);
        NBTHelper.registerAdapter(PowerData.Container.class, PowerData.Container.Adapter.class);
        ALNetworkManager.register();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModMenus.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModSounds.register(modEventBus);
        ModWorldgen.register(modEventBus);
        if (isEpicFightLoaded) {
            EpicFightIntegration.register(modEventBus);
        }
        modEventBus.addListener(ModDataGenerators::gatherData);
        ModLoadingContext.get().registerConfig(Type.CLIENT, ModConfig.SPEC);
        proxy.registerModEvents(modEventBus);
        MinecraftForge.EVENT_BUS.register(new ALEntityData.EntityCapabilityEvents());
        MinecraftForge.EVENT_BUS.register(new ALPlayerData.PlayerCapabilityEvents());
        MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
        MinecraftForge.EVENT_BUS.register(new CommandForce());
        if (isDynamicLightsLoaded) {
            MinecraftForge.EVENT_BUS.register(new CommonEventHandlerDL());
        }
        MinecraftForge.EVENT_BUS.register(proxy);
    }
}
