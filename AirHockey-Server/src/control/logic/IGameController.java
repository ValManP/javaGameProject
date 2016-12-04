package control.logic;

import control.connect.IConnectionController;

public interface IGameController {
    public void game();
    void handleGameOver();
    void saveGame();
}
