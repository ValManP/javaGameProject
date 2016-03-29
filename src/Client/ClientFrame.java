/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.io.*;
import java.lang.Math;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
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
    
    int num_player = -1;
    
    Message message;
    
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    
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
                //inputStream = new ObjectInputStream(socket.getInputStream());
           
                while (inputStream != null)
                {
                    Message m = (Message)inputStream.readObject();
//                    if (num_player == 1) {
//                        message.setMallet_2(m.getMallet_2());
//                    } else {
//                        message.setMallet_1(m.getMallet_1());
//                    }

                    if (m.getMallet_1() != null) {
                        message.setMallet_1(m.getMallet_1());
                    }
                    if (m.getMallet_2() != null) {
                        message.setMallet_2(m.getMallet_2());
                    }
                    if (m.getPuck() != null) {
                        message.setPuck(m.getPuck());
                    }
                   
                    //mallet = new Point(200, 300);
                    
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
                        
                        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
 
                        if (message.getMallet_1() != null) {
                            g2d.fillOval(message.getMallet_1().x - 10, message.getMallet_1().y - 10, 20, 20);
                        }

                        if (message.getMallet_2() != null) {
                            g2d.fillOval(message.getMallet_2().x - 10, message.getMallet_2().y - 10, 20, 20);
                        }
                        
                        if (message.getPuck() != null) {
                            g2d.setColor(Color.red);   
                            g2d.fillOval(message.getPuck().x - 10, message.getPuck().y - 10, 20, 20);
                        }
		}
                
                public void mouseDragged( MouseEvent e )
                {
                    if (Math.abs(mallet.x - e.getX()) < 10 && Math.abs(mallet.y - e.getY()) < 10) {
                        mallet.x = e.getX();
                        mallet.y = e.getY();
                        if (num_player == 1) {
                            message.setMallet_1(mallet);
                        } else {
                            message.setMallet_2(mallet);
                        }
                        if (outputStream != null) {
                            try {
                                outputStream.reset();
                                outputStream.writeObject(message);
                            } catch (IOException ex) {
                                Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
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
                public void mouseClicked( MouseEvent e ) {}
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
        this.pack();
        setVisible(true);
        this.setLocationRelativeTo(null);
        
        message = new Message();
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
                    .addComponent(connect)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(405, Short.MAX_VALUE)
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
                
                if (num_player == -1) {
                      num_player = ((Message)inputStream.readObject()).getNumPlayer();
                }
                
                if (num_player == 1) {
                    mallet = new Point(100, 200);
                    message.setMallet_1(mallet);
                } else {
                    mallet = new Point(200, 200);
                    message.setMallet_2(mallet);
                }
                
                
                isConnected = true; 
                        
                logArea.append("Connected to the server\n");
                logArea.append("You are player #"+num_player+"\n");
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
