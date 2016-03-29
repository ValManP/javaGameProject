/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Physics;

/**
 *
 * @author pozdv
 */
public class Vector2f {
        public float X;
        public float Y;

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
        public static Vector2f sum(Vector2f v1, Vector2f v2)
        {
            return new Vector2f(v1.X + v2.X, v1.Y + v2.Y);
        }

        // Вычитание векторов
        public static Vector2f negative(Vector2f v1, Vector2f v2)
        {
            return new Vector2f(v1.X - v2.X, v1.Y - v2.Y);
        }

        // Скалярное произведение
        public static float multiply(Vector2f v1, Vector2f v2)
        {
            return (v1.X * v2.X + v1.Y * v2.Y);
        }
        
        // Умножение вектора на число
        public static Vector2f multiplyWithNumber(Vector2f v, float a)
        {
            return new Vector2f(v.X * a, v.Y * a);
        }

        // Частное двух векторов = скалярное произведение / скалярный квадрат делителя
        public static float divide(Vector2f v1, Vector2f v2)
        {
            return Vector2f.multiply(v1, v2) / v2.sqr();
        }

        // Деление вектора на число
        public static Vector2f divideByNumber(Vector2f v, float a)
        {
            return new Vector2f(v.X / a, v.Y / a);
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
                return Vector2f.divideByNumber(this, magnitude());
            else
                return this;
        }
}
