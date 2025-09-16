package org.fentanylsolutions.fentlib.varinstances;

import java.util.concurrent.ExecutorService;

import org.fentanylsolutions.fentlib.FentLibThreadPool;

public class VarInstanceClient {

    public ExecutorService gifloaderPool = FentLibThreadPool.createSingleThreadPool("FentLib-GifLoader");
}
