package com.spectrasonic.dispara_al_fantasma.listeners;

import com.spectrasonic.dispara_al_fantasma.Main;
import com.spectrasonic.dispara_al_fantasma.Utils.MessageUtils;
import com.spectrasonic.dispara_al_fantasma.Utils.SoundUtils;
import com.spectrasonic.dispara_al_fantasma.manager.GameManager;
import com.spectrasonic.dispara_al_fantasma.Utils.PointsManager;
import org.bukkit.Bukkit;
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

        // Verificar que sea una flecha
        if (!(projectile instanceof Arrow)) {
            return;
        }

        // Verificar que el juego esté activo y que el proyectil fue lanzado por un
        // jugador
        if (!GameManager.getInstance().isActive() || !(projectile.getShooter() instanceof Player)) {
            return;
        }

        // Verificar que golpeó a un murciélago / Fantasma
        if (!(event.getHitEntity() instanceof Bat)) {
            return;
        }

        Bat bat = (Bat) event.getHitEntity();
        Player shooter = (Player) projectile.getShooter();
        PersistentDataContainer pdc = bat.getPersistentDataContainer();

        // Verificar si el murciélago tiene la etiqueta de tipo de fantasma
        if (pdc.has(GameManager.GHOST_TYPE_KEY, PersistentDataType.STRING)) {
            String ghostType = pdc.get(GameManager.GHOST_TYPE_KEY, PersistentDataType.STRING);

            PointsManager pointsManager = Main.getInstance().getPointsManager();

            if ("evil".equals(ghostType)) {
                pointsManager.addPoints(shooter, 1);
                MessageUtils.sendActionBar(shooter, "<green><b>+1 Punto");
                SoundUtils.playerSound(shooter, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            } else if ("good".equals(ghostType)) {
                pointsManager.subtractPoints(shooter, 3);
                MessageUtils.sendActionBar(shooter, "<red><bold>-3 Puntos");
                SoundUtils.playerSound(shooter, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 1.0f);
            }

            // Eliminar el murciélago (y su modelo asociado)
            bat.remove();

            // Respawnear un nuevo fantasma del mismo tipo
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                GameManager.getInstance().spawnSingleGhost(Main.getInstance(), ghostType);
            });

            // Remover la flecha para que no golpee más cosas
            projectile.remove();
        }
    }
}
