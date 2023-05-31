package com.redrixone.sumo.arena;

import com.redrixone.Duels;

public class GameStats {

    private Duels plugin;

    public GameStats() {
        this.plugin = Duels.getPlugin(Duels.class);
    }

    public Duels getPlugin() {
        return plugin;
    }
}