/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Physics.Mallet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

/**
 *
 * @author pozdv
 */
public class ClientFrame extends javax.swing.JFrame {

    InetAddress ip = null;
    int port = 2222;
    Boolean isConnected = false;
    Boolean isPressed = false;
    
    Socket socket;
    
    Point mallet;
    
    int player_num = -1;
    
    // Game objects
    State currentState;
    State incomingState;
    Rectangle gameArea;
   
    
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    
    private Image fieldImg, yourMalletImg, puckImg, enemyMalletImg;
    
    public void ListenThread() 
    {
         Thread IncomingReader = new Thread(new IncomingReader());
         IncomingReader.start();
    }
    
    //--------------------------//
    
    public class IncomingReader implements Runnable
    {
        @Override
        public void run() 
        {
            try {
           
                while (inputStream != null)
                {
                    incomingState = (State)inputStream.readObject();

                    if (incomingState.getMallet_1() != null) {
                        currentState.setMallet_1(incomingState.getMallet_1());
                    }
                    if (incomingState.getMallet_2() != null) {
                        currentState.setMallet_2(incomingState.getMallet_2());
                    }
                    if (incomingState.getPuck() != null) {
                        currentState.setPuck(incomingState.getPuck());
                    }
                    
                }
             } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    //--------------------------//
    
    public class DrawPanel extends JPanel implements Runnable, MouseInputListener {
 
		private long t = System.nanoTime();
 
		public DrawPanel() {
			super();
                        addMouseListener(this);
                        addMouseMotionListener(this);
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
                        
                        //g2d.drawImage(fieldImg, 0, 0, 400, 700, null);
                        
                        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
                        
                        if (currentState.getMallet_1() != null) {
                            if (player_num == 1) {
                                g2d.setColor(Color.green);
                            } else {
                                g2d.setColor(Color.red);
                            }
                            g2d.fillOval(currentState.getMallet_1().x - 20, currentState.getMallet_1().y - 20, 40, 40);
                            //g2d.drawImage(yourMalletImg, currentState.getMallet_1().x - 90, currentState.getMallet_1().y - 65, 150, 100, null);
                        }

                        if (currentState.getMallet_2() != null) {
                            if (player_num == 2) {
                                g2d.setColor(Color.green);
                            } else {
                                g2d.setColor(Color.red);
                            }
                            g2d.fillOval(currentState.getMallet_2().x - 20, currentState.getMallet_2().y - 20, 40, 40);
                            //g2d.drawImage(yourMalletImg, message.getMallet_2().x - 10, message.getMallet_2().y - 10, 150, 100, null);
                        }

                        if (currentState.getPuck() != null) {
                            g2d.setColor(Color.yellow);   
                            g2d.fillOval(currentState.getPuck().x - 20, currentState.getPuck().y - 20, 40, 40);
                            //g2d.drawImage(puckImg, message.getPuck().x - 50, message.getPuck().y - 50, 100, 100, null);
                        }
                        
		}
                
                public void mouseDragged( MouseEvent e )
                {
                    if (mallet != null) {
                        if (Math.abs(mallet.x - e.getX()) < 20 && Math.abs(mallet.y - e.getY()) < 20) {
                            mallet.x = getClippedX(e.getX(), gameArea);
                            mallet.y = getClippedY(e.getY(), gameArea);
                            if (player_num == 1) {
                                currentState.setMallet_1(mallet);
                            } else {
                                currentState.setMallet_2(mallet);
                            }
                            if (outputStream != null) {
                                try {
                                    outputStream.reset();
                                    outputStream.writeObject(currentState);
                                } catch (IOException ex) {
                                    Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                }
                
                public int getClippedX( int oldX, Rectangle r)
                {
                    if (oldX <= currentState.getMalletRadius()) {
                        return currentState.getMalletRadius();
                    } else {
                        return Math.min(oldX, r.width - 20);
                    }
                }
                
                public int getClippedY( int oldY, Rectangle r)
                {
                    if (oldY <= currentState.getMalletRadius()) {
                        return currentState.getMalletRadius();
                    } else {
                        return Math.min(oldY, r.height - 20);
                    }
                }
                
                public void mousePressed( MouseEvent e )
                {
                    
                }

                public void mouseReleased( MouseEvent e )
                {
                   
                }
                
                // Unused Mouse Listener Methods
                public void mouseMoved( MouseEvent e ) {}
                public void mouseClicked( MouseEvent e ) {
                    logArea.append(e.getPoint().toString()+"\n");
                }
                public void mouseEntered( MouseEvent e ) {}
                public void mouseExited( MouseEvent e ) {}
	}
    

    //--------------------------//
    
    /**
     * Creates new form ClientFrame
     */
    public ClientFrame() {
        initComponents();
        
        DrawPanel panel = new DrawPanel();
        panel.setBackground(Color.white);
        panel.setSize(new Dimension(300, 400));
        this.add(panel);
        //this.pack();
        setVisible(true);
        //this.setLocationRelativeTo(null);
        try {
            fieldImg = ImageIO.read(new File("src/image/fieldImg.jpg"));
            yourMalletImg = ImageIO.read(new File("src/image/malletImg.png"));
            yourMalletImg = ImageIO.read(new File("src/image/malletImg.png"));
            puckImg = ImageIO.read(new File("src/image/puckImg.png"));
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        currentState = new State();
        incomingState = new State();
        gameArea = new Rectangle(0, 0, 300, 400);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connect = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        logArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setPreferredSize(new java.awt.Dimension(305, 600));
        setResizable(false);

        connect.setText("Connect");
        connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectActionPerformed(evt);
            }
        });

        logArea.setColumns(20);
        logArea.setRows(5);
        jScrollPane1.setViewportView(logArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(connect)
                        .addGap(0, 306, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(693, Short.MAX_VALUE)
                .addComponent(connect)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectActionPerformed
        if (isConnected == false) 
        {

            try 
            {
                socket = new Socket(InetAddress.getLocalHost(), port);
 
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
                
                if (player_num == -1) {
                      player_num = ((State)inputStream.readObject()).getPlayerNum();
                }
                
                if (player_num == 1) {
                    mallet = new Point(150, 50);
                    currentState.setMallet_1(mallet);
                } else {
                    mallet = new Point(150, 250);
                    currentState.setMallet_2(mallet);
                }
                
                
                isConnected = true; 
                        
                logArea.append("Connected to the server\n");
                logArea.append("You are player #"+player_num+"\n");
            } 
            catch (Exception ex) 
            {
                logArea.append("Cannot Connect! Try Again. \n");
            }
            
            ListenThread();
            
        } else if (isConnected == true) 
        {
            logArea.append("You are already connected. \n");
        }
    }//GEN-LAST:event_connectActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ClientFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton connect;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea logArea;
    // End of variables declaration//GEN-END:variables
}
