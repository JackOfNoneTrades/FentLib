package org.fentanylsolutions.fentlib.core;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DepLoaderTerminalProgress {

    private static final Logger LOG = LogManager.getLogger("DepLoader");
    private static final PrintStream TERMINAL = new PrintStream(new FileOutputStream(FileDescriptor.out), true);
    private static final String PREFIX = "[DepLoader] ";
    private static final int BAR_WIDTH = 12;
    private static final int MAX_ARTIFACT_WIDTH = 24;
    private static final int SIZE_WIDTH = 9;
    private static final int MILESTONE_STEP = 25;
    private static volatile TaskFields taskFields;

    private DepLoaderTerminalProgress() {}

    public static Thread createThread(AtomicBoolean active, Map<String, ?> tasks, JFrame frame) {
        Thread thread = new Thread(() -> renderProgress(active, tasks, frame));
        thread.setDaemon(true);
        thread.setName("FalsePatternLib Terminal Download Progress");
        return thread;
    }

    private static void renderProgress(AtomicBoolean active, Map<String, ?> tasks, JFrame frame) {
        boolean interactive = System.console() != null;
        Map<String, Integer> renderedMilestones = new HashMap<>();
        InteractiveState interactiveState = new InteractiveState();
        try {
            while (active.get()) {
                if (interactive) {
                    renderInteractive(tasks, interactiveState);
                } else {
                    renderMilestones(tasks, renderedMilestones);
                }
                try {
                    Thread.sleep(75L);
                } catch (InterruptedException ignored) {
                    Thread.currentThread()
                        .interrupt();
                    break;
                }
            }

            if (interactive) {
                renderInteractive(tasks, interactiveState);
                finishInteractiveLine(interactiveState);
            } else {
                renderMilestones(tasks, renderedMilestones);
            }
            writeTerminalLine(PREFIX + "Complete");
        } catch (ReflectiveOperationException | RuntimeException e) {
            finishInteractiveLine(interactiveState);
            LOG.warn("Could not display FalsePattern DepLoader progress in the terminal.", e);
        } finally {
            if (frame != null) {
                frame.dispose();
            }
        }
    }

    public static void printNotice() {
        writeTerminalLine(PREFIX + "Progress hijacked by FentLib; disable it under Misc Tweaks in the config.");
    }

    public static boolean shouldCreateThread(JFrame frame, boolean nestedLoad) {
        return frame != null || !nestedLoad;
    }

    private static void renderMilestones(Map<String, ?> tasks, Map<String, Integer> renderedMilestones)
        throws ReflectiveOperationException {
        for (Map.Entry<String, ?> entry : tasks.entrySet()) {
            TaskSnapshot snapshot = takeSnapshot(entry);
            if (snapshot.state == 0) {
                continue;
            }

            int milestone = snapshot.percent >= 100 ? 100 : snapshot.percent / MILESTONE_STEP * MILESTONE_STEP;
            Integer previousMilestone = renderedMilestones.get(entry.getKey());
            if (previousMilestone != null && milestone <= previousMilestone) {
                continue;
            }
            renderedMilestones.put(entry.getKey(), milestone);
            writeTerminalLine(formatProgress(snapshot));
        }
    }

    private static void renderInteractive(Map<String, ?> tasks, InteractiveState interactiveState)
        throws ReflectiveOperationException {
        TaskSnapshot activeSnapshot = null;
        for (Map.Entry<String, ?> entry : tasks.entrySet()) {
            TaskSnapshot snapshot = takeSnapshot(entry);
            if (snapshot.state == 1) {
                if (activeSnapshot == null) {
                    activeSnapshot = snapshot;
                }
            } else if (snapshot.state != 0 && interactiveState.completedArtifacts.add(snapshot.artifact)) {
                writeInteractiveProgress(formatProgress(snapshot), true);
                interactiveState.lineOpen = false;
            }
        }

        if (activeSnapshot != null) {
            writeInteractiveProgress(formatProgress(activeSnapshot), false);
            interactiveState.lineOpen = true;
        } else {
            finishInteractiveLine(interactiveState);
        }
    }

    private static TaskSnapshot takeSnapshot(Map.Entry<String, ?> entry) throws ReflectiveOperationException {
        Object task = entry.getValue();
        TaskFields fields = getTaskFields(task);
        int state = ((AtomicInteger) fields.state.get(task)).get();
        long contentLength = ((AtomicLong) fields.contentLength.get(task)).get();
        long downloadedBytes = Math.max(0L, ((AtomicLong) fields.downloaded.get(task)).get());
        long totalBytes = contentLength > 0L ? contentLength : state == 1 ? -1L : downloadedBytes;
        int percent = state == 0 ? 0
            : state == 1 && contentLength > 0L
                ? (int) Math.max(0L, Math.min(100L, downloadedBytes * 100L / contentLength))
                : state == 1 ? 0 : 100;
        return new TaskSnapshot(entry.getKey(), state, downloadedBytes, totalBytes, percent);
    }

    private static String formatProgress(TaskSnapshot snapshot) {
        return String.format(
            Locale.ROOT,
            "%s%s %3d%% %" + SIZE_WIDTH + "s/%" + SIZE_WIDTH + "s  %s",
            PREFIX,
            progressBar(snapshot.percent),
            snapshot.percent,
            formatFileSize(snapshot.downloadedBytes),
            snapshot.totalBytes >= 0L ? formatFileSize(snapshot.totalBytes) : "?",
            compactArtifact(snapshot.artifact));
    }

    private static TaskFields getTaskFields(Object task) throws ReflectiveOperationException {
        TaskFields fields = taskFields;
        if (fields != null && fields.taskClass == task.getClass()) {
            return fields;
        }

        synchronized (DepLoaderTerminalProgress.class) {
            fields = taskFields;
            if (fields == null || fields.taskClass != task.getClass()) {
                fields = new TaskFields(task.getClass());
                taskFields = fields;
            }
        }
        return fields;
    }

    private static String progressBar(int percent) {
        int filled = Math.max(0, Math.min(BAR_WIDTH, percent * BAR_WIDTH / 100));
        StringBuilder bar = new StringBuilder(BAR_WIDTH + 2);
        bar.append('[');
        for (int i = 0; i < BAR_WIDTH; i++) {
            bar.append(i < filled ? '\u2588' : '\u2591');
        }
        return bar.append(']')
            .toString();
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024L) {
            return bytes + " B";
        }
        String[] units = { "KiB", "MiB", "GiB", "TiB" };
        double value = bytes;
        int unit = -1;
        do {
            value /= 1024D;
            unit++;
        } while (value >= 1024D && unit < units.length - 1);
        return String.format(Locale.ROOT, "%.1f %s", value, units[unit]);
    }

    private static String compactArtifact(String coordinate) {
        int groupSeparator = coordinate.indexOf(':');
        String artifact = groupSeparator < 0 ? coordinate : coordinate.substring(groupSeparator + 1);
        int versionSeparator = artifact.indexOf(':');
        if (versionSeparator >= 0) {
            artifact = artifact.substring(0, versionSeparator);
        }
        if (artifact.length() <= MAX_ARTIFACT_WIDTH) {
            return artifact;
        }
        return artifact.substring(0, MAX_ARTIFACT_WIDTH - 1) + '\u2026';
    }

    private static void writeTerminalLine(String text) {
        synchronized (TERMINAL) {
            TERMINAL.println(text);
            TERMINAL.flush();
        }
    }

    private static void writeInteractiveProgress(String text, boolean complete) {
        synchronized (TERMINAL) {
            TERMINAL.print('\r');
            TERMINAL.print(text);
            if (complete) {
                TERMINAL.println();
            } else {
                TERMINAL.flush();
            }
        }
    }

    private static void finishInteractiveLine(InteractiveState state) {
        if (!state.lineOpen) {
            return;
        }
        synchronized (TERMINAL) {
            TERMINAL.println();
            TERMINAL.flush();
        }
        state.lineOpen = false;
    }

    private static final class TaskFields {

        private final Class<?> taskClass;
        private final Field state;
        private final Field contentLength;
        private final Field downloaded;

        private TaskFields(Class<?> taskClass) throws ReflectiveOperationException {
            this.taskClass = taskClass;
            state = accessibleField(taskClass, "dlState");
            contentLength = accessibleField(taskClass, "contentLength");
            downloaded = accessibleField(taskClass, "downloaded");
        }

        private static Field accessibleField(Class<?> taskClass, String name) throws ReflectiveOperationException {
            Field field = taskClass.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        }
    }

    private static final class TaskSnapshot {

        private final String artifact;
        private final int state;
        private final long downloadedBytes;
        private final long totalBytes;
        private final int percent;

        private TaskSnapshot(String artifact, int state, long downloadedBytes, long totalBytes, int percent) {
            this.artifact = artifact;
            this.state = state;
            this.downloadedBytes = downloadedBytes;
            this.totalBytes = totalBytes;
            this.percent = percent;
        }
    }

    private static final class InteractiveState {

        private final Set<String> completedArtifacts = new HashSet<>();
        private boolean lineOpen;
    }

}
