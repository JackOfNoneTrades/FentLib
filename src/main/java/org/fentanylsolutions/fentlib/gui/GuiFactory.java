package org.fentanylsolutions.fentlib.gui;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;

import org.fentanylsolutions.fentlib.Config;
import org.fentanylsolutions.fentlib.FentLib;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;

@SuppressWarnings("unused")
public class GuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {}

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    public static class ConfigGui extends GuiConfig {

        public ConfigGui(GuiScreen parentScreen) {
            super(
                parentScreen,
                ImmutableList.of(
                    new ConfigElement(
                        Config.getRawConfig()
                            .getCategory(Config.Categories.general)),
                    new ConfigElement(
                        Config.getRawConfig()
                            .getCategory(Config.Categories.debug)),
                    new ConfigElement(
                        Config.getRawConfig()
                            .getCategory(Config.Categories.miscTweaks))),
                FentLib.MODID,
                FentLib.MODID,
                false,
                false,
                I18n.format(FentLib.MODID + ".configgui.title"));
        }

        @Override
        public void initGui() {
            super.initGui();
            FentLib.debug("Initializing config gui");
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            // You can do things like create animations, draw additional elements, etc. here
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        protected void actionPerformed(GuiButton b) {
            FentLib.debug("Config button id " + b.id + " pressed");
            super.actionPerformed(b);
            /* "Done" button */
            if (b.id == 2000) {
                /* Syncing config */
                FentLib.debug("Saving config");
                Config.getRawConfig()
                    .save();
                Config.loadConfig(FentLib.confFile);
            }
        }
    }
}
