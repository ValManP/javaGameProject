package model.physics;

import java.io.Serializable;

public class State implements Serializable {
    private boolean isAndroid = false;
    private String player_name;
    
    public void setIsAndroid(boolean isAndroid) {
        this.isAndroid = isAndroid;
    }
    
    public boolean getIsAndroid() {
        return this.isAndroid;
    }
    
     public String getPlayerName() {
        return player_name;
    }

    public void setPlayerName(String player_name) {
        this.player_name = player_name;
    }
}
