package com.redrixone.sumo.arena;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class GameManager implements Listener {

    Plugin plugin = getServer().getPluginManager().getPlugin("SumoGame");
    private GameStats gameStats;
    private List<Player> players;
    private BukkitRunnable countdownTask;
    private int countdownSeconds = 10;
    private BukkitRunnable delayTask;
    private int matchDurationSeconds = 60;
    private int endHeight = 61;

    public GameManager(GameStats gameStats) {
        this.gameStats = gameStats;
        this.players = new ArrayList<>();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getInventory().setItem(8, new ItemStack(Material.BED));
        if (players.size() < 2) {
            players.add(player);

            if (players.size() == 2) {
                startCountdown();
            }
        } else {
            sendToLobby(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (players.contains(player)) {
            players.remove(player);

            if (countdownTask != null && players.size() < 2) {
                stopCountdown();
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();

        if (players.contains(player) && action == Action.RIGHT_CLICK_AIR && item != null && item.getType() == Material.BED) {
            if (isMatchInProgress()) {
                event.setCancelled(true);
                return;
            }

            sendToLobby(player);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        Player player = (Player) event.getEntity();
        player.setHealth(20);
    }

    private void startCountdown() {
        countdownTask = new BukkitRunnable() {
            int secondsLeft = countdownSeconds;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    broadcastMessage("Match starting in " + secondsLeft + " seconds...");
                    secondsLeft--;
                } else {
                    broadcastMessage("Match started!");
                    cancel();
                    startMatch();
                }
            }
        };
        countdownTask.runTaskTimer(gameStats.getPlugin(), 0, 20);
    }

    private void stopCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    private void startMatch() {
        World world = Bukkit.getWorlds().get(0);
        Location player1Location = new Location(world, 7.5, 66, 0.5);
        Location player2Location = new Location(world, -7.5, 66, 0.5);
        players.get(0).teleport(player1Location);
        players.get(1).teleport(player2Location);
        players.get(0).getInventory().remove(Material.BED);
        players.get(1).getInventory().remove(Material.BED);

        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    if (player.getLocation().getY() < endHeight) {
                        endMatch();
                        return;
                    }
                }

                matchDurationSeconds--;
                if (matchDurationSeconds > 0 && matchDurationSeconds < 11) {
                    broadcastMessage("Match will end in " + matchDurationSeconds + " seconds.");
                } else if (matchDurationSeconds <= 0) {
                    cancel();
                    endMatch();
                }
            }
        };

        countdownTask.runTaskTimer(gameStats.getPlugin(), 0, 20);
    }

    private boolean isMatchInProgress() {
        return countdownTask != null;
    }

    private void endMatch() {
        if (players.size() == 2) {
            Player player1 = players.get(0);
            Player player2 = players.get(1);

            if (player1.getLocation().getY() < endHeight && player2.getLocation().getY() < endHeight) {
                broadcastMessage("Match ended in a draw!");
            } else if (player1.getLocation().getY() < endHeight) {
                sendMatchResult(player2, player1);
            } else if (player2.getLocation().getY() < endHeight) {
                sendMatchResult(player1, player2);
            } else {
                broadcastMessage("Match ended in a draw!");
            }

            countdownTask.cancel();

            delayTask = new BukkitRunnable() {
                @Override
                public void run() {
                    sendToLobby(player2);
                    sendToLobby(player1);
                }
            };

            delayTask.runTaskLater(gameStats.getPlugin(), 60);
        }

        players.clear();
    }

    private void sendToLobby(Player player) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("Connect");
            out.writeUTF("lobby");
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
    }

    private void sendMatchResult(Player winner, Player loser) {
        winner.sendTitle(ChatColor.translateAlternateColorCodes('&', "&bVictory!"), ChatColor.translateAlternateColorCodes('&', "&3You won the match!"));
        loser.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cDefeat!"), ChatColor.translateAlternateColorCodes('&', "&4" + winner.getName() + " won the match"));
    }

    private void broadcastMessage(String message) {
        Bukkit.broadcastMessage(message);
    }

}
