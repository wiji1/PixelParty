package dev.wiji.pixelparty.util;

import java.util.*;
import java.lang.*;

public class ELOMatch {
    public final ArrayList<ELOPlayer> players = new ArrayList<>();

    public void addPlayer(UUID name, int place, int elo) {
        ELOPlayer player = new ELOPlayer();
        
        player.uuid = name;
        player.place = place;
        player.eloPre = elo;
        
        players.add(player);
    }

    public int getELO(UUID name) {
        for(ELOPlayer p : players) {
            if(Objects.equals(p.uuid, name)) return p.eloPost;
        }
        return 1000;
    }

    public int getELOChange(UUID name) {
        for(ELOPlayer p : players) {
            if(Objects.equals(p.uuid, name)) return p.eloChange;
        }
        return 0;
    }

    public void calculateELOs() {
        int n = players.size();
        float k = 32 / (float)(n - 1);
        
        for(int i = 0; i < n; i++) {
            int curPlace = players.get(i).place;
            int curELO   = players.get(i).eloPre;
            
            for(int j = 0; j < n; j++) {
                if(i != j) {
                    int opponentPlace = players.get(j).place;
                    int opponentELO   = players.get(j).eloPre;

                    float s;
                    if(curPlace < opponentPlace) s = 1.0F;
                    else if(curPlace == opponentPlace) s = 0.5F;
                    else s = 0.0F;

                    float ea = 1 / (1.0f + (float)Math.pow(10.0f, (opponentELO - curELO) / 400.0f));

                    players.get(i).eloChange += Math.round(k * (s - ea));
                }
            }

            players.get(i).eloPost = players.get(i).eloPre + players.get(i).eloChange;
        }
    }

    public static class ELOPlayer {
        public UUID uuid;

        public int place = 0;
        public int eloPre = 0;
        public int eloPost = 0;
        public int eloChange = 0;
    }
}