package org.fentanylsolutions.fentlib;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import org.fentanylsolutions.fentlib.carbonextension.FentSuggestionRenderers;

import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.ISuggestionRenderer;
import carbonconfiglib.gui.config.RegistryElement;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import org.fentanylsolutions.fentlib.carbonextension.MobWrapper;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ISuggestionRenderer.Registry
            .register(MobWrapper.class, new FentSuggestionRenderers.MobSuggestionRenderer());
        DataType.registerType(
            EntityLivingBase.class,
            RegistryElement.createForType(MobWrapper.class, "net.minecraft.entity.monster.EntitySnowman"));
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        FentLib.varInstanceClient.postInitHook();
    }

    @Override
    public EntityPlayer getPlayerEntityFromContext(MessageContext ctx) {
        // Note that if you simply return 'Minecraft.getMinecraft().thePlayer',
        // your packets will not work because you will be getting a client
        // player even when you are on the server! Sounds absurd, but it's true.

        // Solution is to double-check side before returning the player:
        return (ctx.side.isClient() ? Minecraft.getMinecraft().thePlayer : super.getPlayerEntityFromContext(ctx));
    }
}
