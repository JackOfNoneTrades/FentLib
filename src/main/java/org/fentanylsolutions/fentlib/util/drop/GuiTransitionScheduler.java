package org.fentanylsolutions.fentlib.util.drop;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.fentanylsolutions.fentlib.FentLib;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Schedules GUI transitions for the next client tick.
 * <p>
 * {@code Minecraft.func_152344_a} runs immediately when called on the client thread
 * in 1.7.10, so it cannot be used to "defer to next tick" from UI handlers.
 * This scheduler processes queued tasks at the end of the next client tick instead.
 */
@SideOnly(Side.CLIENT)
public final class GuiTransitionScheduler {

    private static final Queue<Runnable> QUEUE = new ConcurrentLinkedQueue<Runnable>();
    private static final GuiTransitionScheduler INSTANCE = new GuiTransitionScheduler();
    private static volatile boolean registered;

    private GuiTransitionScheduler() {}

    public static synchronized void register() {
        if (registered) return;
        FMLCommonHandler.instance()
            .bus()
            .register(INSTANCE);
        registered = true;
    }

    /** Schedule an action to run at the end of the next client tick. */
    public static void nextTick(Runnable action) {
        if (action == null) return;
        QUEUE.add(action);
    }

    /**
     * Close a ModularUI panel, then run an action after a two-tick delay.
     * The delay ensures the old screen is fully torn down before the new one opens.
     */
    public static void transition(com.cleanroommc.modularui.screen.ModularPanel currentPanel, Runnable openAction) {
        nextTick(() -> {
            if (currentPanel != null) {
                currentPanel.closeIfOpen();
            }
            nextTick(openAction);
        });
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Runnable action;
        int processed = 0;
        final int maxPerTick = 64;
        while (processed < maxPerTick && (action = QUEUE.poll()) != null) {
            try {
                action.run();
            } catch (Throwable t) {
                FentLib.LOG.error("Deferred GUI transition failed", t);
            }
            processed++;
        }
    }
}
