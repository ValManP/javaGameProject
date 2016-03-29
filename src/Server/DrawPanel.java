/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Client.Message;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author pozdv
 */
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
    }
