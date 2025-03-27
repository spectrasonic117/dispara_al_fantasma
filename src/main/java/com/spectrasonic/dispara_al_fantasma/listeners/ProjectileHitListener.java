package com.spectrasonic.dispara_al_fantasma.listeners;

import com.spectrasonic.dispara_al_fantasma.Utils.MessageUtils;
import com.spectrasonic.dispara_al_fantasma.manager.GameManager;
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
        // Solo procesar si el juego está activo y es una bola de nieve
        if (!GameManager.getInstance().isActive() || !(projectile.getShooter() instanceof Player)
                || !(event.getHitEntity() instanceof Bat)) {
            return;
        }

        Bat bat = (Bat) event.getHitEntity();
        Player shooter = (Player) projectile.getShooter();
        PersistentDataContainer pdc = bat.getPersistentDataContainer();

        // Verificar si el murciélago tiene la etiqueta de tipo de fantasma [citation:4]
        if (pdc.has(GameManager.GHOST_TYPE_KEY, PersistentDataType.STRING)) {
            String ghostType = pdc.get(GameManager.GHOST_TYPE_KEY, PersistentDataType.STRING);

            if ("good".equals(ghostType)) {
                MessageUtils.sendActionBar(shooter, "<green>¡Has Puntuado!</green>");
                // Aquí podrías añadir lógica de puntuación si la tienes
            } else if ("evil".equals(ghostType)) {
                MessageUtils.sendActionBar(shooter, "<red>Resta Punto</red>");
                // Aquí podrías añadir lógica de penalización si la tienes
            }

            // Eliminar el murciélago (y su modelo asociado)
            bat.remove();

            // Opcional: Remover la bola de nieve para que no golpee más cosas
            projectile.remove();
        }
        // Si no tiene la etiqueta, podría ser un murciélago normal, así que no hacemos
        // nada.
    }
}
