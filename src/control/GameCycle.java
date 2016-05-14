/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import model.physics.Circle2f;
import model.physics.Mallet;
import model.physics.Vector2f;
import model.physics.Wall2f;
import java.awt.Dimension;
import java.awt.Point;

/**
 *
 * @author pozdv
 */
public class GameCycle {
    model.physics.State state;
    
    Point leftCorner;
    Dimension size;
    
    // Игровые объекты
    Wall2f[] walls;
    Mallet[] mallets;
    Circle2f puck;
        
    // Скорость игры
    float dt = 0.015f;
    // Максимальная скорость передвижения
    float maxVelocity = 1000.0f;
    
    // Время начала предыдущей итерации
    long previousTime;
    // Текущее время
    long currentTime;

    public GameCycle(model.physics.State m) {
        // Время предыдущей итерации = время инициализации класса
        previousTime = System.nanoTime();
        currentTime = previousTime;
        
        state = m;

        leftCorner = new Point(0, 0);
        size = new Dimension(400, 700);
        
        walls = new Wall2f[8];
        // Верхняя
        walls[0] = new Wall2f(leftCorner.x, leftCorner.y, leftCorner.x + size.width / 3, leftCorner.y);
        walls[1] = new Wall2f(leftCorner.x + size.width / 3, leftCorner.y, leftCorner.x + size.width * 2 / 3, leftCorner.y); // Верхние ворота
        walls[2] = new Wall2f(leftCorner.x + size.width * 2 / 3, leftCorner.y, leftCorner.x + size.width, leftCorner.y);
        // Правая
        walls[3] = new Wall2f(leftCorner.x + size.width, leftCorner.y, leftCorner.x + size.width, leftCorner.y + size.height);
        // Нижняя
        walls[4] = new Wall2f(leftCorner.x + size.width, leftCorner.y + size.height, leftCorner.x + size.width * 2 / 3, leftCorner.y + size.height);
        walls[5] = new Wall2f(leftCorner.x + size.width * 2 / 3, leftCorner.y + size.height, leftCorner.x + size.width / 3, leftCorner.y + size.height); // Нижние ворота
        walls[6] = new Wall2f(leftCorner.x + size.width / 3, leftCorner.y + size.height, leftCorner.x, leftCorner.y + size.height);
        // Левая
        walls[7] = new Wall2f(leftCorner.x, leftCorner.y + size.height, leftCorner.x, leftCorner.y);

        mallets = new Mallet[2];
        mallets[0] = new Mallet(size.width/2, 50, 30);
        mallets[1] = new Mallet(size.width/2, size.height-50, 30);
        puck = new Circle2f(size.width/2, size.height/2, 20, 50);
        
        state.setPuck(new Point(size.width/2, size.height/2));
    }
    
    public model.physics.State calculate(model.physics.State oldState) {
        // Засекаем время, прошедшее с момента предыдущей итерации
        currentTime = System.nanoTime();
        dt = (currentTime - previousTime) / 1000000000.0f;
        previousTime = currentTime;
        
        // Physics calculation
        process(oldState.getMallet_1(), oldState.getMallet_2());
        
        currentTime = System.nanoTime();
        
        setPuckPosition(new Point((int)puck.getPosition().X, (int)puck.getPosition().Y));
        setMallet1Position(new Point((int)mallets[0].getPosition().X, (int)mallets[0].getPosition().Y));
        setMallet2Position(new Point((int)mallets[1].getPosition().X, (int)mallets[1].getPosition().Y));

        return state;
    }
    
    private synchronized Point getMallet1Position(){
        return state.getMallet_1();
    }
    
    private synchronized Point getMallet2Position(){
        return state.getMallet_2();
    }
    
    private synchronized void setPuckPosition(Point p){
        state.setPuck(p);
    }
    
    private synchronized void setMallet1Position(Point p){
        state.setMallet_1(p);
    }
    
    private synchronized void setMallet2Position(Point p){
        state.setMallet_2(p);
    }
    
    void process(Point m1, Point m2)
    {
        // Считать новые координаты мышки и попытаться передвинуть туда щетку
        if (m1 != null) {
            mallets[0].setNextPosition(m1.x, m1.y, 0.2f);
            //mallets[0].correctVelocity(0.9f * maxVelocity);
        }

        if (m2 != null) {
            mallets[1].setNextPosition(m2.x, m2.y, 0.2f);
            //mallets[1].correctVelocity(0.9f * maxVelocity);
        }

        // Посчитать, куда должна будет переместиться шайба
        puck.calculateNextPosition(dt);

            // Переменная, в которой будут храниться результаты проверки на стокновение
            Circle2f.WillCollideWrapper collision;

        // Ближайшая стена, с которой столкнется шайба
        Wall2f wall_to_collide = null;
        float min_k_wall = 1.0f;
        for (int i = 0; i < 8; ++i)
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
        for (int i = 0; i < 2; ++i)
        {
            collision = puck.willCollide(mallets[i]);  // Проверка на столкновение с щеткой 1 игрока
            if (collision.willCollide)
                if (collision.k <= min_k_mallet)
                {
                    mallet_to_collide = mallets[i];
                    min_k_mallet = collision.k;
                }
        }

        // Смотрим, с чем столкновении произойдет быстрее
        // С этим предметом и обрабатываем столкновение
        if (min_k_wall < min_k_mallet)  // Шайба сталкивается со стеной
        {
            puck.moveToNextPosition(min_k_wall);
            
            // Обработка гола
            if (wall_to_collide == walls[1]) {
                handleGoal(true);
            }
            if (wall_to_collide == walls[5])  {
                handleGoal(false);
            }
            
            puck.collideWithWall(wall_to_collide);

            mallets[0].moveToNextPosition();
            mallets[1].moveToNextPosition();
        }
        if (min_k_mallet < min_k_wall)  // Шайба сталкивается с щеткой
        {
            puck.moveToNextPosition(min_k_mallet);

            puck.collideWithInfMass(mallet_to_collide);
            //puck.correctVelocity(maxVelocity);

            mallet_to_collide.moveToNextPosition(min_k_mallet);

            // Передвинуть щетку, с коротой шайба не столкнулась
            if (mallet_to_collide == mallets[0])
                mallets[1].moveToNextPosition();
            else
                mallets[0].moveToNextPosition();
        }
        if (min_k_wall == min_k_mallet)  // Шайба сталкивается и с щеткой, и со стеной, или ни с чем
        {
            puck.moveToNextPosition(min_k_wall);

            if (wall_to_collide != null)
            {
                // Обработка гола
                if (wall_to_collide == walls[1]) {
                    handleGoal(true);
                }
                if (wall_to_collide == walls[5]) {
                    handleGoal(false);
                }
                
                puck.collideWithWall(wall_to_collide);
            }
            if (mallet_to_collide != null)
                puck.collideWithInfMass(mallet_to_collide);
                puck.correctVelocity(maxVelocity);

            mallets[0].moveToNextPosition(min_k_wall);
            mallets[1].moveToNextPosition(min_k_wall);
        }

        /* ЭПИЧНЫЙ КОСТЫЛЬ */
        for (int i = 0; i < 2; ++i)
        {
            // Насколько щетка заехала на шайбу
            float dist = puck.getRadius() + mallets[i].getRadius() - puck.getPosition().sub(mallets[i].getPosition()).magnitude();
            // Попытаться вывести шайбу из-под щетки, увеличив скорость шайбы
            if (dist > 1.0f)
            {
                // Если щетка прижала шайбу к стене - отодвинуть шайбу вдоль стены
                if (min_k_wall <= 0.00001f)
                {
                    // Направление щетки на шайбу
                    Vector2f view = puck.getPosition().sub(mallets[i].getPosition());
                    // Вектор, показывающий, куда нужно оттолкнуть шайбу
                    Vector2f N = wall_to_collide.getDirection();

                    puck.stop();
                    puck.pushAwayFrom(mallets[i]);

                    puck.addVelocity(N.mult(2.0f * dist * Math.signum(view.dot(N))));
                    //puck.correctVelocity(maxVelocity);
                }
                else
                {
                    // Вектор, показывающий, куда нужно оттолкнуть шайбу
                    Vector2f N = puck.getPosition().sub(mallets[i].getPosition()).normalize();

                    puck.pushAwayFrom(mallets[i]);

                    puck.addVelocity(N.mult(dist));
                    //puck.correctVelocity(maxVelocity);
                }
            }
        }
    }
    
    public long getElapsedNanoTime()
    {
        return currentTime - previousTime;
    }
    
    private void handleGoal(boolean isFirst) {
        if (isFirst) {
            this.state.firstScore++;
            puck = new Circle2f(size.width/2, size.height/2 - 100, 20, 50);
        } else {
            this.state.secondScore++;
            puck = new Circle2f(size.width/2, size.height/2 + 100, 20, 50);
        }
        
    }
}
