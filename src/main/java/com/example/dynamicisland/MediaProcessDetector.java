package com.example.dynamicisland;

import java.util.Optional;

public class MediaProcessDetector {

    public enum Player { NETEASE, SPOTIFY, NONE }

    private static Player currentPlayer = Player.NONE;
    private static long lastCheck = 0;
    private static final long CHECK_INTERVAL_MS = 2000;

    public static Player getCurrentPlayer() {
        long now = System.currentTimeMillis();
        if (now - lastCheck < CHECK_INTERVAL_MS) return currentPlayer;
        lastCheck = now;
        currentPlayer = detect();
        return currentPlayer;
    }

    private static boolean isAndroid() {
        String vm = System.getProperty("java.vm.name", "").toLowerCase();
        String rt = System.getProperty("java.runtime.name", "").toLowerCase();
        return vm.contains("dalvik") || rt.contains("android");
    }

    private static Player detect() {
        if (isAndroid()) {
            return detectAndroid();
        }
        return detectDesktop();
    }

    private static Player detectDesktop() {
        try {
            Optional<ProcessHandle> netease = ProcessHandle.allProcesses()
                    .filter(p -> p.info().command()
                            .map(c -> c.toLowerCase().contains("cloudmusic"))
                            .orElse(false))
                    .findFirst();
            if (netease.isPresent()) return Player.NETEASE;

            Optional<ProcessHandle> spotify = ProcessHandle.allProcesses()
                    .filter(p -> p.info().command()
                            .map(c -> c.toLowerCase().contains("spotify"))
                            .orElse(false))
                    .findFirst();
            if (spotify.isPresent()) return Player.SPOTIFY;
        } catch (Exception ignored) {
        }
        return Player.NONE;
    }

    private static Player detectAndroid() {
        // Android 沙箱限制，普通应用无法读取其他进程，返回 NONE。
        // 如设备已 root，可尝试读取 /proc。
        try {
            java.io.File proc = new java.io.File("/proc");
            java.io.File[] pids = proc.listFiles();
            if (pids == null) return Player.NONE;
            for (java.io.File pid : pids) {
                java.io.File cmd = new java.io.File(pid, "cmdline");
                if (!cmd.exists() || !cmd.canRead()) continue;
                try (java.io.BufferedReader r =
                             new java.io.BufferedReader(new java.io.FileReader(cmd))) {
                    String line = r.readLine();
                    if (line == null) continue;
                    String l = line.toLowerCase();
                    if (l.contains("cloudmusic")) return Player.NETEASE;
                    if (l.contains("spotify")) return Player.SPOTIFY;
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return Player.NONE;
    }
}
