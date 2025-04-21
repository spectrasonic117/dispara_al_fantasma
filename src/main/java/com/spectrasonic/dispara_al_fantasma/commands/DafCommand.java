package com.spectrasonic.dispara_al_fantasma.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.spectrasonic.dispara_al_fantasma.Main;
import com.spectrasonic.dispara_al_fantasma.Utils.MessageUtils;
import com.spectrasonic.dispara_al_fantasma.manager.GameManager;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;

@CommandAlias("daf")
@NoArgsConstructor
public class DafCommand extends BaseCommand {

    private final Main plugin = Main.getInstance();
    private final GameManager gameManager = GameManager.getInstance();

    @Subcommand("game")
    @CommandPermission("daf.admin")
    @CommandCompletion("start|stop")
    @Description("Inicia o detiene el minijuego Dispara al Fantasma.")
    public void onGame(CommandSender sender, @Name("action") String action) {
        Player player = (Player) sender;
        if (action.equalsIgnoreCase("start")) {

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
            gameManager.startGame(plugin);
            MessageUtils.sendMessage(sender, "<green>Juego iniciado. ¡Fantasmas han aparecido!</green>");
            MessageUtils.broadcastTitle("<aqua>¡Dispara al Fantasma!</aqua>", "", 1,
                    2, 1);

        } else if (action.equalsIgnoreCase("stop")) {
            if (!gameManager.isActive()) {

                MessageUtils.sendMessage(sender, "<red>El juego no está activo.</red>");
                return;
            }
            // In the onGame method, inside the stop action
            player.performCommand("id true");
            gameManager.stopGame(plugin);

            // Limpiar arcos y flechas solo de inventarios de jugadores en modo ADVENTURE
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                if (onlinePlayer.getGameMode() == GameMode.ADVENTURE) {
                    onlinePlayer.getInventory().clear();
                }
            });

            MessageUtils.sendMessage(sender, "<yellow>Juego detenido y entidades eliminadas.</yellow>");
            // MessageUtils.broadcastTitle("<yellow>Juego Terminado</yellow>","<white>Los fantasmas han desaparecido</white>", 1, 3, 1);

        } else {
            MessageUtils.sendMessage(sender,
                    "<red>Acción desconocida. Usa <white>start</white> o <white>stop</white>.</red>");
        }
    }

    @Subcommand("reload")
    @CommandPermission("daf.admin")
    @Description("Recarga la configuración del plugin.")
    public void onReload(CommandSender sender) {
        // Recargar config.yml de Bukkit
        plugin.reloadConfig();
        // Recargar valores en GameManager
        gameManager.loadConfigValues(plugin);
        MessageUtils.sendMessage(sender, "<green>Configuración recargada.</green>");
        // Nota: Esto no reinicia un juego activo ni respawnea mobs con la nueva config.
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

    // Comando de ayuda básico
    @Default
    @HelpCommand
    @Subcommand("help")
    @CommandPermission("daf.admin")
    @Description("Muestra la ayuda de comandos.")
    public void onHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "<gold>Comandos de Dispara al Fantasma:</gold>");
        MessageUtils.sendMessage(sender,
                "<gray>/daf game <start|stop></gray> <white>- Inicia o detiene el juego.</white>");
        MessageUtils.sendMessage(sender, "<gray>/daf reload</gray> <white>- Recarga la configuración.</white>");
        MessageUtils.sendMessage(sender,
                "<gray>/daf spawn</gray> <white>- Spawnea una nueva oleada de fantasmas.</white>");
    }
}
