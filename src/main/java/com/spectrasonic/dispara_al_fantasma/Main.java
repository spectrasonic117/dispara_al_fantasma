package com.spectrasonic.dispara_al_fantasma;

import co.aikar.commands.PaperCommandManager;
import com.spectrasonic.dispara_al_fantasma.commands.DafCommand;
import com.spectrasonic.dispara_al_fantasma.listeners.ProjectileHitListener;
import com.spectrasonic.dispara_al_fantasma.Utils.MessageUtils;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Main extends JavaPlugin {

    private static Main instance;
    private PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        registerCommands();
        registerEvents();
        MessageUtils.sendStartupMessage(this);
    }

    @Override
    public void onDisable() {
        MessageUtils.sendShutdownMessage(this);
    }

    public static Main getInstance() {
        return instance;
    }

    public void registerCommands() {
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new DafCommand());
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(), this);
    }
}
