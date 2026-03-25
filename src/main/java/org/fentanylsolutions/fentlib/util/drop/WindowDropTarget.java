package org.fentanylsolutions.fentlib.util.drop;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import org.fentanylsolutions.fentlib.FentLib;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDL_DropEvent;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_EventFilterI;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

/**
 * Generic SDL3 drag-and-drop handler for the Minecraft window.
 * <p>
 * Installs an SDL event watch on the first client tick and dispatches
 * events to all registered {@link DropListener}s. Requires lwjgl3ify.
 * <p>
 * Usage:
 *
 * <pre>
 * WindowDropTarget.register();
 * WindowDropTarget.addListener(myListener);
 * </pre>
 */
@Lwjgl3Aware
@SideOnly(Side.CLIENT)
public final class WindowDropTarget {

    private static final WindowDropTarget INSTANCE = new WindowDropTarget();
    private static volatile boolean registered;
    private boolean watchInstalled;

    /** Strong reference to prevent GC while SDL holds the native pointer. */
    private SDL_EventFilterI eventWatch;

    private static final List<DropListener> LISTENERS = new CopyOnWriteArrayList<>();

    // Per-drag session state (written from SDL thread, read on main thread)
    private volatile String pendingDropText;
    private volatile String pendingDropFile;
    private volatile float lastDropX;
    private volatile float lastDropY;

    private WindowDropTarget() {}

    public static synchronized void register() {
        if (registered) return;
        if (!Loader.isModLoaded("lwjgl3ify")) {
            FentLib.LOG.info("[WindowDropTarget] lwjgl3ify not present, drag-and-drop support disabled");
            return;
        }
        FMLCommonHandler.instance()
            .bus()
            .register(INSTANCE);
        registered = true;
    }

    public static void addListener(DropListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void removeListener(DropListener listener) {
        LISTENERS.remove(listener);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!watchInstalled) {
            installEventWatch();
        }
    }

    private void installEventWatch() {
        eventWatch = (userdata, eventPtr) -> {
            SDL_Event sdlEvent = SDL_Event.create(eventPtr);
            int type = sdlEvent.type();

            if (type == SDLEvents.SDL_EVENT_DROP_BEGIN) {
                pendingDropText = null;
                pendingDropFile = null;
                scheduleOnMainThread(() -> {
                    for (DropListener l : LISTENERS) {
                        try {
                            l.onDragBegin();
                        } catch (Throwable t) {
                            FentLib.LOG.error("[WindowDropTarget] Listener onDragBegin failed", t);
                        }
                    }
                });
            } else if (type == SDLEvents.SDL_EVENT_DROP_TEXT) {
                SDL_DropEvent drop = sdlEvent.drop();
                pendingDropText = drop.dataString();
                String text = pendingDropText;
                for (DropListener l : LISTENERS) {
                    try {
                        l.onDropText(text);
                    } catch (Throwable t) {
                        FentLib.LOG.error("[WindowDropTarget] Listener onDropText failed", t);
                    }
                }
            } else if (type == SDLEvents.SDL_EVENT_DROP_FILE) {
                SDL_DropEvent drop = sdlEvent.drop();
                pendingDropFile = drop.dataString();
                lastDropX = drop.x();
                lastDropY = drop.y();
                String file = pendingDropFile;
                float x = lastDropX;
                float y = lastDropY;
                for (DropListener l : LISTENERS) {
                    try {
                        l.onDropFile(file, x, y);
                    } catch (Throwable t) {
                        FentLib.LOG.error("[WindowDropTarget] Listener onDropFile failed", t);
                    }
                }
            } else if (type == SDLEvents.SDL_EVENT_DROP_POSITION) {
                SDL_DropEvent drop = sdlEvent.drop();
                lastDropX = drop.x();
                lastDropY = drop.y();
                float x = lastDropX;
                float y = lastDropY;
                for (DropListener l : LISTENERS) {
                    try {
                        l.onDragPosition(x, y);
                    } catch (Throwable t) {
                        FentLib.LOG.error("[WindowDropTarget] Listener onDragPosition failed", t);
                    }
                }
            } else if (type == SDLEvents.SDL_EVENT_DROP_COMPLETE) {
                String text = pendingDropText;
                String file = pendingDropFile;
                float x = lastDropX;
                float y = lastDropY;
                pendingDropText = null;
                pendingDropFile = null;
                DropResult result = new DropResult(text, file, x, y);
                scheduleOnMainThread(() -> {
                    for (DropListener l : LISTENERS) {
                        try {
                            l.onDragComplete(result);
                        } catch (Throwable t) {
                            FentLib.LOG.error("[WindowDropTarget] Listener onDragComplete failed", t);
                        }
                    }
                });
            }

            return true;
        };

        SDLEvents.SDL_AddEventWatch(eventWatch, 0L);
        watchInstalled = true;
        FentLib.LOG.info("[WindowDropTarget] SDL drop event watch installed");
    }

    // --- Coordinate conversion ---

    /**
     * Converts SDL logical-point coordinates to Minecraft GUI-scaled coordinates.
     *
     * @param sx SDL X in logical points
     * @param sy SDL Y in logical points
     * @return float array {guiX, guiY}
     */
    public static float[] sdlToGuiCoords(float sx, float sy) {
        float pixelScale = org.lwjgl.opengl.Display.getPixelScaleFactor();
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        float fbX = sx * pixelScale;
        float fbY = sy * pixelScale;
        float guiX = fbX / sr.getScaleFactor();
        float guiY = fbY / sr.getScaleFactor();
        return new float[] { guiX, guiY };
    }

    private static void scheduleOnMainThread(Runnable action) {
        Minecraft.getMinecraft()
            .func_152344_a(action);
    }

    // --- Drop result ---

    public static final class DropResult {

        private final String text;
        private final String filePath;
        private final float sdlX;
        private final float sdlY;

        DropResult(String text, String filePath, float sdlX, float sdlY) {
            this.text = text;
            this.filePath = filePath;
            this.sdlX = sdlX;
            this.sdlY = sdlY;
        }

        /** The dropped text, or null if this was a file drop. */
        public String getText() {
            return text;
        }

        /** The dropped file path, or null if this was a text drop. */
        public String getFilePath() {
            return filePath;
        }

        /** SDL X coordinate at drop time, in logical points. */
        public float getSdlX() {
            return sdlX;
        }

        /** SDL Y coordinate at drop time, in logical points. */
        public float getSdlY() {
            return sdlY;
        }

        public boolean isTextDrop() {
            return text != null;
        }

        public boolean isFileDrop() {
            return filePath != null;
        }
    }
}
