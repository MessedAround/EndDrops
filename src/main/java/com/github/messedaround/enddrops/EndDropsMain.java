package com.github.messedaround.enddrops;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("ALL")
public class EndDropsMain extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        checkConfig(this);
    }

    @Override
    public void onDisable() {}

    // Check if data folder and config.yml exist
    public static void checkConfig(final Plugin plugin) {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
                Bukkit.getLogger().info("[EndDropsMain] Could not find data folder. Created data folder.");
            }
            final File config = new File(plugin.getDataFolder(), "config.yml");
            if (!config.exists()) {
                plugin.saveDefaultConfig();
                Bukkit.getLogger().info("[EndDropsMain] Could not find config.yml. Created config.yml.");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Splitting up values from config
    public static String getConfigString(Plugin plugin, String string) {
        FileConfiguration config = plugin.getConfig();
        return config.getString(string).replace("'", "") + ",";
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event){
        Entity entity = event.getEntity();

        // Get config drop location coordinates
        String[] location = getConfigString(this, "drop_location").split(",");
        double x, y, z;
        x = Double.parseDouble(location[0]);
        y = Double.parseDouble(location[1]);
        z = Double.parseDouble(location[2]);

        // Double Shulker Shells
        if (entity.getType() == EntityType.SHULKER) {
            if(this.getConfig().getBoolean("double_shulker_shells", true)) {
                event.getDrops().clear();
                ItemStack shulkerStack = new ItemStack(Material.SHULKER_SHELL, 2);
                event.getEntity().getWorld().dropItemNaturally(entity.getLocation(), shulkerStack);
            }
        }

        // Ender Dragon Drops
        if (entity.getType() == EntityType.ENDER_DRAGON) {
            LivingEntity enderDragon = event.getEntity();
            Player dragonKiller = enderDragon.getKiller();
            ItemStack elytra = new ItemStack(Material.ELYTRA, 1);
            ItemStack dragonEgg = new ItemStack(Material.DRAGON_EGG, 1);
            ItemStack dragonHead = new ItemStack(Material.DRAGON_HEAD, 1);
            World dragonWorld = event.getEntity().getWorld();

            if (this.getConfig().getBoolean("give_to_player", false)) {
                if (dragonKiller.getInventory().firstEmpty() == -1) {
                    final Location dropLocation = dragonKiller.getLocation();
                    final Item elytraDropped = dragonWorld.dropItem(dropLocation, new ItemStack(Material.ELYTRA, 1));

                    dragonKiller.sendMessage("[End Drops] Inventory Full!");
                    dragonKiller.sendMessage("[End Drops] Elytra Drop Coordinates -");
                    dragonKiller.sendMessage("[End Drops] X: " + dropLocation.getBlockX() + " Y: " + dropLocation.getBlockY() + " Z: " + dropLocation.getBlockZ());

                    if (this.getConfig().getBoolean("drop_dragon_egg", true)) {
                        final Item dragonEggDropped = dragonWorld.dropItem(dropLocation, dragonEgg);
                    }

                    if(this.getConfig().getBoolean("drop_dragon_head", true)) {
                        final Item dragonHeadDropped = dragonWorld.dropItem(dropLocation, dragonHead);
                    }
                }
                else {
                    final Location dropLocation = dragonKiller.getLocation();
                    dragonKiller.getInventory().addItem(elytra);

                    if (this.getConfig().getBoolean("drop_dragon_egg", true)) {
                        if (dragonKiller.getInventory().firstEmpty() == -1) {
                            final Item dragonEggDropped = dragonWorld.dropItem(dropLocation, dragonEgg);
                        }
                        else {
                            dragonKiller.getInventory().addItem(dragonEgg);
                        }
                    }

                    if(this.getConfig().getBoolean("drop_dragon_head", true)) {
                        if (dragonKiller.getInventory().firstEmpty() == -1) {
                            final Item dragonHeadDropped = dragonWorld.dropItem(dropLocation, dragonHead);
                        }
                        else {
                            dragonKiller.getInventory().addItem(dragonHead);
                        }
                    }

                    dragonKiller.sendMessage("[End Drops] Elytra has been added to your inventory.");
                }
            }
            else {
                if (this.getConfig().getBoolean("drop_on_ground", true)) {
                    final Location dropLocation = new Location(dragonWorld, x, y, z);
                    final Item elytraDropped = dragonWorld.dropItem(dropLocation, elytra);

                    if(this.getConfig().getBoolean("drop_dragon_egg", true)) {
                        final Item dragonEggDropped = dragonWorld.dropItem(dropLocation, dragonEgg);
                    }

                    if (this.getConfig().getBoolean("drop_dragon_head", true)) {
                        final Item dragonHeadDropped = dragonWorld.dropItem(dropLocation, dragonHead);
                    }

                    dragonKiller.sendMessage("[End Drops] Elytra Drop Coordinates -");
                    dragonKiller.sendMessage("[End Drops] X: " + dropLocation.getBlockX() + " Y: " + dropLocation.getBlockY() + " Z: " + dropLocation.getBlockZ());
                }
                else {
                    if (this.getConfig().getBoolean("place_in_chest", false)) {
                        final Location chestLocation = new Location(dragonWorld, x, y, z);

                        if (!(chestLocation.getBlock() instanceof Chest)) {
                            chestLocation.getBlock().setType(Material.CHEST);
                        }

                        try {
                            final Chest chestBlock = (Chest) chestLocation.getBlock().getState();

                            Directional chestData = (Directional)chestBlock.getBlockData();
                            chestData.setFacing(BlockFace.SOUTH);
                            chestBlock.setBlockData(chestData);
                            chestBlock.update();
                            if (chestBlock.getInventory().firstEmpty() == -1) {
                                final Item elytraDropped = dragonWorld.dropItem(chestLocation.add(0, 1, 0), new ItemStack(Material.ELYTRA, 1));

                                if (this.getConfig().getBoolean("drop_dragon_egg", true)) {
                                    final Item dragonEggDropped = dragonWorld.dropItem(chestLocation.add(0, 1, 0), new ItemStack(Material.DRAGON_EGG, 1));
                                }

                                if (this.getConfig().getBoolean("drop_dragon_head", true)) {
                                    final Item dragonHeadDropped = dragonWorld.dropItem(chestLocation.add(0, 1, 0), new ItemStack(Material.DRAGON_HEAD, 1));
                                }

                                dragonKiller.sendMessage("[End Drops] Drop Chest Full! Dropping on top of chest - ");
                                dragonKiller.sendMessage("[End Drops] X: " + chestLocation.getBlockX() + " Y: " + chestLocation.getBlockY() + " Z: " + chestLocation.getBlockZ());
                            }
                            else {
                                chestBlock.getInventory().setItem(chestBlock.getInventory().firstEmpty(), elytra);

                                if (this.getConfig().getBoolean("drop_dragon_egg", true)) {
                                    if (chestBlock.getInventory().firstEmpty() == -1) {
                                        final Item dragonEggDropped = dragonWorld.dropItem(chestLocation.add(0, 1, 0), new ItemStack(Material.DRAGON_EGG, 1));
                                    }
                                    else {
                                        chestBlock.getInventory().setItem(chestBlock.getInventory().firstEmpty(), dragonEgg);
                                    }
                                }

                                if (this.getConfig().getBoolean("drop_dragon_head", true)) {
                                    if (chestBlock.getInventory().firstEmpty() == -1) {
                                        final Item dragonHeadDropped = dragonWorld.dropItem(chestLocation.add(0, 1, 0), new ItemStack(Material.DRAGON_HEAD, 1));
                                    }
                                    else {
                                        chestBlock.getInventory().setItem(chestBlock.getInventory().firstEmpty(), dragonHead);
                                    }
                                }

                                dragonKiller.sendMessage("[End Drops] Drop Chest Coordinates -");
                                dragonKiller.sendMessage("[End Drops] X: " + chestLocation.getBlockX() + " Y: " + chestLocation.getBlockY() + " Z: " + chestLocation.getBlockZ());
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
