package control.logic;

import model.physics.AirHockeyState;

public interface IGameController{
    
    public void startGame();
    public void resetGame();
    public void disconnectGame();
    public void changeGameStatus(boolean status);
    public void handleScore(AirHockeyState incomingState);
}
