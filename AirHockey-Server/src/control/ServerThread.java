package control;

import model.database.DBInterface;
import model.physics.Physics;
import model.physics.AirHockeyState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ServerThread extends Thread {
    JTextArea log;
    boolean f = true;
    
    int port;
    InetAddress ip;
    
    ServerSocket ss;
    ClientThread player_1, player_2;
    int client_count = 0;
    
    AirHockeyState currentState;
    AirHockeyState incomingState;
    
    GameCycle gameCycle;
    DBInterface dbInterface;
    Thread gameThread;
    
    public ServerThread(JTextArea log, JFrame mainPanel) {
        this.log = log;
        
        DrawPanel panel = new DrawPanel();
        panel.setBackground(Color.white);
        panel.setSize(Physics.Field);
        panel.setLocation(20, 20);
        mainPanel.add(panel);
        mainPanel.pack();
        mainPanel.setVisible(true);
        
        currentState = new AirHockeyState();
        incomingState = new AirHockeyState();
        
        gameCycle = new GameCycle(currentState);
        dbInterface = new DBInterface(log);
        try {
            dbInterface.connect();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(port, 0, ip);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        while (f == true) {
            try {
                Socket clientSocket = ss.accept();
                AirHockeyState m = new AirHockeyState();
                
                ClientThread ct = new ClientThread(this, clientSocket);
                
                if (dbInterface.findUserByName(ct.getPlayerName()) == 0) {
                    addToLog("Player not found. Added " + ct.getPlayerName() + " to game DB.");
                    dbInterface.addUser(ct.getPlayerName());
                }
                
                if (player_1 == null) {
                    player_1 = ct;
                    m.setPlayerNum(1);
                    ct.setPlayerNum(1);
                    ct.sendMessage(m);
                } else if (player_2 == null) {
                    player_2 = ct;
                    m.setPlayerNum(2);
                    ct.setPlayerNum(2);
                    ct.sendMessage(m);
                }
                
                //ct.sendMessage(m);

                addToLog("Player " + ct.getPlayerName() + " connected to the game.");
            } catch (IOException | SQLException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public class DrawPanel extends JPanel implements Runnable {
        public DrawPanel() {
            super();
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

            if (currentState.getMallet1() != null) {
                g2d.setColor(Color.green);   
                g2d.fillOval(
                    currentState.getMallet1().x - (int)Physics.MalletRadius,
                    currentState.getMallet1().y - (int)Physics.MalletRadius,
                    2 * (int)Physics.MalletRadius,
                    2 * (int)Physics.MalletRadius
                );
            }

            if (currentState.getMallet2() != null) {
                g2d.setColor(Color.cyan);  
                g2d.fillOval(
                    currentState.getMallet2().x - (int)Physics.MalletRadius,
                    currentState.getMallet2().y - (int)Physics.MalletRadius,
                    2 * (int)Physics.MalletRadius,
                    2 * (int)Physics.MalletRadius
                );
            }

            if (currentState.getPuck() != null) {
                g2d.setColor(Color.red);   
                g2d.fillOval(
                    currentState.getPuck().x - (int)Physics.PuckRadius,
                    currentState.getPuck().y - (int)Physics.PuckRadius,
                    2 * (int)Physics.PuckRadius,
                    2 * (int)Physics.PuckRadius
                );
            }
        }
    }
    
    public synchronized void stopServer() {
        f = false;
        currentState.setDisconnected(true);
        
        if (player_1 != null) {
            player_1.sendMessage(currentState);
            player_1.Disconnect();
            disconnect(1);
        }
        if (player_2 != null) {
            player_2.sendMessage(currentState);
            player_2.Disconnect();
            disconnect(2);
        }
        try {
            ss.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        interrupt();
    }
    
    public synchronized AirHockeyState getMessage() {
        return currentState;
    }
    
    public void disconnect(int player_num) {
        addToLog("Player " + player_num + " disconnected");
        if (gameThread != null) {
            gameThread.interrupt();
            gameThread = null;
        }
        switch (player_num) {
            case 1: {
                player_1.interrupt();
                player_1 = null;
                sendGameOver("Player 1 has disconnected\n", player_2);
                break;
            }
            case 2: {
                player_2.interrupt();
                player_2 = null;
                sendGameOver("Player 2 has disconnected\n", player_1);
                break;
            }
        }
        reset();
    }
    
    public void sendGameOver(String message, ClientThread player) {
        AirHockeyState state = new AirHockeyState();
        state.message = message;
        state.isGameOver = true;
        player.sendMessage(state);
    }
    
    private void reset(){
        currentState = new AirHockeyState();
        incomingState = new AirHockeyState();
        gameCycle = new GameCycle(currentState);
    }
    
    public void game() {
        if (gameThread == null) {
            addToLog("New game");
            gameThread = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        Point m1 = null, m2 = null;

                        if (player_1 != null) {
                            m1 = player_1.getMes().getMallet1();

                            currentState.isFirstReady = player_1.getMes().isFirstReady;
                            if (m1 != null) {
                                incomingState.setMallet1(m1);
                           }
                        }

                        if (player_2 != null) {
                            m2 = player_2.getMes().getMallet2(); 

                            currentState.isSecondReady = player_2.getMes().isSecondReady;
                            if (m2 != null) {
                                incomingState.setMallet2(m2);
                            }
                        }

                        if (m1 != null && m2 != null) {
                            if (currentState.isFirstReady && currentState.isSecondReady) {
                                currentState.isGame = true;
                                currentState = gameCycle.calculate(incomingState);
                                handleGameOver();
                                sendToAll(currentState);
                            } else {
                                currentState.isGame = false;
                            }
/*
                            float elapsedMilliTime = gameCycle.getElapsedNanoTime() / 1000.0f;
                            float toSleep = 17.0f - elapsedMilliTime;
                            if (toSleep > 0.0f) {
                                try {
                                    Thread.sleep((long)toSleep);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }*/
                        }
                    }
                }
            };
            gameThread.start();
       }
    }
    
    public void setConnectionData(InetAddress _ip, int _port) {
        ip = _ip;
        port = _port;
    }
    
    public synchronized void addToLog(String s) {
        log.append(s + "\n");
    }
    
    synchronized void sendToAll(AirHockeyState message) {
        if (player_1 != null && player_2 != null) {
            player_1.sendMessage(message);
            player_2.sendMessage(message);
        }
    }
    
    private void handleGameOver() {
        if (currentState.firstScore == GameCycle.MAX_SCORE 
                || currentState.secondScore == GameCycle.MAX_SCORE) {
            saveGame();
            if (gameThread != null) {
                gameThread.interrupt();
                gameThread = null;
            }
            if (currentState.firstScore == GameCycle.MAX_SCORE) {
                sendGameOver("You win\n", player_1);
                sendGameOver("You lose\n", player_2);
            } else if (currentState.secondScore == GameCycle.MAX_SCORE) {
                sendGameOver("You lose\n", player_1);
                sendGameOver("You win\n", player_2);
            }
            reset();
        }
    }
    
    private void saveGame() {
        try {
            dbInterface.addGame(player_1.getPlayerName(),
                    player_2.getPlayerName(),
                    currentState.firstScore, currentState.secondScore);
        } catch (SQLException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
