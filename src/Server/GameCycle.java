/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Physics.Circle2f;
import Physics.Mallet;
import Physics.Vector2f;
import Physics.Wall2f;
import java.awt.Dimension;
import java.awt.Point;

/**
 *
 * @author pozdv
 */
public class GameCycle {
    Client.State state;
    
    Point leftCorner;
    Dimension size;
    
    // Игровые объекты
    Wall2f[] walls;
    Mallet mallet1;
    Mallet mallet2;
    Circle2f puck;
        
    // Скорость игры
    float dt = 0.015f;
    // Максимальная скорость передвижения
    float maxVelocity = 150.0f;

    public GameCycle(Client.State m) {
        state = m;

        leftCorner = new Point(0, 0);
        size = new Dimension(300, 400);
        
        walls = new Wall2f[4];
        walls[0] = new Wall2f(leftCorner.x, leftCorner.y, leftCorner.x + size.width, leftCorner.y);
        walls[1] = new Wall2f(leftCorner.x + size.width, leftCorner.y, leftCorner.x + size.width, leftCorner.y + size.height);
        walls[2] = new Wall2f(leftCorner.x + size.width, leftCorner.y + size.height, leftCorner.x, leftCorner.y + size.height);
        walls[3] = new Wall2f(leftCorner.x, leftCorner.y + size.height, leftCorner.x, leftCorner.y);

        mallet1 = new Mallet(300, 300, 20);
        mallet2 = new Mallet(300, 300, 20);
        puck = new Circle2f(50, 50, 20, 4);
        
        state.setPuck(new Point(50, 50));
    }
    
    public Client.State calculate(Client.State oldState) {
        
        
        // Physics calculation
        process(oldState.getMallet_1(), oldState.getMallet_2());
        
        setPuckPosition(new Point((int)puck.getPosition().X, (int)puck.getPosition().Y));
        setMallet1Position(new Point((int)mallet1.getPosition().X, (int)mallet1.getPosition().Y));
        setMallet2Position(new Point((int)mallet2.getPosition().X, (int)mallet2.getPosition().Y));

        return state;
    }
    
    private synchronized Point getMallet1Position() {
        return state.getMallet_1();
    }
    
    private synchronized Point getMallet2Position() {
        return state.getMallet_2();
    }
    
    private synchronized void setPuckPosition(Point p) {
        state.setPuck(p);
    }
    
    private synchronized void setMallet1Position(Point p) {
        state.setMallet_1(p);
    }
    
    private synchronized void setMallet2Position(Point p) {
        state.setMallet_2(p);
    }
    
    void process(Point m1, Point m2)
    {
        // Считать новые координаты мышки и попытаться передвинуть туда щетку
        if (m1 != null) {
            mallet1.setNextPosition(m1.x, m1.y, dt);
            mallet1.correctVelocity(0.9f * maxVelocity);
        }

        if (m2 != null) {
            mallet2.setNextPosition(m2.x, m2.y, dt);
            mallet2.correctVelocity(0.9f * maxVelocity);
        }

        // Посчитать, куда должен будет переместиться шайба
        puck.calculateNextPosition(dt);

        // Переменная, в которой будут храниться результаты проверки на стокновение
        Circle2f.WillCollideWrapper collision;

        // Ближайшая стена, с которой столкнется шайба
        Wall2f wall_to_collide = null;
        float min_k_wall = 1.0f;
        for (int i = 0; i < 4; ++i)
        {
            collision = puck.willCollide(walls[i]);
            if (collision.willCollide)
                if (collision.k <= min_k_wall)
                {
                    wall_to_collide = walls[i];
                    min_k_wall = collision.k;
                }
        }

        // Ближайшая щетка, с которой столкнется шайба
        Mallet mallet_to_collide = null;
        float min_k_mallet = 1.0f;

        collision = puck.willCollide(mallet1);  // Проверка на столкновение с щеткой 1 игрока
        if (collision.willCollide)
        {
            if (collision.k <= min_k_mallet)
            {
                mallet_to_collide = mallet1;
                min_k_mallet = collision.k;
            }
        }
        collision = puck.willCollide(mallet2);  // Проверка на столкновение с щеткой 2 игрока
        if (collision.willCollide)
        {
            if (collision.k <= min_k_mallet)
            {
                mallet_to_collide = mallet2;
                min_k_mallet = collision.k;
            }
        }

        // Смотрим, с чем столкновении произойдет быстрее
        // С этим предметом и обрабатываем столкновение
        if (min_k_wall < min_k_mallet)  // Шайба сталкивается со стеной
        {
            puck.moveToNextPosition(min_k_wall);
            puck.collideWithWall(wall_to_collide);

            mallet1.moveToNextPosition();
            mallet2.moveToNextPosition();
        }
        if (min_k_mallet < min_k_wall)  // Шайба сталкивается с щеткой
        {
            puck.moveToNextPosition(min_k_mallet);

            puck.collideWithInfMass(mallet_to_collide);
            puck.correctVelocity(maxVelocity);

            mallet_to_collide.moveToNextPosition(min_k_mallet);

            // Передвинуть щетку, с коротой шайба не столкнулась
            if (mallet_to_collide == mallet1)
            {
                mallet2.moveToNextPosition();
            }
            else
                mallet1.moveToNextPosition();
        }
        if (min_k_wall == min_k_mallet)  // Шайба сталкивается и с щеткой, и со стеной, или ни с чем
        {
            puck.moveToNextPosition(min_k_wall);

            if (wall_to_collide != null)
                puck.collideWithWall(wall_to_collide);
            if (mallet_to_collide != null)
                puck.collideWithInfMass(mallet_to_collide);

            mallet1.moveToNextPosition(min_k_wall);
            mallet2.moveToNextPosition(min_k_wall);
        }

        // TODO: разобраться что тут происходит
        /* ЭПИЧНЫЙ КОСТЫЛЬ */
        // Насколько 1 щетка заехала на шайбу
        float dist1 = puck.getRadius() + mallet1.getRadius() - puck.getPosition().sub(mallet1.getPosition()).magnitude();
        // Попытаться вывести шайбу из-под щетки, увеличив скорость шайбы
        if (dist1 > 1.0f)
        {
            // Если щетка прижала шайбу к стене - отодвинуть шайбу вдоль стены
            if (min_k_wall == 0.0f)
            {
                // Направление щетки на шайбу
                Vector2f view = puck.getPosition().sub(mallet1.getPosition());
                // Вектор, показывающий, куда нужно оттолкнуть шайбу
                Vector2f N = wall_to_collide.getDirection();

                puck.stop();
                puck.addVelocity(N.mult(dist1 * Math.signum(view.dot(N))));
                puck.pushAwayFrom(mallet1);
            }
            else
            {
                // Вектор, показывающий, куда нужно оттолкнуть шайбу
                Vector2f N = puck.getPosition().sub(mallet1.getPosition()).normalize();
                puck.addVelocity(N.mult(dist1));
            }
        }
        // Насколько 2 щетка заехала на шайбу
        float dist2 = puck.getRadius() + mallet2.getRadius() - puck.getPosition().sub(mallet2.getPosition()).magnitude();
        // Попытаться вывести шайбу из-под щетки, увеличив скорость шайбы
        if (dist2 > 1.0f)
        {
            // Если щетка прижала шайбу к стене
            if (min_k_wall == 0.0f)
            {
                // Направление щетки на шайбу
                Vector2f view = puck.getPosition().sub(mallet2.getPosition());
                // Вектор, показывающий, куда нужно оттолкнуть шайбу
                Vector2f N = wall_to_collide.getDirection();

                puck.stop();
                puck.addVelocity(N.mult(dist2 * Math.signum(view.dot(N))));
                puck.pushAwayFrom(mallet1);
            }
            else
            {
                // Вектор, показывающий, куда нужно оттолкнуть шайбу
                Vector2f N = puck.getPosition().sub(mallet2.getPosition()).normalize();
                puck.addVelocity(N.mult(dist2));
            }
        }
    }
}
