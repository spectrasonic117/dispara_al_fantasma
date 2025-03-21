package com.spectrasonic.dispara_al_fantasma.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.spectrasonic.dispara_al_fantasma.Main;
import com.spectrasonic.dispara_al_fantasma.Utils.MessageUtils;
import com.spectrasonic.dispara_al_fantasma.manager.GameManager;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

@CommandAlias("daf")
@NoArgsConstructor
public class DafCommand extends BaseCommand {

    private final Main plugin = Main.getInstance();

    @Subcommand("game")
    @CommandCompletion("start|stop")
    public void onGame(CommandSender sender, @Name("action") String action) {
        if (action.equalsIgnoreCase("start")) {
            if (GameManager.getInstance().isActive()) {
                MessageUtils.sendMessage(sender, "<red>El juego ya está activo.</red>");
                return;
            }
            GameManager.getInstance().startGame(plugin);
            MessageUtils.sendMessage(sender, "<green>Juego iniciado.</green>");
        } else if (action.equalsIgnoreCase("stop")) {
            if (!GameManager.getInstance().isActive()) {
                MessageUtils.sendMessage(sender, "<red>El juego no está activo.</red>");
                return;
            }
            GameManager.getInstance().stopGame(plugin);
            // Clear all snowballs from player inventories
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.getInventory().all(Material.SNOWBALL).keySet()
                        .forEach(slot -> player.getInventory().setItem(slot, null));
                player.updateInventory();
            });

            // Kill any remaining ghasts in all worlds
            Bukkit.getWorlds().forEach(world -> {
                world.getEntitiesByClass(Ghast.class).forEach(Entity::remove);
            });

            MessageUtils.sendMessage(sender, "<yellow>Juego detenido y entidades eliminadas.</yellow>");
        } else {
            MessageUtils.sendMessage(sender, "<red>Acción desconocida. Usa start o stop.</red>");
        }
    }

    @Subcommand("reload")
    public void onReload(CommandSender sender) {
        plugin.reloadConfig();
        MessageUtils.sendMessage(sender, "<green>Configuración recargada.</green>");
    }

    @Subcommand("spawn")
    public void onSpawn(CommandSender sender) {
        if (!GameManager.getInstance().isActive()) {
            MessageUtils.sendMessage(sender, "<red>El juego no está activo. No se pueden spawnear mobs.</red>");
            return;
        }
        GameManager.getInstance().spawnMobs(plugin);
        MessageUtils.sendMessage(sender, "<green>Nuevos mobs han sido spawneados.</green>");
    }
}
