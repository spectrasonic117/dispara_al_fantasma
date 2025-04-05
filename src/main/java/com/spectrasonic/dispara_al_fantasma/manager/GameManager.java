package com.spectrasonic.dispara_al_fantasma.manager;

import com.spectrasonic.dispara_al_fantasma.Main;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.spectrasonic.dispara_al_fantasma.Utils.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
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
    private double spawnX1, spawnY1, spawnZ1;
    private double spawnX2, spawnY2, spawnZ2;
    private int goodGhostCount;
    private int evilGhostCount;
    private int snowballAmount;
    private String goodGhostModelId;
    private String evilGhostModelId;
    private World spawnWorld;

    // PDC Key
    public static final NamespacedKey GHOST_TYPE_KEY = new NamespacedKey("dispara_al_fantasma", "ghost_type");

    private Main plugin;

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    // Carga los valores desde config.yml
    public void loadConfigValues(JavaPlugin plugin) {
        plugin.reloadConfig(); // Asegura leer la última versión

        // Cargar coordenadas de spawn
        ConfigurationSection spawnSection = plugin.getConfig().getConfigurationSection("spawn_mobs");
        if (spawnSection != null) {
            spawnX1 = spawnSection.getDouble("pos1.x", -100);
            spawnY1 = spawnSection.getDouble("pos1.y", 70);
            spawnZ1 = spawnSection.getDouble("pos1.z", -100);
            spawnX2 = spawnSection.getDouble("pos2.x", 100);
            spawnY2 = spawnSection.getDouble("pos2.y", 100);
            spawnZ2 = spawnSection.getDouble("pos2.z", 100);

            plugin.getLogger().info("Coordenadas de spawn cargadas: (" +
                    spawnX1 + "," + spawnY1 + "," + spawnZ1 + ") a (" +
                    spawnX2 + "," + spawnY2 + "," + spawnZ2 + ")");
        } else {
            plugin.getLogger()
                    .warning("No se encontró la sección 'spawn_mobs' en config.yml. Usando valores predeterminados.");
            // Valores predeterminados si no hay configuración
            spawnX1 = -100;
            spawnY1 = 70;
            spawnZ1 = -100;
            spawnX2 = 100;
            spawnY2 = 100;
            spawnZ2 = 100;
        }

        // Cargar cantidades de fantasmas
        ConfigurationSection ghostCountsSection = plugin.getConfig().getConfigurationSection("ghost_counts");
        if (ghostCountsSection != null) {
            goodGhostCount = ghostCountsSection.getInt("good_ghost", 50);
            evilGhostCount = ghostCountsSection.getInt("evil_ghost", 50);
        } else {
            // Intentar leer de la raíz para compatibilidad con versiones anteriores
            goodGhostCount = plugin.getConfig().getInt("good_ghost_spawn", 50);
            evilGhostCount = plugin.getConfig().getInt("evil_ghost_spawn", 50);
        }

        plugin.getLogger().info("Cantidades de fantasmas: buenos=" + goodGhostCount + ", malos=" + evilGhostCount);

        // Cargar cantidad de bolas de nieve
        snowballAmount = plugin.getConfig().getInt("snowball_inventory", 999);

        // Cargar IDs de modelos
        ConfigurationSection modelNamesSection = plugin.getConfig().getConfigurationSection("model_names");
        if (modelNamesSection != null) {
            goodGhostModelId = modelNamesSection.getString("good_ghost", "good_ghost");
            evilGhostModelId = modelNamesSection.getString("evil_ghost", "evil_ghost");
        } else {
            goodGhostModelId = "good_ghost";
            evilGhostModelId = "evil_ghost";
        }

        plugin.getLogger().info("IDs de modelos: buenos=" + goodGhostModelId + ", malos=" + evilGhostModelId);

        // Cargar mundo de spawn
        String worldName = plugin.getConfig().getString("spawn_world");
        if (worldName != null) {
            spawnWorld = Bukkit.getWorld(worldName);
            if (spawnWorld != null) {
                plugin.getLogger().info("Usando mundo configurado: " + worldName);
            } else {
                plugin.getLogger().warning("El mundo configurado '" + worldName + "' no existe.");
            }
        }

        if (spawnWorld == null) {
            spawnWorld = Bukkit.getWorlds().get(0); // Fallback al primer mundo
            plugin.getLogger().warning("Usando el primer mundo disponible: " + spawnWorld.getName());
        }

        plugin.getLogger().info("Configuración de GameManager cargada correctamente.");
    }

    public void startGame(JavaPlugin plugin) {
        if (active) {
            plugin.getLogger().info("El juego ya está activo, ignorando llamada a startGame()");
            return;
        }

        if (!((Main) plugin).isModelEngineEnabled()) {
            plugin.getLogger().severe("No se puede iniciar el juego, Model Engine no está habilitado.");
            return;
        }

        active = true;
        plugin.getLogger().info("Iniciando juego, spawneando fantasmas...");

        // Limpiar entidades previas por si acaso
        clearAllGhosts();

        // Spawnear nuevos fantasmas
        spawnMobs(plugin);

        // Dar arcos y flechas solo a los jugadores en modo ADVENTURE
        Bukkit.getOnlinePlayers().forEach(player -> {
            // Verificar si el jugador está en modo ADVENTURE
            if (player.getGameMode() == GameMode.ADVENTURE) {
                ItemStack bow = ItemBuilder.setMaterial("BOW")
                        .setName("<gold>Arco Anti Fantasmas</gold>")
                        .setLore("<gray>Usa este arco para disparar a los fantasmas</gray>")
                        .addEnchantment("infinity", 1)
                        .setUnbreakable(true)
                        .setFlag("HIDE_ENCHANTS")
                        .build();

                ItemStack arrow = new ItemStack(Material.ARROW, 1);

                player.getInventory().addItem(bow, arrow);
            }
        });

        plugin.getLogger().info("Juego iniciado correctamente.");
    }

    public void stopGame(JavaPlugin plugin) {
        if (!active) {
            plugin.getLogger().info("El juego no está activo, ignorando llamada a stopGame()");
            return;
        }

        active = false;
        plugin.getLogger().info("Deteniendo juego, eliminando fantasmas...");

        clearAllGhosts();

        // Clear only ADVENTURE mode players' inventories
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getGameMode() == GameMode.ADVENTURE) {
                player.getInventory().clear();
            }
        });

        plugin.getLogger().info("Juego detenido correctamente.");
    }

    private void clearAllGhosts() {
        // Eliminar entidades rastreadas
        spawnedGhostEntities.removeIf(entity -> {
            if (entity != null && !entity.isDead()) {
                entity.remove();
                return true;
            }
            return entity == null || entity.isDead();
        });

        // Limpieza adicional por si acaso
        if (spawnWorld != null) {
            spawnWorld.getEntitiesByClass(Bat.class).forEach(bat -> {
                if (bat.getPersistentDataContainer().has(GHOST_TYPE_KEY, PersistentDataType.STRING)) {
                    bat.remove();
                }
            });
        }
    }

    public void spawnMobs(JavaPlugin plugin) {
        if (!active) {
            plugin.getLogger().warning("Intento de spawnear mobs mientras el juego no está activo");
            return;
        }

        if (spawnWorld == null) {
            plugin.getLogger().severe("No hay mundo de spawn configurado");
            return;
        }

        if (!((Main) plugin).isModelEngineEnabled()) {
            plugin.getLogger().severe("Model Engine no está habilitado, no se pueden spawnear mobs");
            return;
        }

        plugin.getLogger().info("Spawneando fantasmas...");

        // Spawnear fantasmas buenos
        int goodSpawned = spawnGhostsOfType(plugin, goodGhostCount, goodGhostModelId, "good");
        plugin.getLogger().info("Spawneados " + goodSpawned + "/" + goodGhostCount + " fantasmas buenos");

        // Spawnear fantasmas malos
        int evilSpawned = spawnGhostsOfType(plugin, evilGhostCount, evilGhostModelId, "evil");
        plugin.getLogger().info("Spawneados " + evilSpawned + "/" + evilGhostCount + " fantasmas malos");

        plugin.getLogger().info("Total de fantasmas spawneados: " + spawnedGhostEntities.size());
    }

    private int spawnGhostsOfType(JavaPlugin plugin, int count, String modelId, String ghostType) {
        if (modelId == null || modelId.isEmpty()) {
            plugin.getLogger().warning("El ID del modelo para '" + ghostType + "' no está configurado o está vacío.");
            return 0;
        }

        int spawnedCount = 0;
        for (int i = 0; i < count; i++) {
            Location loc = getRandomLocation();
            if (loc == null) {
                plugin.getLogger().warning("No se pudo obtener una ubicación aleatoria para spawnear.");
                continue;
            }

            // Asegurar que la chunk está cargada
            if (!loc.getChunk().isLoaded()) {
                loc.getChunk().load(true);
            }

            try {
                // Spawnear murciélago
                Bat bat = spawnWorld.spawn(loc, Bat.class, entity -> {
                    entity.setSilent(true);
                    entity.setAI(true);
                    entity.setRemoveWhenFarAway(false);
                    entity.setPersistent(true);
                    entity.setInvulnerable(true);
                    entity.setInvisible(true);
                    entity.getPersistentDataContainer().set(GHOST_TYPE_KEY, PersistentDataType.STRING, ghostType);
                });

                // Aplicar modelo usando ModelEngine API
                ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(bat);
                if (modeledEntity == null) {
                    plugin.getLogger().warning("No se pudo crear ModeledEntity para el murciélago en " + loc);
                    bat.remove();
                    continue;
                }

                ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
                if (activeModel == null) {
                    plugin.getLogger()
                            .warning("No se pudo crear ActiveModel para el ID: " + modelId + ". ¿Existe el modelo?");
                    bat.remove();
                    continue;
                }

                // Añadir modelo a la entidad
                modeledEntity.addModel(activeModel, true);

                // Registrar entidad para limpieza posterior
                spawnedGhostEntities.add(bat);
                spawnedCount++;

            } catch (Exception e) {
                plugin.getLogger().severe("Error al spawnear fantasma: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return spawnedCount;
    }

    private Location getRandomLocation() {
        if (spawnWorld == null) {
            if (plugin != null) {
                plugin.getLogger().severe("spawnWorld es null, no se puede generar ubicación aleatoria");
            }
            return null;
        }

        // Asegurar que min <= max
        double minX = Math.min(spawnX1, spawnX2);
        double maxX = Math.max(spawnX1, spawnX2);
        double minY = Math.min(spawnY1, spawnY2);
        double maxY = Math.max(spawnY1, spawnY2);
        double minZ = Math.min(spawnZ1, spawnZ2);
        double maxZ = Math.max(spawnZ1, spawnZ2);

        // Generar coordenadas aleatorias dentro del área definida
        double x = ThreadLocalRandom.current().nextDouble(minX, maxX);
        double y = ThreadLocalRandom.current().nextDouble(minY, maxY);
        double z = ThreadLocalRandom.current().nextDouble(minZ, maxZ);

        return new Location(spawnWorld, x, y, z);
    }

    public void setPlugin(Main plugin) {
        this.plugin = plugin;
    }

    public boolean spawnSingleGhost(JavaPlugin plugin, String ghostType) {
        if (!active) {
            plugin.getLogger().warning("Intento de spawnear un fantasma mientras el juego no está activo");
            return false;
        }

        if (spawnWorld == null) {
            plugin.getLogger().severe("No hay mundo de spawn configurado");
            return false;
        }

        if (!((Main) plugin).isModelEngineEnabled()) {
            plugin.getLogger().severe("Model Engine no está habilitado, no se puede spawnear el fantasma");
            return false;
        }

        String modelId;
        if ("good".equals(ghostType)) {
            modelId = goodGhostModelId;
        } else if ("evil".equals(ghostType)) {
            modelId = evilGhostModelId;
        } else {
            plugin.getLogger().warning("Tipo de fantasma desconocido: " + ghostType);
            return false;
        }

        // Usar el método existente para spawnear un solo fantasma
        int spawned = spawnGhostsOfType(plugin, 1, modelId, ghostType);
        return spawned > 0;
    }
}
