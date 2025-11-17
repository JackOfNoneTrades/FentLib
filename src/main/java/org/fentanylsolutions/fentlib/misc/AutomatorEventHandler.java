package org.fentanylsolutions.fentlib.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.GuiOpenEvent;

import org.fentanylsolutions.fentlib.FentLib;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AutomatorEventHandler {

    private static boolean clientTriggered = false;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onGuiOpen(GuiOpenEvent event) {
        if ((event.gui instanceof GuiMainMenu) && !clientTriggered) {
            clientTriggered = true;
            FentLib.LOG.warn("Minecraft Loaded");
            WorldSettings settings = new WorldSettings(
                System.currentTimeMillis(),
                WorldSettings.GameType.CREATIVE,
                true, // structures
                false, // hardcore
                WorldType.FLAT);
            settings.enableCommands();

            // Minecraft.getMinecraft().displayGuiScreen(null);
            Minecraft.getMinecraft().loadingScreen = new net.minecraft.client.LoadingScreenRenderer(
                Minecraft.getMinecraft());
            Minecraft.getMinecraft()
                .launchIntegratedServer("AutoWorld", "AutoWorld", settings);
        }
    }

    /*
     * @SubscribeEvent
     * public void onClientTick(TickEvent.ClientTickEvent e) {
     * Minecraft mc = Minecraft.getMinecraft();
     * if (!clientTriggered && mc.theWorld == null && mc.currentScreen == null) {
     * clientTriggered = true;
     * System.out.println("SNEEDFEED");
     * WorldSettings settings = new WorldSettings(
     * System.currentTimeMillis(),
     * WorldSettings.GameType.CREATIVE,
     * true, // structures
     * false, // hardcore
     * WorldType.FLAT
     * );
     * settings.enableCommands();
     * mc.launchIntegratedServer("AutoWorld", "AutoWorld", settings);
     * }
     * }
     */
}
