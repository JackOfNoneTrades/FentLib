package org.fentanylsolutions.fentlib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;

import org.fentanylsolutions.fentlib.misc.AutomatorEventHandler;
import org.fentanylsolutions.fentlib.services.S00PacketServerInfoModifyService;
import org.fentanylsolutions.fentlib.varinstances.VarInstanceClient;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        AutomatorEventHandler.tailLog();

        FentLib.varInstanceClient = new VarInstanceClient();

        S00PacketServerInfoModifyService.registerDeserializeHandler((response, fentlibData, serverData) -> {
            if (fentlibData.has(FentLib.MODID)) {
                FentLib.debug("Fentlib detected for server @ " + serverData.serverIP);
            } else {
                FentLib.debug("Fentlib not detected for server @ " + serverData.serverIP);
            }
        });

        if (System.getenv("AUTOSTART_CLIENT") != null) {
            FentLib.debug("AUTOSTART_CLIENT env var is set, registering AutomatorEventHandler");
            AutomatorEventHandler lol = new AutomatorEventHandler();
            FMLCommonHandler.instance()
                .bus()
                .register(lol);
            MinecraftForge.EVENT_BUS.register(lol);
            Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
        }

        FentLib.varInstanceClient.preinitHook();
        cleanOrphanServerGifIcons();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        super.serverStarting(event);
    }

    private void cleanOrphanServerGifIcons() {
        try {
            NBTTagCompound nbttagcompound = CompressedStreamTools
                .read(new File(Minecraft.getMinecraft().mcDataDir, "servers.dat"));
            if (nbttagcompound == null) {
                return;
            }
            NBTTagList nbttaglist = nbttagcompound.getTagList("servers", 10);
            ArrayList<String> referencedHashes = new ArrayList<>();
            for (int i = 0; i < nbttaglist.tagCount(); i++) {
                NBTTagCompound nbtCompound = nbttaglist.getCompoundTagAt(i);
                String animatedIconHash = nbtCompound.getString("animatedIconHash");
                if (animatedIconHash.startsWith("gif:")) {
                    referencedHashes.add(animatedIconHash.substring(4));
                }
            }
            File[] iconFiles = FentLib.varInstanceClient.serverIconDir.listFiles();
            if (iconFiles == null) {
                return;
            }
            for (File f : iconFiles) {
                if (!referencedHashes.contains(f.getName())) {
                    f.delete();
                    FentLib.LOG.info("Cleaned up orphaned icon {}", f.getName());
                }
            }
        } catch (IOException e) {
            FentLib.LOG.info("servers.dat not found");
        }
    }
}
