package model.physics;

import model.physics.Circle2f;

// Класс щетка
public class Mallet extends Circle2f
{
    public boolean captured;

    public Mallet(float x, float y, float r)
    {
        super(x, y, r, 1.0f);
        captured = false;
    }

    public void setNextPosition(float x, float y, float dt)
    {
        Vector2f newPosition = new Vector2f(x, y);

        dX = newPosition.sub(position);

        velocity = dX.dev(dt);
    }

    @Override
    public void moveToNextPosition(float k)
    {
        // Изменение положения
        dX = dX.mult(k);
        position = position.add(dX);

        // Обнулить вектор перемещения и изменения скорости (перемещение совершено)
        dX = new Vector2f();
        //dV = new Vector2f();
    }
}
