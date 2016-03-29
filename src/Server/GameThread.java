/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Client.Message;
import Physics.*;
import java.awt.Dimension;
import java.awt.Point;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author pozdv
 */
public class GameThread extends Thread {

    Message message;
    
    Point leftCorner;
    Dimension size;
    
    // Игровые объекты
        Wall2f[] walls;
        Mallet mallet1;
        Mallet mallet2;
        Circle2f puck;
        
        // Скорость игры
        float dt = 0.5f;
        
        ServerThread st;
    
    public GameThread(Message m, ServerThread thr) {
        message = m;
        
        st = thr;
        
        leftCorner = new Point(0, 0);
        size = new Dimension(300, 400);
        
        walls = new Wall2f[4];
        walls[0] = new Wall2f(leftCorner.x, leftCorner.y, leftCorner.x + size.width, leftCorner.y);
        walls[1] = new Wall2f(leftCorner.x + size.width, leftCorner.y, leftCorner.x + size.width, leftCorner.y + size.height);
        walls[2] = new Wall2f(leftCorner.x + size.width, leftCorner.y + size.height, leftCorner.x, leftCorner.y + size.height);
        walls[3] = new Wall2f(leftCorner.x, leftCorner.y + size.height, leftCorner.x, leftCorner.y);

        mallet1 = new Mallet(message.getMallet_1().x, message.getMallet_1().y, 20);
        mallet2 = new Mallet(message.getMallet_2().x, message.getMallet_2().y, 20);
        puck = new Circle2f(50, 50, 20, 1);
        
        message.setPuck(new Point(50, 50));
    }
    
    @Override
    public void run() {
        
        boolean f = true;
        while(f) {
            mallet1.moveTo(getMallet1Position().x, getMallet1Position().y, dt);
            //mallet1.moveToNextPosition(1.0f);
            //mallet2.moveTo(getMallet2Position().x, getMallet2Position().y, dt);

            //puck.calculateNextPosition(dt);
            //puck.moveToNextPosition(1.0f);

            // Найти ближайшее столкновение шайбы со стенами
                puck.calculateNextPosition(dt);
                Wall2f wall_to_collide = null;
                Float k = 1.0f, min_k_wall = 1.0f;
                for (int i = 0; i < 4; ++i)
                    if (puck.willCollide(walls[i], k))
                        if (k < min_k_wall)
                        {
                            wall_to_collide = walls[i];
                            min_k_wall = k;
                        }
    
                Mallet mallet_to_collide = null;
                float min_k_mallet = 1.0f;
                if (puck.willCollide(mallet1, k))
                    if (k < min_k_mallet)
                    {
                        st.addToLog(message.getMallet_1().toString());
                        mallet_to_collide = mallet1;
                        min_k_mallet = k;
                    }
    
                if (min_k_wall < min_k_mallet)
                {
                    puck.moveToNextPosition(min_k_wall);
                    // Обработать столкновение с ближайшей стеной
                    if (wall_to_collide != null)
                        puck.collideWithWall(wall_to_collide);
    
                    mallet1.moveToNextPosition(1.0f);
                }
                else
                {
                    puck.moveToNextPosition(min_k_mallet);
                    if (mallet_to_collide != null)
                    {
                        puck.collideWithInfMass(mallet_to_collide);
                        puck.correctVelocity(100);
                        mallet_to_collide.moveToNextPosition(min_k_mallet);
                    }
                    else
                    {
                        mallet1.moveToNextPosition(1.0f);
                        //mallet2.moveToNextPosition(1.0f);
                    }
                }
               

            setPuckPosition(new Point((int)puck.getPosition().X, (int)puck.getPosition().Y));
            setMallet1Position(new Point((int)mallet1.getPosition().X, (int)mallet1.getPosition().Y));
            //setPuckPosition(new Point((int)puck.getPosition().X, (int)puck.getPosition().Y));

            //st.addToLog(message.getPuck().toString());
            
            try {
                GameThread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(GameThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
            
      
    }
    
    private synchronized Point getMallet1Position(){
        return message.getMallet_1();
    }
    
    private synchronized Point getMallet2Position(){
        return message.getMallet_2();
    }
    
    private synchronized void setPuckPosition(Point p){
        message.setPuck(p);
    }
    
    private synchronized void setMallet1Position(Point p){
        message.setMallet_1(p);
    }
    
}
