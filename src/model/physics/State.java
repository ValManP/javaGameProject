package model.physics;

import java.io.Serializable;
import java.awt.Point;

public class State implements Serializable {
    // Player data
    private int player_num;
    private String player_name;
    
    // Game data
    public boolean isPaused = true;
    public boolean isFirstReady = false;
    public boolean isSecondReady = false;
    public boolean isGame = false;
    private Point puck;
    private Point mallet_1, mallet_2;
    
    private int malletRadius = 20;
    
    private int disconnectedPlayer = 0;
    private boolean disconnected = false;
    
    public int firstScore = 0;
    public int secondScore = 0;
    

    public State(Point puck, Point mallet_1, Point mallet_2) {
        this.puck = puck;
        this.mallet_1 = mallet_1;
        this.mallet_2 = mallet_2;
    }
    
    public State() {
    }
    
    public State(boolean disconnect) {
        disconnected = disconnect;
    }

    public Point getMallet_1() {
        return mallet_1;
    }

    public Point getMallet_2() {
        return mallet_2;
    }

    public void setMallet_1(Point mallet) {
        this.mallet_1 = mallet;
    }

    public void setMallet_2(Point mallet) {
        this.mallet_2 = mallet;
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
