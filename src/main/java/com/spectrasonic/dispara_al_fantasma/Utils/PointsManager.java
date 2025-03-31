package com.spectrasonic.dispara_al_fantasma.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PointsManager {

    private final JavaPlugin plugin;

    public PointsManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void addPoints(Player player, int amount) {
        String playerName = player.getName();
        String command = "2dgamepoints add " + playerName + " " + amount;
        String screen_point = "playglow " + playerName + " green 0.1 1 0.1 80 50";
        executeCommand(command, screen_point);
    }

    public void subtractPoints(Player player, int amount) {
        String playerName = player.getName();
        String command = "2dgamepoints subtract " + playerName + " " + amount;
        String screen_point = "playglow " + playerName + " red 0.1 1 0.1 80 50";
        executeCommand(command, screen_point);
    }

    private void executeCommand(String command, String screen_point) {
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), screen_point));
    }
}