package me.wenlyt.whoFastPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class WhoFastPlugin extends JavaPlugin implements Listener {
    private boolean isChallengeActive = false;
    private final Map<Player, Boolean> completedPlayers = new HashMap<>();
    private int timeRemaining = 3600;
    private BukkitRunnable timerTask;
    private final Map<Location, Material> originalBlocks = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Plugin Activated...");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Deactivated...");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("whofaststart")) {
            if (sender instanceof Player) {
                if (!isChallengeActive) {
                    startChallenge(((Player) sender).getWorld());
                    return true;
                } else {
                    sender.sendMessage(ChatColor.BLUE + "Игра уже начата");
                }
            } else {
                sender.sendMessage(ChatColor.GREEN + "Напиши комманду в майнкрате за игрока.");
            }
        }
        return false;
    }

    private void startChallenge(org.bukkit.World world) {
        isChallengeActive = true;
        completedPlayers.clear();
        timeRemaining = 3600;
        saveWorldState(world); // Сохранить мир
        Bukkit.broadcastMessage(ChatColor.GREEN + "Игра началась! Убейте дракона первым!");
        startTimer();
    }

    private void startTimer() {
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (timeRemaining <= 0) {
                    Bukkit.broadcastMessage(ChatColor.RED + "Время вышло.");
                    endChallenge();
                    this.cancel();
                    return;
                }

                if (timeRemaining % 900 == 0 && timeRemaining > 300) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "Осталось " + (timeRemaining / 60) + " минут.");
                }

                if (timeRemaining <= 300 && timeRemaining % 60 == 0 && timeRemaining > 10) {
                    Bukkit.broadcastMessage(ChatColor.GRAY + "Осталось " + (timeRemaining / 60) + " минут.");
                }

                if (timeRemaining <= 10) {
                    Bukkit.broadcastMessage(ChatColor.RED + "Осталось " + timeRemaining + " секунд.");
                }

                timeRemaining--;
            }
        };
        timerTask.runTaskTimer(this, 0L, 20L);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (isChallengeActive && event.getEntity().getType() == EntityType.ENDER_DRAGON) {
            Player killer = event.getEntity().getKiller();
            if (killer != null && !completedPlayers.containsKey(killer)) {
                completedPlayers.put(killer, true);
                Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + killer.getName() + " победил в игре!");
                endChallenge();
            }
        }
    }

    private void saveWorldState(org.bukkit.World world) {
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Сохраняем состояние мира...");
        for (Chunk chunk : world.getLoadedChunks()) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < world.getMaxHeight(); y++) {
                        Location loc = chunk.getBlock(x, y, z).getLocation();
                        Block block = loc.getBlock();
                        if (block.getType() != Material.AIR) {
                            originalBlocks.put(loc, block.getType());
                        }
                    }
                }
            }
        }
        Bukkit.broadcastMessage(ChatColor.GREEN + "Состояние мира сохранено.");
    }

    private void restoreWorldState() {
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Восстанавливаем состояние мира...");
        for (Map.Entry<Location, Material> entry : originalBlocks.entrySet()) {
            Location loc = entry.getKey();
            Material originalMaterial = entry.getValue();
            Block block = loc.getBlock();
            if (block.getType() != originalMaterial) {
                block.setType(originalMaterial);
            }
        }
        originalBlocks.clear();
        Bukkit.broadcastMessage(ChatColor.GREEN + "Мир восстановлен.");
    }

    private void endChallenge() {
        isChallengeActive = false;
        if (timerTask != null) {
            timerTask.cancel();
        }
        Bukkit.broadcastMessage(ChatColor.GRAY + "Игра закончилась");
        restoreWorldState();
    }
}