package model.physics;

import java.io.Serializable;
import java.awt.Point;

public class AirHockeyState implements Serializable {
    // Player data
    private int player_num;
    private String player_name;
    
    // Game data
    public boolean isPaused = true;
    public boolean isFirstReady = false;
    public boolean isSecondReady = false;
    public boolean isGame = false;
    private Point puck;
    private Point mallet1, mallet2;
    
    final private int malletRadius = 30;
    final private int puckRadius = 20;
    
    private int disconnectedPlayer = 0;
    private boolean disconnected = false;
    
    public int firstScore = 0;
    public int secondScore = 0;
    

    public AirHockeyState(Point puck, Point mallet1, Point mallet2) {
        this.puck = puck;
        this.mallet1 = mallet1;
        this.mallet2 = mallet2;
    }
    
    public AirHockeyState() {}
    
    public AirHockeyState(boolean disconnect) {
        disconnected = disconnect;
    }

    public Point getMallet1() {
        return mallet1;
    }

    public Point getMallet2() {
        return mallet2;
    }

    public void setMallet1(Point mallet) {
        this.mallet1 = mallet;
    }

    public void setMallet2(Point mallet) {
        this.mallet2 = mallet;
    }

    public Point getPuck() {
        return puck;
    }

    public void setPuck(Point puck) {
        this.puck = puck;
    }
    
    public int getPlayerNum() {
        return player_num;
    }

    public void setPlayerNum(int value) {
        this.player_num = value;
    }
    
    public int getMalletRadius() {
        return malletRadius;
    }
    
    public int getPuckRadius() {
        return puckRadius;
    }
            
    public int getDisconnectedPlayer() {
        return disconnectedPlayer;
    }

    public void setDisconnectedPlayer(int disconnectedPlayer) {
        this.disconnectedPlayer = disconnectedPlayer;
    }
    
    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }
    
    public String getPlayerName() {
        return player_name;
    }

    public void setPlayerName(String player_name) {
        this.player_name = player_name;
    }
}
