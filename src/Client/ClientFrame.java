package Client;

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

public class ClientFrame extends javax.swing.JFrame {

    // Connection data
    private InetAddress ip = null;
    private int port = 2222;
    private boolean isConnected = false;
    private Socket socket;
    private Thread incomingReader;   
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    
    // Game data
    private Point mallet;
    private Image fieldImg, yourMalletImg, puckImg, enemyMalletImg;
    private final Rectangle gameArea;
    
    // Game objects
    private final State currentState;
    private State incomingState;
    private int player_num = -1;
    
    //--------------------------//
    /**
     * Input listener
     */
    public void ListenThread() 
    {
         incomingReader = new Thread(new IncomingReader());
         incomingReader.start();
    }
    
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
    /**
     * Draw Panel
     */
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
                        g2d.setColor(Color.black);
                        g2d.drawRoundRect(0, 0, gameArea.width, gameArea.height, 20, 20);
                        g2d.drawRect(0, 0, gameArea.width, gameArea.height/2);
                        
                        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
                        
                        if (currentState.getMallet_1() != null) {
                            if (player_num == 1) {
                                g2d.setColor(Color.green);
                            } else {
                                g2d.setColor(Color.red);
                            }
                            g2d.fillOval(currentState.getMallet_1().x - currentState.getMalletRadius(), 
                                    currentState.getMallet_1().y - currentState.getMalletRadius(), 
                                    2 * currentState.getMalletRadius(),
                                    2 * currentState.getMalletRadius());
                            //g2d.drawImage(yourMalletImg, currentState.getMallet_1().x - 90, currentState.getMallet_1().y - 65, 150, 100, null);
                        }

                        if (currentState.getMallet_2() != null) {
                            if (player_num == 2) {
                                g2d.setColor(Color.green);
                            } else {
                                g2d.setColor(Color.red);
                            }
                            g2d.fillOval(currentState.getMallet_2().x - currentState.getMalletRadius(), 
                                    currentState.getMallet_2().y - currentState.getMalletRadius(), 
                                    2 * currentState.getMalletRadius(), 
                                    2 * currentState.getMalletRadius());
                            //g2d.drawImage(yourMalletImg, message.getMallet_2().x - 10, message.getMallet_2().y - 10, 150, 100, null);
                        }

                        if (currentState.getPuck() != null) {
                            g2d.setColor(Color.yellow);   
                            g2d.fillOval(currentState.getPuck().x - currentState.getMalletRadius(), 
                                    currentState.getPuck().y - currentState.getMalletRadius(), 
                                    2 * currentState.getMalletRadius(), 
                                    2 * currentState.getMalletRadius());
                            //g2d.drawImage(puckImg, message.getPuck().x - 50, message.getPuck().y - 50, 100, 100, null);
                        }
                        
		}
                
                @Override
                public void mouseDragged( MouseEvent e )
                {
                    if (mallet != null) {
                        if (Math.abs(mallet.x - e.getX()) < currentState.getMalletRadius() && Math.abs(mallet.y - e.getY()) < currentState.getMalletRadius()) {
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
                        return Math.min(oldX, r.width - currentState.getMalletRadius());
                    }
                }
                
                public int getClippedY( int oldY, Rectangle r)
                {
                    int delta = 0;
                    
                    if (player_num == 2) {
                        delta = r.height / 2;
                    }

                    if (oldY <= delta + currentState.getMalletRadius()) {
                        return delta + currentState.getMalletRadius();
                    } else {
                        if (player_num == 1) {
                            delta = r.height / 2;
                        } else {
                            delta = 0;
                        }
                        return Math.min(oldY, r.height - delta - currentState.getMalletRadius());
                    }
                }

                @Override
                public void mouseClicked( MouseEvent e ) {
                    logArea.append(e.getPoint().toString()+"\n");
                }
                
                // Unused Mouse Listener Methods
                @Override
                public void mousePressed( MouseEvent e ) {}
                @Override
                public void mouseMoved( MouseEvent e ) {}
                @Override
                public void mouseReleased( MouseEvent e ) {}
                @Override
                public void mouseEntered( MouseEvent e ) {}
                @Override
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
        panel.setSize(new Dimension(400, 700));
        this.add(panel);
        setVisible(true);
        
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
        gameArea = new Rectangle(0, 0, 400, 700);
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
        lb_address = new javax.swing.JLabel();
        tf_address = new javax.swing.JTextField();
        lb_port = new javax.swing.JLabel();
        tf_port = new javax.swing.JTextField();
        b_disconnect = new javax.swing.JButton();
        nameTextField = new javax.swing.JTextField();
        startGameButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setPreferredSize(new java.awt.Dimension(410, 960));
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

        lb_address.setText("Address : ");

        tf_address.setText("localhost");
        tf_address.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tf_addressActionPerformed(evt);
            }
        });

        lb_port.setText("Port :");

        tf_port.setText("2222");
        tf_port.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tf_portActionPerformed(evt);
            }
        });

        b_disconnect.setText("Disconnect");
        b_disconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_disconnectActionPerformed(evt);
            }
        });

        nameTextField.setText("Your name");

        startGameButton.setText("Start");
        startGameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startGameButtonActionPerformed(evt);
            }
        });

        jButton2.setText("Pause");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(startGameButton, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(31, 31, 31)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(connect, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(b_disconnect, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(29, 29, 29)
                        .addComponent(lb_address, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tf_address, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19)
                        .addComponent(lb_port, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tf_port, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(578, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startGameButton)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connect)
                    .addComponent(lb_address)
                    .addComponent(tf_address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lb_port)
                    .addComponent(tf_port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(b_disconnect)
                .addGap(4, 4, 4))
        );

        startGameButton.getAccessibleContext().setAccessibleName("startGameButton");
        jButton2.getAccessibleContext().setAccessibleName("pauseButton");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectActionPerformed
        if (isConnected == false) 
        {

            try 
            {
                if (tf_address.getText().equals("localhost")) {
                    ip = InetAddress.getLocalHost();
                } else {
                    ip = InetAddress.getByName(tf_address.getText());
                }
                port = Integer.valueOf(tf_port.getText());
                
                socket = new Socket(ip, port);
 
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
                
                if (player_num == -1) {
                      player_num = ((State)inputStream.readObject()).getPlayerNum();
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

    private void tf_addressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tf_addressActionPerformed

    }//GEN-LAST:event_tf_addressActionPerformed

    private void tf_portActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tf_portActionPerformed

    }//GEN-LAST:event_tf_portActionPerformed

    private void b_disconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_disconnectActionPerformed
        sendDisconnect();
        Disconnect();
    }//GEN-LAST:event_b_disconnectActionPerformed

    private void startGameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startGameButtonActionPerformed
        if (player_num == 1) {
            mallet = new Point(gameArea.width / 2, 50);
            currentState.setMallet_1(mallet);
        } else {
            mallet = new Point(gameArea.width / 2, gameArea.height - 50);
            currentState.setMallet_2(mallet);
        }

        currentState.setPuck(new Point(gameArea.width / 2, gameArea.height / 2));
    }//GEN-LAST:event_startGameButtonActionPerformed

    public void sendDisconnect() 
    {
        try
        {
            currentState.setDisconnectedPlayer(player_num);
            outputStream.reset();
            outputStream.writeObject(currentState);
        } catch (Exception e) 
        {
            logArea.append("Could not send Disconnect message.\n");
        }
    }
    
    public void Disconnect()
    {
        try 
        {
            logArea.append("Disconnected.\n");
            incomingReader.stop();
            socket.close();
        } catch(Exception ex) {
            logArea.append("Failed to disconnect. \n");
        }
        isConnected = false;
    }
    
    public void sendName(String name) 
    {
        try
        {
            currentState.setPlayerName(name);
            outputStream.reset();
            outputStream.writeObject(currentState);
        } catch (Exception e) 
        {
            logArea.append("Could not send sendName message.\n");
        }
    }
    
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
    private javax.swing.JButton b_disconnect;
    private javax.swing.JButton connect;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lb_address;
    private javax.swing.JLabel lb_port;
    private javax.swing.JTextArea logArea;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton startGameButton;
    private javax.swing.JTextField tf_address;
    private javax.swing.JTextField tf_port;
    // End of variables declaration//GEN-END:variables
}
