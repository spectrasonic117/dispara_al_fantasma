package com.spectrasonic.dispara_al_fantasma;

import co.aikar.commands.PaperCommandManager;
import com.spectrasonic.dispara_al_fantasma.commands.DafCommand;
import com.spectrasonic.dispara_al_fantasma.listeners.ProjectileHitListener;
import com.spectrasonic.dispara_al_fantasma.manager.GameManager;
import com.spectrasonic.dispara_al_fantasma.Utils.MessageUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Main extends JavaPlugin {

    private static Main instance;
    private PaperCommandManager commandManager;
    private boolean modelEngineEnabled = false;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        if (!checkModelEngine()) {
            // ModelEngine no está, deshabilitar o manejar como prefieras
            MessageUtils.sendConsoleMessage(
                    "<red>Model Engine no encontrado. El plugin no funcionará correctamente sin él.</red>");
            // Podrías deshabilitar el plugin aquí si es estrictamente necesario:
            // getServer().getPluginManager().disablePlugin(this);
            // return;
        } else {
            MessageUtils.sendConsoleMessage("<green>Model Engine encontrado y habilitado.</green>");
        }

        // Cargar configuración inicial del GameManager
        GameManager.getInstance().loadConfigValues(this);

        registerCommands();
        registerEvents();
        MessageUtils.sendStartupMessage(this);
    }

    @Override
    public void onDisable() {
        // Asegurarse de detener el juego si está activo
        if (GameManager.getInstance().isActive()) {
            GameManager.getInstance().stopGame(this);
        }
        MessageUtils.sendShutdownMessage(this);
    }

    public static Main getInstance() {
        return instance;
    }

    private boolean checkModelEngine() {
        if (Bukkit.getPluginManager().getPlugin("ModelEngine") != null
                && Bukkit.getPluginManager().isPluginEnabled("ModelEngine")) {
            modelEngineEnabled = true;
            return true;
        }
        modelEngineEnabled = false;
        return false;
    }

    public void registerCommands() {
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new DafCommand());
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(), this);
    }
}
