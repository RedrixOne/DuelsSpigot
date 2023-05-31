package com.redrixone;

import com.redrixone.sumo.arena.GameManager;
import com.redrixone.sumo.arena.GameStats;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Duels extends JavaPlugin {

    //todo: several{Migliorie a livello di organizzazione; Espandere le modalità; Creazione di un file di configurazione}
    private GameManager gameManager;
    private GameStats gameStats;

    public void onEnable() {
        gameStats = new GameStats();
        gameManager = new GameManager(gameStats);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getPluginManager().registerEvents(gameManager, this);

        System.out.println("[SumoGame] Enabled! Waiting for players");
    }

    public void onDisable() {
        System.out.println("[SumoGame] Goodbye!");
    }
}
