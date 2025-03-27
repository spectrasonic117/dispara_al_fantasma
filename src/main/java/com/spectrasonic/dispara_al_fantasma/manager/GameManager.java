package com.spectrasonic.dispara_al_fantasma.manager;

import com.spectrasonic.dispara_al_fantasma.Main;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class GameManager {

    private boolean active;
    private final List<Entity> spawnedGhostEntities = new ArrayList<>();
    private static GameManager instance;

    // Config values
    private int goodGhostCount;
    private int evilGhostCount;
    private int snowballAmount;
    private String goodGhostModelId;
    private String evilGhostModelId;
    private World spawnWorld;

    // PDC Key
    public static final NamespacedKey GHOST_TYPE_KEY = new NamespacedKey("dispara_al_fantasma", "ghost_type");

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    // Carga los valores desde config.yml
    public void loadConfigValues(JavaPlugin plugin) {
        plugin.reloadConfig(); // Asegura leer la última versión

        goodGhostCount = plugin.getConfig().getInt("ghost_counts.good_ghost", 50);
        evilGhostCount = plugin.getConfig().getInt("ghost_counts.evil_ghost", 50);
        snowballAmount = plugin.getConfig().getInt("snowball_inventory", 999);

        goodGhostModelId = plugin.getConfig().getString("model_names.good_ghost", "good_ghost");
        evilGhostModelId = plugin.getConfig().getString("model_names.evil_ghost", "evil_ghost");

        String worldName = plugin.getConfig().getString("spawn_world");
        if (worldName != null) {
            spawnWorld = Bukkit.getWorld(worldName);
        }
        if (spawnWorld == null) {
            spawnWorld = Bukkit.getWorlds().get(0); // Fallback al primer mundo
            plugin.getLogger().warning(
                    "No se especificó o no se encontró el mundo 'spawn_world' en config.yml. Usando el primer mundo disponible: "
                            + spawnWorld.getName());
        }
        if (spawnWorld == null) {
            plugin.getLogger().severe("No se pudo encontrar ningún mundo para spawnear entidades!");
        }

        plugin.getLogger().info("Configuración de GameManager cargada.");
    }

    public void startGame(JavaPlugin plugin) {
        if (active)
            return;
        if (!((Main) plugin).isModelEngineEnabled()) {
            plugin.getLogger().severe("No se puede iniciar el juego, Model Engine no está habilitado.");
            return;
        }
        active = true;
        spawnMobs(plugin);
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.getInventory().addItem(new ItemStack(Material.SNOWBALL, snowballAmount));
        });
    }

    public void stopGame(JavaPlugin plugin) {
        if (!active)
            return;
        active = false;

        spawnedGhostEntities.removeIf(entity -> {
            if (entity != null && !entity.isDead()) {
                entity.remove(); // Elimina la entidad Bukkit
                return true; // Indica que se eliminó de la lista
            }
            return entity == null || entity.isDead(); // Elimina nulos o ya muertos de la lista
        });

        if (spawnWorld != null) {
            spawnWorld.getEntitiesByClass(Bat.class).forEach(bat -> {
                if (bat.getPersistentDataContainer().has(GHOST_TYPE_KEY, PersistentDataType.STRING)) {
                    if (!bat.isDead()) {
                        bat.remove();
                    }
                }
            });
        }
        plugin.getLogger().info("Juego detenido y entidades limpiadas.");
    }

    public void spawnMobs(JavaPlugin plugin) {
        if (!active || spawnWorld == null || !((Main) plugin).isModelEngineEnabled())
            return;

        spawnGhostsOfType(plugin, goodGhostCount, goodGhostModelId, "good");
        spawnGhostsOfType(plugin, evilGhostCount, evilGhostModelId, "evil");
    }

    private void spawnGhostsOfType(JavaPlugin plugin, int count, String modelId, String ghostType) {
        if (modelId == null || modelId.isEmpty()) {
            plugin.getLogger().warning("El ID del modelo para '" + ghostType + "' no está configurado o está vacío.");
            return;
        }

        int spawnedCount = 0;
        for (int i = 0; i < count; i++) {
            Location loc = getRandomLocation();
            if (loc == null) {
                plugin.getLogger().warning("No se pudo obtener una ubicación aleatoria para spawnear.");
                continue;
            }

            if (!loc.isWorldLoaded() || !loc.getChunk().isLoaded()) {
                loc.getChunk().load();
            }

            Bat bat = spawnWorld.spawn(loc, Bat.class, entity -> {
                entity.setSilent(true);
                entity.setAI(true);
                entity.setRemoveWhenFarAway(false);
                entity.setPersistent(true);
                entity.setInvulnerable(true);
                entity.getPersistentDataContainer().set(GHOST_TYPE_KEY, PersistentDataType.STRING, ghostType);
            });

            try {
                if (!((Main) plugin).isModelEngineEnabled()) {
                    plugin.getLogger()
                            .severe("Model Engine no está habilitado al intentar aplicar modelo. Abortando spawn.");
                    bat.remove();
                    return;
                }

                ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(bat);
                if (modeledEntity == null) {
                    plugin.getLogger().warning("No se pudo crear ModeledEntity para el murciélago en " + loc);
                    bat.remove();
                    continue;
                }

                ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
                if (activeModel != null) {
                    modeledEntity.addModel(activeModel, true);
                    spawnedGhostEntities.add(bat);
                    spawnedCount++;
                } else {
                    plugin.getLogger()
                            .warning("No se pudo crear ActiveModel para el ID: " + modelId + ". ¿Existe el modelo?");
                    bat.remove();
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error al aplicar modelo '" + modelId + "' a la entidad: " + e.getMessage());
                e.printStackTrace();
                bat.remove();
            }
        }
        plugin.getLogger().info("Intentando spawnear " + count + " fantasmas de tipo: " + ghostType
                + ". Spawneados exitosamente: " + spawnedCount);
    }

    private Location getRandomLocation() {
        if (spawnWorld == null) {
            System.out.println("spawnWorld es null");
            return null;
        }

        double minX = -100; // Example values, adjust as needed
        double maxX = 100;
        double minY = 50;
        double maxY = 100;
        double minZ = -100;
        double maxZ = 100;

        double x = ThreadLocalRandom.current().nextDouble(minX, maxX);
        double y = ThreadLocalRandom.current().nextDouble(minY, maxY);
        double z = ThreadLocalRandom.current().nextDouble(minZ, maxZ);

        return new Location(spawnWorld, x, y, z);
    }

    private Main plugin;

    public void setPlugin(Main plugin) {
        this.plugin = plugin;
    }
}