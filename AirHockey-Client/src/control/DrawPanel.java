package control;

import control.connect.IConnectionController;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;
import model.GameData;
import model.physics.Physics;

public class DrawPanel extends JPanel implements Runnable, MouseInputListener {
    private long t = System.nanoTime();

    IConnectionController connectionController;
    GameData gameData;

    public DrawPanel(GameData gameData, IConnectionController connectionController) {
        super();
        addMouseListener(this);
        addMouseMotionListener(this);
        setLocation(10, 10);
        this.gameData = gameData;
        this.connectionController = connectionController;
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

    // Отразить координату X для первого игрока
    private int getCoordinateX(int coord) {
        return (gameData.playerNum == 1) ? gameData.gameArea.width - coord : coord;
    }

    // Отразить координату Y для первого игрока
    private int getCoordinateY(int coord) {
        return (gameData.playerNum == 1) ? gameData.gameArea.height - coord : coord;
    }

    @Override
    public void paint(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.drawImage(gameData.fieldImg, 0, 0, Physics.Field.width, Physics.Field.height, null);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (gameData.currentState.getMallet1() != null) {
            g2d.drawImage(
                (gameData.playerNum == 1) ? gameData.yourMalletImg : gameData.enemyMalletImg,
                getCoordinateX(gameData.currentState.getMallet1().x) - (int)Physics.MalletRadius,
                getCoordinateY(gameData.currentState.getMallet1().y) - (int)Physics.MalletRadius,
                2 * (int)Physics.MalletRadius,
                2 * (int)Physics.MalletRadius,
                null
            );
        }

        if (gameData.currentState.getMallet2() != null) {
            g2d.drawImage(
                (gameData.playerNum == 2) ? gameData.yourMalletImg : gameData.enemyMalletImg,
                getCoordinateX(gameData.currentState.getMallet2().x) - (int)Physics.MalletRadius,
                getCoordinateY(gameData.currentState.getMallet2().y) - (int)Physics.MalletRadius,
                2 * (int)Physics.MalletRadius,
                2 * (int)Physics.MalletRadius,
                null
            );
        }

        if (gameData.currentState.getPuck() != null) {
            g2d.drawImage(
                gameData.puckImg, 
                getCoordinateX(gameData.currentState.getPuck().x) - (int)Physics.PuckRadius,
                getCoordinateY(gameData.currentState.getPuck().y) - (int)Physics.PuckRadius,
                2 * (int)Physics.PuckRadius,
                2 * (int)Physics.PuckRadius,
                null
            );
        }

        Font font = new Font("Courier", Font.BOLD, 30);

        g2d.setFont(font);
        g2d.setColor(Color.CYAN);
        if (gameData.playerNum == 2) {
            g2d.drawString(String.valueOf(gameData.currentState.secondScore), Physics.Field.width - 35, Physics.Field.height / 2 - 20);
            g2d.drawString(String.valueOf(gameData.currentState.firstScore), Physics.Field.width - 35, Physics.Field.height / 2 + 40);
        } else {
            g2d.drawString(String.valueOf(gameData.currentState.firstScore), Physics.Field.width - 35, Physics.Field.height / 2 - 20);
            g2d.drawString(String.valueOf(gameData.currentState.secondScore), Physics.Field.width - 35, Physics.Field.height / 2 + 40);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (gameData.mallet != null && gameData.currentState.isGame) {
            if ((Math.abs(getCoordinateX(gameData.mallet.x)- e.getX()) < Physics.MalletRadius) &&
                (Math.abs(getCoordinateY(gameData.mallet.y) - e.getY()) < Physics.MalletRadius)) {

                gameData.mallet.x = getClippedX(getCoordinateX(e.getX()), gameData.gameArea);
                gameData.mallet.y = getClippedY(getCoordinateY(e.getY()), gameData.gameArea);

                if (gameData.playerNum == 1) {
                    gameData.currentState.setMallet1(gameData.mallet);
                } else {
                    gameData.currentState.setMallet2(gameData.mallet);
                }

                connectionController.sendMessage();
            }
        }
    }

    public int getClippedX(int oldX, Rectangle rect) {
        if (oldX <= Physics.MalletRadius) {
            return (int)Physics.MalletRadius;
        } else {
            return Math.min(oldX, rect.width - (int)Physics.MalletRadius);
        }
    }

    public int getClippedY(int oldY, Rectangle rect) {
        int delta = 0;

        if (gameData.playerNum == 2) {
            delta = rect.height / 2;
        }

        if (oldY <= delta + Physics.MalletRadius) {
            return delta + (int)Physics.MalletRadius;
        } else {
            if (gameData.playerNum == 1) {
                delta = rect.height / 2;
            } else {
                delta = 0;
            }
            return Math.min(oldY, rect.height - delta - (int)Physics.MalletRadius);
        }
    }

    // Unused Mouse Listener Methods
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
}
