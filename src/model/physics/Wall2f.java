/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.physics;

import model.physics.Vector2f;
import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author frim
 */

// Стена от точки A до B
public class Wall2f
{
    public Vector2f A;
    public Vector2f B;

    // Конструктор по умолчанию
    public Wall2f()
    {
        A = new Vector2f(0.0f, 0.0f);
        B = new Vector2f(0.0f, 0.0f);
    }

    // Конструктор
    public Wall2f(float _AX, float _AY, float _BX, float _BY)
    {
        A = new Vector2f(_AX, _AY);
        B = new Vector2f(_BX, _BY);
    }

    // Нарисовать стену
    public void draw(Graphics g, Color color)
    {
        g.setColor(color);
        g.drawLine((int)A.X, (int)A.Y, (int)B.X, (int)B.Y);
    }

    // Получить направление стены (единичный вектор из A в B)
    public Vector2f getDirection()
    {
        return B.sub(A).normalize();
    }
}
