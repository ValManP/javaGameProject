package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import model.data.GameData;
import model.physics.Physics;

public class DrawPanel extends JPanel implements Runnable {
    GameData gameData;
    
    public DrawPanel(GameData gameData) {
        super();
        this.gameData = gameData;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            repaint();
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {}
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        if (gameData.currentState.getMallet1() != null) {
            g2d.setColor(Color.green);   
            g2d.fillOval(
                gameData.currentState.getMallet1().x - (int)Physics.MalletRadius,
                gameData.currentState.getMallet1().y - (int)Physics.MalletRadius,
                2 * (int)Physics.MalletRadius,
                2 * (int)Physics.MalletRadius
            );
        }

        if (gameData.currentState.getMallet2() != null) {
            g2d.setColor(Color.cyan);  
            g2d.fillOval(
                gameData.currentState.getMallet2().x - (int)Physics.MalletRadius,
                gameData.currentState.getMallet2().y - (int)Physics.MalletRadius,
                2 * (int)Physics.MalletRadius,
                2 * (int)Physics.MalletRadius
            );
        }

        if (gameData.currentState.getPuck() != null) {
            g2d.setColor(Color.red);   
            g2d.fillOval(
                gameData.currentState.getPuck().x - (int)Physics.PuckRadius,
                gameData.currentState.getPuck().y - (int)Physics.PuckRadius,
                2 * (int)Physics.PuckRadius,
                2 * (int)Physics.PuckRadius
            );
        }
    }
}
