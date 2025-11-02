package org.fentanylsolutions.fentlib.varinstances;

import java.util.concurrent.ExecutorService;

import net.minecraft.client.Minecraft;

import org.fentanylsolutions.fentlib.FentLibThreadPool;

public class VarInstanceClient {

    public ExecutorService gifloaderPool = FentLibThreadPool.createSingleThreadPool("FentLib-GifLoader");
    public Minecraft minecraftRef = Minecraft.getMinecraft();
}
