package com.fiskmods.lightsabers;

import com.fiskmods.lightsabers.common.block.ModBlocks;
import com.fiskmods.lightsabers.common.config.ModConfig;
import com.fiskmods.lightsabers.common.command.CommandForce;
import com.fiskmods.lightsabers.common.container.ModMenus;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.ALEntityData;
import com.fiskmods.lightsabers.common.data.ALPlayerData;
import com.fiskmods.lightsabers.common.data.ModAttachments;
import com.fiskmods.lightsabers.common.data.generator.ModDataGenerators;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.entity.ModEntities;
import com.fiskmods.lightsabers.common.event.CommonEventHandlerDL;
import com.fiskmods.lightsabers.common.event.CommonEventHandler;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerData;
import com.fiskmods.lightsabers.common.hilt.HiltManager;
import com.fiskmods.lightsabers.common.generator.worldgen.ModWorldgen;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(Lightsabers.MODID)
public final class Lightsabers {
    public static final String NAME = "Advanced Lightsabers";
    public static final String MODID = "lightsabers";
    public static final String VERSION = "1.2.2";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static CommonProxy proxy;

    public static Lightsabers instance;
    public static boolean isBattlegearLoaded;
    public static boolean isDynamicLightsLoaded;
    public static boolean isEpicFightLoaded;
    public static boolean isLootrLoaded;

    public Lightsabers(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;
        proxy = FMLEnvironment.getDist().isClient() ? new ClientProxy() : new CommonProxy();
        isBattlegearLoaded = false;
        isDynamicLightsLoaded = ModList.get().isLoaded(ALConstants.DYNAMIC_LIGHTS);
        isEpicFightLoaded = ModList.get().isLoaded(ALConstants.EPIC_FIGHT);
        isLootrLoaded = ModList.get().isLoaded(ALConstants.LOOTR);

        HiltManager.register();
        Effect.register();
        ALData.init();
        NBTHelper.registerAdapter(LightsaberData.class, LightsaberData.Adapter.class);
        NBTHelper.registerAdapter(Power.class, Power.Adapter.class);
        NBTHelper.registerAdapter(PowerData.class, PowerData.Adapter.class);
        NBTHelper.registerAdapter(PowerData.Container.class, PowerData.Container.Adapter.class);
        ALNetworkManager.register(modEventBus);

        ModEntities.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModMenus.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModSounds.register(modEventBus);
        ModWorldgen.register(modEventBus);
        modEventBus.addListener(ModDataGenerators::gatherData);
        modContainer.registerConfig(Type.CLIENT, ModConfig.SPEC);
        proxy.registerModEvents(modEventBus);
        NeoForge.EVENT_BUS.register(new CommonEventHandler());
        NeoForge.EVENT_BUS.register(new CommandForce());
        if (isDynamicLightsLoaded) {
            NeoForge.EVENT_BUS.register(new CommonEventHandlerDL());
        }
    }
}
