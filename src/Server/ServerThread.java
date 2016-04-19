/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Client.ClientFrame;
import Client.State;
import java.awt.Canvas;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.MouseInputListener;

public class ServerThread extends Thread{
    JTextArea log;
    boolean f = true;
    
    int port = 2222;
    InetAddress ip = null;
    
    ServerSocket ss;
    ClientThread player_1, player_2;
    int client_count = 0;
    
    Client.State currentState;
    Client.State incomingState;
    
    GameCycle gameCycle;
    
    public synchronized void addToLog(String s)
    {
        log.append(s + "\n");
    }
    
    synchronized void sendToAll(Client.State message)
    {
        if (player_1 != null && player_2 != null) {
            player_1.sendMessage(currentState);
            player_2.sendMessage(currentState);
        }
    }
    
    synchronized void stopServer()
    {
        f = false;
        stop();
    }
    
    ServerThread(JTextArea log, JFrame mainPanel)
    {
        this.log = log;
        
        try {
            ip = InetAddress.getLocalHost();
            ss = new ServerSocket(port, 0, ip);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        DrawPanel panel = new DrawPanel();
        panel.setBackground(Color.white);
        panel.setSize(new Dimension(300, 400));
        panel.setLocation(50, 50);
        mainPanel.add(panel);
        mainPanel.pack();
        mainPanel.setVisible(true);
        //mainPanel.setLocationRelativeTo(null);
        
        currentState = new Client.State();
        incomingState = new Client.State();
        
        gameCycle = new GameCycle(currentState);
    }

    @Override
    public void run() {
        
        while (f == true) 
        {
            try {
                Socket clientSocket = ss.accept();
                Client.State m = new Client.State();
                
                ClientThread ct = new ClientThread(this, clientSocket);
                
                client_count++;
                m.setPlayerNum(client_count);
                
                if (client_count == 1) {
                   player_1 = ct;
                } else if (client_count == 2) {
                   player_2 = ct;
                }             
                
                ct.sendMessage(m);

                addToLog("Player " + client_count + " connect to the game. \n");
            } catch (IOException ex) {
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
                g2d.fillOval(currentState.getMallet_1().x - 20, currentState.getMallet_1().y - 20, 40, 40);
            }

            if (currentState.getMallet_2() != null) {
                g2d.setColor(Color.cyan);  
                g2d.fillOval(currentState.getMallet_2().x - 20, currentState.getMallet_2().y - 20, 40, 40);
            }

            if (currentState.getPuck() != null) {
                g2d.setColor(Color.red);   
                g2d.fillOval(currentState.getPuck().x - 20, currentState.getPuck().y - 20, 40, 40);
            }
        }
    }
    
    public synchronized Client.State getMessage()
    {
        return currentState;
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

                        if (m1 != null) {
                            incomingState.setMallet_1(m1);
                       }
                    }

                    if (player_2 != null) {
                        m2 = player_2.getMes().getMallet_2(); 

                        if (m2 != null) {
                            incomingState.setMallet_2(m2);
                        }
                    }

                    if (m1 != null || m2 != null) {
                        currentState = gameCycle.calculate(incomingState);
                        sendToAll(currentState);

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
}
