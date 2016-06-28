package model.physics;

import java.awt.Point;

// Класс вектор
public class Vector2f
{
    public float X;
    public float Y;

    // Конструктор по умолчанию
    public Vector2f()
    {
        X = 0.0f;
        Y = 0.0f;
    }

    // Конструктор
    public Vector2f(float x, float y)
    {
        X = x;
        Y = y;
    }

    // Конструктор копирования
    public Vector2f(Vector2f other)
    {
        X = other.X;
        Y = other.Y;
    }

    // Сложение векторов
    public Vector2f add(Vector2f other)
    {
        return new Vector2f(X + other.X, Y + other.Y);
    }

    // Вычитание векторов
    public Vector2f sub(Vector2f other)
    {
        return new Vector2f(X - other.X, Y - other.Y);
    }

    // Скалярное произведение
    public float dot(Vector2f other)
    {
        return (X * other.X + Y * other.Y);
    }

    // Умножение вектора на число
    public Vector2f mult(float a)
    {
        return new Vector2f(X * a, Y * a);
    }

    // Частное двух векторов = скалярное произведение / скалярный квадрат делителя
    public float dev(Vector2f other)
    {
        return dot(other) / other.sqr();
    }

    // Деление вектора на число
    public Vector2f dev(float a)
    {
        return new Vector2f(X / a, Y / a);
    }

    // Скалярный квадрат вектора
    public float sqr()
    {
        return X * X + Y * Y;
    }

    // Длина вектора
    public float magnitude()
    {
        return (float)Math.sqrt(sqr());
    }

    // Вернуть нормированный вектор
    public Vector2f normalize()
    {
        float mag = magnitude();
        if (mag != 0.0f)
            return dev(mag);
        else
            return this;
    }
    
    // Вернуть как объект Point
    public Point toPoint()
    {
        return new Point((int)X, (int)Y);
    }
}
