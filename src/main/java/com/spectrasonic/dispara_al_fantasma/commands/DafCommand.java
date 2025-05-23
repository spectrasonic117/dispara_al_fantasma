package com.spectrasonic.dispara_al_fantasma.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.spectrasonic.dispara_al_fantasma.Main;
import com.spectrasonic.dispara_al_fantasma.Utils.MessageUtils;
import com.spectrasonic.dispara_al_fantasma.manager.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;

@CommandAlias("daf")
public class DafCommand extends BaseCommand {

    private final Main plugin;
    private final GameManager gameManager;

    public DafCommand(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    @Subcommand("game start")
    @CommandCompletion("1|2|3")
    @CommandPermission("daf.admin")
    @Description("Inicia el minijuego Dispara al Fantasma en la ronda especificada.")
    public void onGameStart(CommandSender sender, @Name("round") int round) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por un jugador.</red>");
            return;
        }
        Player player = (Player) sender;

        if (round < 1 || round > 3) {
            MessageUtils.sendMessage(sender, "<red>La ronda debe ser 1, 2 o 3.</red>");
            return;
        }
        if (gameManager.isActive()) {
            MessageUtils.sendMessage(sender, "<red>El juego ya está activo.</red>");
            return;
        }

        if (!plugin.isModelEngineEnabled()) {
            MessageUtils.sendMessage(sender,
                    "<red>Error: Model Engine no está habilitado. No se puede iniciar el juego.</red>");
            return;
        }

        player.performCommand("id false");
        gameManager.startGame(plugin, round);
        MessageUtils.sendMessage(sender, "<green>Juego iniciado. ¡Fantasmas han aparecido!</green>");
        MessageUtils.broadcastTitle("<aqua><bold>¡Dispara al Fantasma!</aqua>", "", 1,
                2, 1);
    }

    @Subcommand("game stop")
    @CommandPermission("daf.admin")
    @Description("Detiene el minijuego Dispara al Fantasma.")
    public void onGameStop(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por un jugador.</red>");
            return;
        }
        Player player = (Player) sender;

        if (!gameManager.isActive()) {
            MessageUtils.sendMessage(sender, "<red>El juego no está activo.</red>");
            return;
        }

        player.performCommand("id true");

        gameManager.stopGame(plugin);

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (onlinePlayer.getGameMode() == GameMode.ADVENTURE) {
                onlinePlayer.getInventory().clear();
            }
        });

        MessageUtils.sendMessage(sender, "<yellow>Juego detenido y entidades eliminadas.</yellow>");
        // MessageUtils.broadcastTitle("<yellow>Juego Terminado</yellow>", "<white>Los fantasmas han desaparecido</white>", 1, 3, 1);
    }

    @Subcommand("reload")
    @CommandPermission("daf.admin")
    @Description("Recarga la configuración del plugin.")
    public void onReload(CommandSender sender) {
        plugin.reloadConfig();
        gameManager.loadConfigValues(plugin);
        MessageUtils.sendMessage(sender, "<green>Configuración recargada.</green>");
        if (gameManager.isActive()) {
        MessageUtils.sendMessage(sender,
                    "<yellow>Advertencia: El juego está activo. Los cambios de spawn no se aplicarán hasta que reinicies el juego.</yellow>");
    }
}

    @Subcommand("spawn")
    @CommandPermission("daf.admin")
    @Description("Fuerza el spawn de una nueva oleada de fantasmas (si el juego está activo).")
    public void onSpawn(CommandSender sender) {
        if (!gameManager.isActive()) {
            MessageUtils.sendMessage(sender, "<red>El juego no está activo. No se pueden spawnear mobs.</red>");
            return;
        }
        if (!plugin.isModelEngineEnabled()) {
            MessageUtils.sendMessage(sender,
                    "<red>Error: Model Engine no está habilitado. No se pueden spawnear mobs.</red>");
            return;
        }
        gameManager.spawnMobs(plugin);
        MessageUtils.sendMessage(sender, "<green>Nuevos fantasmas han sido spawneados.</green>");
    }

    @Default
    @HelpCommand
    @Subcommand("help")
    @CommandPermission("daf.admin")
    @Description("Muestra la ayuda de comandos.")
    public void onHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "<gold>Comandos de Dispara al Fantasma:</gold>");
        MessageUtils.sendMessage(sender,
                "<gray>/daf game start <1|2|3></gray> <white>- Inicia el juego en la ronda especificada.</white>");
        MessageUtils.sendMessage(sender,
                "<gray>/daf game stop</gray> <white>- Detiene el juego.</white>");
        MessageUtils.sendMessage(sender, "<gray>/daf reload</gray> <white>- Recarga la configuración.</white>");
        MessageUtils.sendMessage(sender,
                "<gray>/daf spawn</gray> <white>- Spawnea una nueva oleada de fantasmas.</white>");
    }
}

