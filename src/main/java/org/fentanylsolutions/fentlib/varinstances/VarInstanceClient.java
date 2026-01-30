package org.fentanylsolutions.fentlib.varinstances;

import java.io.File;
import java.util.concurrent.ExecutorService;

import net.minecraft.client.Minecraft;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.FentLibThreadPool;

public class VarInstanceClient {

    public ExecutorService gifloaderPool = FentLibThreadPool.createSingleThreadPool("FentLib-GifLoader");
    public Minecraft minecraftRef = Minecraft.getMinecraft();
    public File serverIconDir;

    public void preinitHook() {
        this.serverIconDir = new File(FentLib.fentlibDir, "server_icons");
        if (!this.serverIconDir.exists()) {
            this.serverIconDir.mkdirs();
        }
    }
}
