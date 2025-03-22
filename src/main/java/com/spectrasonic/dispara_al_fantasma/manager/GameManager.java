package com.spectrasonic.dispara_al_fantasma.manager;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Bat;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class GameManager {

    private boolean active;
    private final List<Entity> spawnedGhasts = new ArrayList<>();

    private static GameManager instance;
    private final List<Entity> spawnedEntities = new ArrayList<>();

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void startGame(JavaPlugin plugin) {
        active = true;
        spawnMobs(plugin);
        int snowballAmount = plugin.getConfig().getInt("snowball_inventory", 999);
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.getInventory()
                    .addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.SNOWBALL, snowballAmount));
        });
    }

    public void stopGame(JavaPlugin plugin) {
        active = false;
        for (Entity e : spawnedEntities) {
            if (e != null && !e.isDead()) {
                e.remove();
            }
        }
        spawnedEntities.clear();
    }

    public void spawnMobs(JavaPlugin plugin) {
        if (!active)
            return;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("spawn_mobs");
        if (section == null)
            return;
        double x1 = section.getConfigurationSection("pos1").getDouble("x");
        double y1 = section.getConfigurationSection("pos1").getDouble("y");
        double z1 = section.getConfigurationSection("pos1").getDouble("z");
        double x2 = section.getConfigurationSection("pos2").getDouble("x");
        double y2 = section.getConfigurationSection("pos2").getDouble("y");
        double z2 = section.getConfigurationSection("pos2").getDouble("z");
        World world = Bukkit.getWorlds().get(0);
        int spawnLimit = plugin.getConfig().getInt("spawn_limit", 100);

        for (int i = 0; i < spawnLimit; i++) {
            // Ensure proper min/max values for random generation
            double minX = Math.min(x1, x2);
            double maxX = Math.max(x1, x2);
            double minY = Math.min(y1, y2);
            double maxY = Math.max(y1, y2);
            double minZ = Math.min(z1, z2);
            double maxZ = Math.max(z1, z2);

            // Generate random coordinates within the defined region
            double x = ThreadLocalRandom.current().nextDouble(minX, maxX);
            double y = ThreadLocalRandom.current().nextDouble(minY, maxY);
            double z = ThreadLocalRandom.current().nextDouble(minZ, maxZ);

            Location loc = new Location(world, x, y, z);
            Bat bat = world.spawn(loc, Bat.class, entity -> {
                entity.setCustomName("Murciélago");
                entity.setCustomNameVisible(true);
                entity.setPersistent(true);

                // Keep AI enabled for natural movement
                // entity.setAI(true);

                // Make the bat not despawn
                entity.setRemoveWhenFarAway(false);
            });
            spawnedEntities.add(bat);
        }
    }
}
