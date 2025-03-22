package com.spectrasonic.dispara_al_fantasma.listeners;

import com.spectrasonic.dispara_al_fantasma.Utils.MessageUtils;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitListener implements Listener {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball) {
            if (event.getHitEntity() instanceof Bat) {
                Bat bat = (Bat) event.getHitEntity();
                bat.remove();
                if (event.getEntity().getShooter() instanceof Player) {
                    Player shooter = (Player) event.getEntity().getShooter();
                    MessageUtils.sendActionBar(shooter, "<green>¡Has puntuado!</green>");
                }
            }
        }
    }
}