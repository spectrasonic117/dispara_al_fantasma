package com.spectrasonic.dispara_al_fantasma;

import com.spectrasonic.dispara_al_fantasma.Utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {

        registerCommands();
        registerEvents();
        MessageUtils.sendStartupMessage(this);

    }

    @Override
    public void onDisable() {
        MessageUtils.sendShutdownMessage(this);
    }

    public void registerCommands() {
        // Set Commands Here
    }

    public void registerEvents() {
        // Set Events Here
    }
}
