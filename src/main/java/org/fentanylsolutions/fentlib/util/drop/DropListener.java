package org.fentanylsolutions.fentlib.util.drop;

/**
 * Callback interface for window drag-and-drop events via SDL3.
 * Register with {@link WindowDropTarget#addListener(DropListener)}.
 * <p>
 * <b>Threading:</b> {@code onDragBegin} and {@code onDragComplete} are called on
 * the main client thread. All other methods are called from the SDL event thread
 * and must not touch Minecraft state directly.
 */
public interface DropListener {

    /** Called on the <b>main thread</b> when a drag enters the window. */
    default void onDragBegin() {}

    /**
     * Called on the <b>main thread</b> when the cursor moves during a drag.
     * Prefer this for UI updates. The legacy {@link #onDragPosition(float, float)}
     * callback remains SDL-threaded for compatibility.
     *
     * @param sdlX cursor X in SDL logical points
     * @param sdlY cursor Y in SDL logical points
     */
    default void onDragPositionMainThread(float sdlX, float sdlY) {}

    /**
     * Called from the <b>SDL thread</b> when the cursor moves during a drag.
     *
     * @param sdlX cursor X in SDL logical points
     * @param sdlY cursor Y in SDL logical points
     */
    default void onDragPosition(float sdlX, float sdlY) {}

    /**
     * Called from the <b>SDL thread</b> when text is dropped.
     *
     * @param text the dropped text content
     */
    default void onDropText(String text) {}

    /**
     * Called on the <b>main thread</b> when SDL has provided text payload for
     * the current drag/drop operation. Use this for payload-specific landing
     * zones. The legacy {@link #onDropText(String)} callback remains
     * SDL-threaded for compatibility.
     *
     * @param text the dropped text content
     */
    default void onDragText(String text) {}

    /**
     * Called from the <b>SDL thread</b> when a file is dropped.
     *
     * @param filePath absolute path to the dropped file
     * @param sdlX     cursor X in SDL logical points at drop time
     * @param sdlY     cursor Y in SDL logical points at drop time
     */
    default void onDropFile(String filePath, float sdlX, float sdlY) {}

    /**
     * Called on the <b>main thread</b> when SDL has provided file payload for
     * the current drag/drop operation. Prefer this for UI updates. The legacy
     * {@link #onDropFile(String, float, float)} callback remains SDL-threaded
     * for compatibility.
     *
     * @param filePath absolute path to the dropped file
     * @param sdlX     cursor X in SDL logical points at drop time
     * @param sdlY     cursor Y in SDL logical points at drop time
     */
    default void onDragFile(String filePath, float sdlX, float sdlY) {}

    /**
     * Called on the <b>main thread</b> when the drag-and-drop sequence completes.
     *
     * @param result summary of what was dropped
     */
    default void onDragComplete(WindowDropTarget.DropResult result) {}
}
