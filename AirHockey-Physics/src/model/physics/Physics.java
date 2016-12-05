package model.physics;

import java.awt.Dimension;

// Класс с физическими константами
public class Physics
{
    // Коэффициент трения
    public static final float Mu = 0.3f;
    // Ускорение свободного падения
    public static final float G = 9.8065f;
    // Коэффициент упругости
    public static final float Restitution = 1f;
    // Скорость игры (для использования с таймером)
    // На разных компьютерах должна быть своя
    //public static final float dt = 0.04f;
    // ---------- Главные параметры игры ---------- //
    // Размер игрового поля
    public static final Dimension Field = new Dimension(300, 600);
    // Радиус щетки
    public static final float MalletRadius = 30.0f;
    // Радиус шайбы
    public static final float PuckRadius = 20.0f;
    // Масса шайбы
    public static final float PuckMass = 50.0f;
    // Добавка к скорости при наложении
    public static final float VelocityIncrease = 2.0f;
}
