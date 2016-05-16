package view;

import model.physics.AirHockeyState;
import model.physics.Physics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
    private int port;
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
    private AirHockeyState currentState;
    private AirHockeyState incomingState;
    private int player_num = -1;
    
    //--------------------------//
    /**
     * Input listener
     */
    public class IncomingReader implements Runnable
    {
        @Override
        public void run() {
            try {
                while (inputStream != null) {
                    incomingState = (AirHockeyState)inputStream.readObject();

                    if (incomingState.getMallet1() != null) {
                        currentState.setMallet1(incomingState.getMallet1());
                    }
                    if (incomingState.getMallet2() != null) {
                        currentState.setMallet2(incomingState.getMallet2());
                    }
                    if (incomingState.getPuck() != null) {
                        currentState.setPuck(incomingState.getPuck());
                    }
                    
                    if (incomingState.isGame && !currentState.isGame) {
                        startGame();
                        currentState.isGame = incomingState.isGame;
                    }
                    
                    handleScore(incomingState);
                    
                    if (incomingState.isDisconnected()) {
                        break;
                    }
                }
                
                Disconnect();
             } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void ListenThread() {
        incomingReader = new Thread(new IncomingReader());
        incomingReader.start();
    }
    
    private void handleScore(AirHockeyState incomingState) {
        currentState.firstScore = incomingState.firstScore;
        currentState.secondScore = incomingState.secondScore;
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
            setLocation(10, 10);
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
            return (player_num == 1) ? gameArea.width - coord : coord;
        }
        
        // Отразить координату Y для первого игрока
        private int getCoordinateY(int coord) {
            return (player_num == 1) ? gameArea.height - coord : coord;
        }

        @Override
        public void paint(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            g2d.drawImage(fieldImg, 0, 0, Physics.Field.width, Physics.Field.height, null);

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (currentState.getMallet1() != null) {
                g2d.drawImage(
                    (player_num == 1) ? yourMalletImg : enemyMalletImg,
                    getCoordinateX(currentState.getMallet1().x) - (int)Physics.MalletRadius,
                    getCoordinateY(currentState.getMallet1().y) - (int)Physics.MalletRadius,
                    2 * (int)Physics.MalletRadius,
                    2 * (int)Physics.MalletRadius,
                    null
                );
            }

            if (currentState.getMallet2() != null) {
                g2d.drawImage(
                    (player_num == 2) ? yourMalletImg : enemyMalletImg,
                    getCoordinateX(currentState.getMallet2().x) - (int)Physics.MalletRadius,
                    getCoordinateY(currentState.getMallet2().y) - (int)Physics.MalletRadius,
                    2 * (int)Physics.MalletRadius,
                    2 * (int)Physics.MalletRadius,
                    null
                );
            }

            if (currentState.getPuck() != null) {
                g2d.drawImage(
                    puckImg, 
                    getCoordinateX(currentState.getPuck().x) - (int)Physics.PuckRadius,
                    getCoordinateY(currentState.getPuck().y) - (int)Physics.PuckRadius,
                    2 * (int)Physics.PuckRadius,
                    2 * (int)Physics.PuckRadius,
                    null
                );
            }

            Font font = new Font("Courier", Font.BOLD, 30);

            g2d.setFont(font);
            g2d.setColor(Color.CYAN);
            g2d.drawString(String.valueOf(currentState.secondScore), Physics.Field.width - 35, Physics.Field.height / 2 - 20);
            g2d.drawString(String.valueOf(currentState.firstScore), Physics.Field.width - 35, Physics.Field.height / 2 + 40);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (mallet != null && currentState.isGame) {
                if ((Math.abs(getCoordinateX(mallet.x)- e.getX()) < Physics.MalletRadius) &&
                    (Math.abs(getCoordinateY(mallet.y) - e.getY()) < Physics.MalletRadius)) {
                    
                    mallet.x = getClippedX(getCoordinateX(e.getX()), gameArea);
                    mallet.y = getClippedY(getCoordinateY(e.getY()), gameArea);
                    
                    if (player_num == 1) {
                        currentState.setMallet1(mallet);
                    } else {
                        currentState.setMallet2(mallet);
                    }
                    
                    if (outputStream != null && !socket.isClosed()) {
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

        public int getClippedX(int oldX, Rectangle rect) {
            if (oldX <= Physics.MalletRadius) {
                return (int)Physics.MalletRadius;
            } else {
                return Math.min(oldX, rect.width - (int)Physics.MalletRadius);
            }
        }

        public int getClippedY(int oldY, Rectangle rect) {
            int delta = 0;

            if (player_num == 2) {
                delta = rect.height / 2;
            }

            if (oldY <= delta + Physics.MalletRadius) {
                return delta + (int)Physics.MalletRadius;
            } else {
                if (player_num == 1) {
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

    //--------------------------//
    /**
     * Creates new form ClientFrame
     */
    public ClientFrame() {
        initComponents();
        
        DrawPanel panel = new DrawPanel();
        panel.setBackground(Color.white);
        panel.setSize(Physics.Field);
        this.add(panel);
        setVisible(true);
        
        try {
            fieldImg = ImageIO.read(new File("src/image/field.jpg"));
            yourMalletImg = ImageIO.read(new File("src/image/yourMallet.png"));
            enemyMalletImg = ImageIO.read(new File("src/image/enemyMallet.png"));
            puckImg = ImageIO.read(new File("src/image/puck.png"));
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        currentState = new AirHockeyState();
        incomingState = new AirHockeyState();
        gameArea = new Rectangle(Physics.Field);
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

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

        startGameButton.setText("READY");
        startGameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startGameButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(320, 320, 320)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameTextField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tf_address)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lb_address, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tf_port, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lb_port, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(connect, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                                .addComponent(b_disconnect)))
                        .addGap(10, 10, 10))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startGameButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb_address)
                    .addComponent(lb_port))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tf_address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tf_port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connect)
                    .addComponent(b_disconnect))
                .addGap(18, 18, 18)
                .addComponent(startGameButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(90, Short.MAX_VALUE))
        );

        startGameButton.getAccessibleContext().setAccessibleName("startGameButton");

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
                
                sendName();
                
                if (player_num == -1) {
                      player_num = ((AirHockeyState)inputStream.readObject()).getPlayerNum();
                }
                
                isConnected = true; 
                        
                logArea.append("Connected to the server.\n");
                logArea.append("You are player #" + player_num + ".\n");
            } 
            catch (Exception ex) {
                logArea.append("Cannot Connect! Try Again.\n");
            }
            
            ListenThread();
        } else
            if (isConnected == true) {
                logArea.append("You are already connected.\n");
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
        changeGameStatus(true);
    }//GEN-LAST:event_startGameButtonActionPerformed

    private void changeGameStatus(boolean status) {
        if (player_num == 1) {
            mallet = new Point(gameArea.width / 2, (int)Physics.MalletRadius + 10);
            currentState.setMallet1(mallet);
            currentState.isFirstReady = status;
        } else {
            mallet = new Point(gameArea.width / 2, gameArea.height - (int)Physics.MalletRadius - 10);
            currentState.setMallet2(mallet);
            currentState.isSecondReady = status;
        }

        currentState.setPuck(new Point(gameArea.width / 2, gameArea.height / 2));
        sendMes();
    }
    
    public void sendDisconnect() {
        try {
            currentState.setDisconnectedPlayer(player_num);
            outputStream.reset();
            outputStream.writeObject(currentState);
            currentState.setDisconnectedPlayer(0);
        } catch (Exception e) {
            logArea.append("Could not send Disconnect message.\n");
        }
    }
    
    public void Disconnect() {
        try  {
            isConnected = false;
            logArea.append("Disconnected.\n");
            incomingReader.stop();
            socket.close();
        } catch(Exception ex) {
            logArea.append("Failed to disconnect. \n");
        }
    }
    
    public String getName() {
        if (!nameTextField.getText().equals("Your name")
                && !nameTextField.getText().equals("")) {
            return nameTextField.getText();
        } else {
            return "Anonym";
        } 
    }
    
    public void sendName() {
        try {
            currentState.setPlayerName(getName());
            outputStream.reset();
            outputStream.writeObject(currentState);
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendMes() {
        try {
            outputStream.reset();
            outputStream.writeObject(currentState);
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void startGame() {
        try {
            logArea.append("Game will start in ");
            for (int i = 5; i > 0; i--) {
                logArea.append(i + "...");
                Thread.sleep(1000);
            }
            logArea.append("\nStart!");
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
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
