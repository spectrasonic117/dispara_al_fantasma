package com.spectrasonic.dispara_al_fantasma.listeners;

import com.spectrasonic.dispara_al_fantasma.Main;
import com.spectrasonic.dispara_al_fantasma.manager.GameManager;
import com.spectrasonic.dispara_al_fantasma.Utils.PointsManager;
import com.spectrasonic.dispara_al_fantasma.Utils.MessageUtils;
import com.spectrasonic.dispara_al_fantasma.Utils.SoundUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ProjectileHitListener implements Listener {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        // Verificar que el proyectil sea una flecha y que haya sido disparado por un jugador
        if (!(projectile instanceof Arrow) || !(projectile.getShooter() instanceof Player)) {
             return;
        }

        // Cast to Arrow here, as we need it later
        Arrow arrow = (Arrow) projectile;
        GameManager gameManager = GameManager.getInstance(); // Get GameManager instance
        Main plugin = Main.getInstance(); // Get plugin instance
        Player shooter = (Player) arrow.getShooter(); // Get shooter from arrow

        // Verificar que el juego esté activo
        if (!gameManager.isActive()) {
            // Even if game isn't active, maybe remove arrows fired by players?
            // For now, we only remove arrows if the game IS active.
            // If you want to remove *any* player-fired arrow always, move the removal logic outside this block.
            return;
        }

        // --- Arrow Removal Logic ---
        // Remove the arrow entity shortly after impact.
        // Using a 1-tick delay can sometimes prevent issues.
        arrow.getServer().getScheduler().runTaskLater(plugin, arrow::remove, 1L);
        // Alternatively, remove immediately:
        // arrow.remove();
        // --------------------------

        // Verificar que golpeó a un murciélago / Fantasma
        if (!(event.getHitEntity() instanceof Bat)) {
            // Arrow removal is already scheduled, just exit if it didn't hit a Bat
            return;
        }

        Bat bat = (Bat) event.getHitEntity();
        PersistentDataContainer pdc = bat.getPersistentDataContainer();

        // Verificar si el murciélago tiene la etiqueta de tipo de fantasma
        if (pdc.has(GameManager.GHOST_TYPE_KEY, PersistentDataType.STRING)) {
            String ghostType = pdc.get(GameManager.GHOST_TYPE_KEY, PersistentDataType.STRING);
            int currentRound = gameManager.getCurrentRound(); // Get the current round
            int pointsChange = 0; // Points to add or subtract
            String messageColor = "<green>"; // Default message color

            PointsManager pointsManager = Main.getInstance().getPointsManager();

            // Determine points based on ghost type and round
            if ("good".equals(ghostType)) {
                // good ghost points decrease
                switch (currentRound) {
                    case 1:
                        pointsChange = -1; // Ronda 1: evil_ghost -1
                        messageColor = "<red>";
                        break;
                    case 2:
                        pointsChange = -3; // Ronda 2: evil_ghost -3
                        messageColor = "<red>";
                        break;
                    case 3:
                        pointsChange = -5; // Ronda 3: evil_ghost -5
                        messageColor = "<red>";
                        break;
                    default:
                        // Should not happen if game is active and round is set correctly
                        plugin.getLogger().warning("Projectile hit evil ghost with unknown round " + currentRound);
                        return; // Exit the method
                }
            } else if ("evil".equals(ghostType)) {
                // Evil ghost points increase
                 switch (currentRound) {
                    case 1:
                        pointsChange = 2; // Ronda 1: good_ghost +2
                        messageColor = "<green>";
                        break;
                    case 2:
                        pointsChange = 4; // Ronda 2: good_ghost +4
                        messageColor = "<green>";
                        break;
                    case 3:
                        pointsChange = 6; // Ronda 3: good_ghost +6
                        messageColor = "<green>";
                        break;
                    default:
                         // Should not happen
                        plugin.getLogger().warning("Projectile hit good ghost with unknown round " + currentRound);
                        return; // Exit the method
                }
            } else {
                // Unknown ghost type - log and ignore
                plugin.getLogger().warning("Projectile hit entity with unknown ghost type: " + ghostType + " at " + bat.getLocation());
                return; // Don't process if type is unknown, exit the method
            }

            // Add/Subtract points and provide feedback
            if (pointsChange > 0) {
                pointsManager.addPoints(shooter, pointsChange);
                 MessageUtils.sendActionBar(shooter, messageColor + "<b>+" + pointsChange + "</b>");
                 SoundUtils.playerSound(shooter, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            } else { // pointsChange is 0 or negative
                 pointsManager.subtractPoints(shooter, Math.abs(pointsChange)); // Use Math.abs to subtract correctly
                 MessageUtils.sendActionBar(shooter, messageColor + "<b>" + pointsChange + "</b>"); // pointsChange is already negative
                 SoundUtils.playerSound(shooter, Sound.ENTITY_BLAZE_HURT, 1.0f, 1.0f); // Sound for hitting bad ghost
            }

            // Remove the hit ghost entity
            bat.remove();

            // Attempt to spawn a new ghost to replace the one removed
            // We can decide if we want to replace any ghost hit or only specific types
            // For now, let's try to replace any hit ghost
            boolean spawned = gameManager.spawnSingleGhost(plugin, ghostType); // Try to spawn the same type

            if (!spawned) {
                plugin.getLogger().warning("Failed to spawn replacement ghost of type: " + ghostType);
            }
        }
        // No else needed here, arrow removal happens regardless of hitting a tagged bat
    }
}