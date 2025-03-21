package com.spectrasonic.dispara_al_fantasma.manager;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class GameManager {

    private boolean active;
    private final List<Entity> spawnedGhasts = new ArrayList<>();

    private static GameManager instance;

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
        for (Entity e : spawnedGhasts) {
            if (e != null && !e.isDead()) {
                e.remove();
            }
        }
        spawnedGhasts.clear();
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
            Ghast ghast = world.spawn(loc, Ghast.class, entity -> {
                entity.setCustomName("Mini Ghast");
                entity.setCustomNameVisible(true);

                // Set the ghast size to 1/4 of the original
                entity.setPersistent(true);

                // Apply size modification using attribute
                AttributeInstance sizeAttribute = entity.getAttribute(Attribute.GENERIC_SCALE);
                if (sizeAttribute != null) {
                    sizeAttribute.setBaseValue(0.25); // 1/4 of original size
                }

                // Make the ghast non-aggressive
                entity.setAware(false); // Prevents the ghast from targeting players

                // Remove targeting goals
                if (entity instanceof Mob) {
                    Mob mob = (Mob) entity;
                    mob.setTarget(null);
                }

                // Set AI flag to false to disable all AI
                entity.setAI(false);

                // But allow movement
                entity.setGravity(false); // Makes them float better
            });
            spawnedGhasts.add(ghast);
        }
    }
}
