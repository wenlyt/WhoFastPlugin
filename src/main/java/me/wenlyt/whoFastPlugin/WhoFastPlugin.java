package me.wenlyt.whoFastPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Plugin Activated...");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Deactivated...");
    }

    // Команда для запуска вызова
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("whofaststart")) {
            if (sender instanceof Player) {
                if (!isChallengeActive) {
                    startChallenge();
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

    private void startChallenge() {
        isChallengeActive = true;
        completedPlayers.clear();
        timeRemaining = 3600;
        Bukkit.broadcastMessage(ChatColor.GREEN + "Игра началась! Убейте дракона первым!");

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
                    Bukkit.broadcastMessage(ChatColor.GRAY + "Осталось" + (timeRemaining / 60) + " минут.");
                }

                if (timeRemaining <= 10) {
                    Bukkit.broadcastMessage(ChatColor.RED + "Осталось" + timeRemaining + " секунд.");
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

    private void endChallenge() {
        isChallengeActive = false;
        if (timerTask != null) {
            timerTask.cancel();
        }
        Bukkit.broadcastMessage(ChatColor.GRAY + "Игра закончилась");
    }
}