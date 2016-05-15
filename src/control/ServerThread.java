package control;

import model.database.DBInterface;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ServerThread extends Thread{
    JTextArea log;
    boolean f = true;
    
    int port;
    InetAddress ip;
    
    ServerSocket ss;
    ClientThread player_1, player_2;
    int client_count = 0;
    
    model.physics.State currentState;
    model.physics.State incomingState;
    
    GameCycle gameCycle;
    DBInterface dbInterface;
    
    public ServerThread(JTextArea log, JFrame mainPanel)
    {
        this.log = log;
        
        DrawPanel panel = new DrawPanel();
        panel.setBackground(Color.white);
        panel.setSize(new Dimension(400, 700));
        panel.setLocation(50, 50);
        mainPanel.add(panel);
        mainPanel.pack();
        mainPanel.setVisible(true);
        
        currentState = new model.physics.State();
        incomingState = new model.physics.State();
        
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
        
        while (f == true) 
        {
            try {
                Socket clientSocket = ss.accept();
                model.physics.State m = new model.physics.State();
                
                ClientThread ct = new ClientThread(this, clientSocket);
                
                if (dbInterface.findUserByName(ct.getPlayerName()) == 0) {
                    addToLog("Player not found. Add " + ct.getPlayerName() + " to game DB.");
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

                addToLog("Player " + ct.getPlayerName() + " connect to the game.");
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

            if (currentState.getMallet_1() != null) {
                g2d.setColor(Color.green);   
                g2d.fillOval(currentState.getMallet_1().x - currentState.getMalletRadius()
                        , currentState.getMallet_1().y - currentState.getMalletRadius()
                        , 2 * currentState.getMalletRadius()
                        , 2 * currentState.getMalletRadius());
            }

            if (currentState.getMallet_2() != null) {
                g2d.setColor(Color.cyan);  
                g2d.fillOval(currentState.getMallet_2().x - currentState.getMalletRadius()
                        , currentState.getMallet_2().y - currentState.getMalletRadius()
                        , 2 * currentState.getMalletRadius()
                        , 2 * currentState.getMalletRadius());
            }

            if (currentState.getPuck() != null) {
                g2d.setColor(Color.red);   
                g2d.fillOval(currentState.getPuck().x - currentState.getPuckRadius()
                        , currentState.getPuck().y - currentState.getPuckRadius()
                        , 2 * currentState.getPuckRadius()
                        , 2 * currentState.getPuckRadius());
            }
        }
    }
    
    public synchronized void stopServer()
    {
        f = false;
        currentState.setDisconnected(true);
        
        if (player_1 != null) {
            player_1.sendMessage(currentState);
            player_1.Disconnect();
            player_1.interrupt();
            player_1 = null;
        }
        if (player_2 != null) {
            player_2.sendMessage(currentState);
            player_2.Disconnect();
            player_2.interrupt();
            player_2 = null;
        }
        try {
            ss.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        stop();
    }
    
    public synchronized model.physics.State getMessage()
    {
        return currentState;
    }
    
    public void disconnect(int player_num) {
        
        addToLog("Player " + player_num + " has disconnected");
        
        switch (player_num) {
            case 1: {
                player_1.interrupt();
                player_1 = null;
                break;
            }
            case 2: {
                player_2.interrupt();
                player_2 = null;
                break;
            }
        }
    }
    
    public void game()
    {
        new Thread()
        {
            @Override
            public void run() {

                while(true){
                    Point m1 = null, m2 = null;

                    if (player_1 != null) {
                        m1 = player_1.getMes().getMallet_1();

                        currentState.isFirstReady = player_1.getMes().isFirstReady;
                        if (m1 != null) {
                            incomingState.setMallet_1(m1);
                       }
                    }

                    if (player_2 != null) {
                        m2 = player_2.getMes().getMallet_2(); 
                        
                        currentState.isSecondReady = player_2.getMes().isSecondReady;
                        if (m2 != null) {
                            incomingState.setMallet_2(m2);
                        }
                    }
                   

                    if (m1 != null && m2 != null) {
                        if (currentState.isFirstReady && currentState.isSecondReady) {
                            currentState.isGame = true;
                            currentState = gameCycle.calculate(incomingState);
                            sendToAll(currentState);
                        } else {
                            currentState.isGame = false;
                        }

                        float elapsedMilliTime = gameCycle.getElapsedNanoTime() / 1000.0f;
                        float toSleep = 17.0f - elapsedMilliTime;
                        if (toSleep > 0.0f)
                        {
                            try {
                                Thread.sleep((long)toSleep);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
        }.start();
    }
    
    public void setConnectionData(InetAddress _ip, int _port) {
        ip = _ip;
        port = _port;
    }
    
    public synchronized void addToLog(String s)
    {
        log.append(s + "\n");
    }
    
    synchronized void sendToAll(model.physics.State message)
    {
        if (player_1 != null && player_2 != null) {
            player_1.sendMessage(message);
            player_2.sendMessage(message);
        }
    }
}
