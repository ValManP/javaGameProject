/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Physics;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author pozdv
 */
public class Circle2f {
            
            // Текущее местоположение круга
            protected Vector2f position;
            // Текущая скорость круга
            protected Vector2f velocity;
            // Текущее ускорение круга
            protected float acceleration;
            // Масса круга
            protected float mass;
            // Радиус круга
            protected float radius;

            // Текущее перемещение к новой точке
            protected Vector2f dX;
            
            public Vector2f getPosition() {
                return position;
            }
            

            // Конструктор
            public Circle2f(float x, float y, float r, float m) {
                position = new Vector2f(x, y);
                velocity = new Vector2f(0, 0);
                radius = r;
                mass = m;

                dX = new Vector2f(0, 0);

                acceleration = Physics.Mu * Physics.G * mass;
            }

            // Движется ли круг
            public boolean isMoving()
            {
                return ((velocity.X != 0.0f) || (velocity.Y != 0.0f));
            }

            // Откорректировать скорость круга по заданной максимальной
            public void correctVelocity(float maxVelocity)
            {
                float vMag = velocity.magnitude();

                if (vMag > maxVelocity)
                    velocity = Vector2f.multiplyWithNumber(velocity, maxVelocity / vMag);
            }

            // Остановить круг
            public void stop()
            {
                velocity.X = 0.0f;
                velocity.Y = 0.0f;
            }

            // Содержит ли круг указанную точку
            public boolean contains(Vector2f p)
            {
                return (position.X - p.X) * (position.X - p.X) + (position.Y - p.Y) * (position.Y - p.Y)
                    <= radius * radius;
            }
            public boolean contains(float x, float y)
            {
                return (position.X - x) * (position.X - x) + (position.Y -y) * (position.Y - y)
                    <= radius * radius;
            }

            // Рассчитать перемещение круга за время dt
            public void calculateNextPosition(float dt)
            {
                // Необходимо уменьшить вектор скорости на значение a * dt
                // v = v0 - a * dt * e, где e - единичный вектор, сонаправленный с v

                // Изменение положения
                dX = Vector2f.multiplyWithNumber(velocity, dt);
            }

            // Переместить круг на новую позицию
            public void moveToNextPosition(float k)
            {
                // Изменение положения
                dX = Vector2f.multiplyWithNumber(dX, k);
                position = Vector2f.sum(position, dX);

                // Изменение скорости
                if (velocity.sqr() > 0.0f)
                {
                    //Vector2f dV = (-1.0f) * acceleration * (dX.magnitude() / velocity.sqr()) * velocity;
                    Vector2f dV = Vector2f.multiplyWithNumber(velocity.normalize(), (-1.0f) * acceleration);
                    // Изменение скорости
                    Vector2f newVelocity = Vector2f.sum(velocity, dV);
                    // Если скорость сменила свое направление, что круг должен остановиться
                    if (Vector2f.multiply(velocity, newVelocity) <= 0.0f)
                        stop();
                    else
                        velocity = newVelocity;
                }

                dX = new Vector2f(0, 0);
            }

            // Столкнутся ли 2 круга
            // k - часть вектора перемещения до точки соприкосновения
            public boolean willCollide(Circle2f other, Float k)
            {
                Vector2f movevec = Vector2f.negative(dX, other.dX);

                // Early Escape test: if the length of the movevec is less
                // than distance between the centers of these circles minus 
                // their radii, there's no way they can hit. 
                float dist = Vector2f.negative(other.position, position).magnitude();
                float sumRadii = (other.radius + radius);
                dist -= sumRadii;

                // Get the magnitude of the movement vector
                float mag = movevec.magnitude();

                if (mag < dist)
                {
                    k = 1.0f;
                    return false;
                }

                // Normalize the movevec
                Vector2f N = movevec.normalize();

                // Find C, the vector from the center of the moving 
                // circle A to the center of B
                Vector2f C = Vector2f.negative(other.position, position);

                // D = N . C = ||C|| * cos(angle between N and C)
                float D = Vector2f.multiply(N, C);

                // Another early escape: Make sure that A is moving 
                // towards B! If the dot product between the movevec and 
                // B.center - A.center is less that or equal to 0, 
                // A isn't isn't moving towards B
                if (D <= 0)
                {
                    k = 1.0f;
                    return false;
                }
                // Find the length of the vector C
                double lengthC = C.magnitude();

                double F = (lengthC * lengthC) - (D * D);

                // Escape test: if the closest that A will get to B 
                // is more than the sum of their radii, there's no 
                // way they are going collide
                double sumRadiiSquared = sumRadii * sumRadii;
                if (F >= sumRadiiSquared)
                {
                    k = 1.0f;
                    return false;
                }

                // We now have F and sumRadii, two sides of a right triangle. 
                // Use these to find the third side, sqrt(T)
                double T = sumRadiiSquared - F;

                // If there is no such right triangle with sides length of 
                // sumRadii and sqrt(f), T will probably be less than 0. 
                // Better to check now than perform a square root of a 
                // negative number. 
                if (T < 0)
                {
                    k = 1.0f;
                    return false;
                }

                // Therefore the distance the circle has to travel along 
                // movevec is D - sqrt(T)
                float distance = D - (float)Math.sqrt(T);
                if (distance <= 0.0f)
                {
                    k = 1.0f;
                    return false;
                }

                // Finally, make sure that the distance A has to move 
                // to touch B is not greater than the magnitude of the 
                // movement vector. 
                if (mag < distance)
                {
                    k = 1.0f;
                    return false;
                }

                // Set the length of the movevec so that the circles will just 
                // touch
                k = distance / mag;
                
                return true;
            }

            // Обработать столкновение двух кругов, имеющих массу
            public void collideWithNormalMass(Circle2f other)
            {
                // Нормированный вектор из центра 1 шара в центр 2
                Vector2f n = Vector2f.negative(other.position, position).normalize();
                // Найти коэффициент проекции каждого вектора скорости на n
                float a1 = Vector2f.multiply(velocity, n);
                float a2 = Vector2f.multiply(other.velocity, n);

                // Найти коэффициент столкновения
                //               2(a1 - a2)
                // optimizedP = -----------
                //                m1 + m2
                float optimizedP = (2.0f * (a1 - a2)) / (mass + other.mass);

                // Пересчитать вектора скоростей
                // v1' = v1 - optimizedP * m2 * n
                velocity = Vector2f.negative(velocity, 
                        Vector2f.multiplyWithNumber(n,
                                optimizedP * other.mass)
                );
                // v2' = v2 + optimizedP * m1 * n
                other.velocity = Vector2f.sum(other.velocity, 
                        Vector2f.multiplyWithNumber(n,
                                optimizedP * mass)
                );
            }

            // Обработать столкновение с кругом бесконечной массы
            public void collideWithInfMass(Circle2f other)
            {
                // Нормированный вектор из центра 1 шара в центр 2
                Vector2f n = Vector2f.negative(other.position, position).normalize();
                // Найти коэффициент проекции каждого вектора скорости на n
                float a1 = Vector2f.multiply(velocity, n);
                float a2 = Vector2f.multiply(other.velocity, n);

                // Пересчитать вектора скоростей
                // v1' = v1 - 2 * (a1 - a2) * m2 * n
                velocity = Vector2f.negative(velocity, Vector2f.multiplyWithNumber(n, 2 * (a1 - a2)));
                // v2' = v2
            }

            // Столкнется ли круг с отрезком
            // k - часть вектора перемещения до точки соприкосновения
            public boolean willCollide(Wall2f wall, Float k)
            {
                if (dX.sqr() == 0.0f)
                {
                    k = 1.0f;
                    return false;
                }

                // Вектор-стена
                Vector2f seg_v = Vector2f.negative(wall.B, wall.A);
                // Вектор из точки A отрезка в центр круга
                Vector2f pt_v = Vector2f.negative(position, wall.A);
                // Нормированный вектор отрезка
                Vector2f seg_v_unit = seg_v.normalize();
                // Коэффициент проекции вектора pt_v на отрезок
                float proj = Vector2f.multiply(pt_v, seg_v_unit);
                // Вектор-проекция pt_v на отрезок
                Vector2f proj_v = Vector2f.multiplyWithNumber(seg_v_unit, proj);
                // Вектор-дистанция от центра круга до прямой, содержащей отрезок
                Vector2f dist_v = Vector2f.negative(Vector2f.sum(wall.A, proj_v), position);
                // Кратчайшее расстояние от центра круга до прямой
                float dist = dist_v.magnitude();

                // Какую дистанцию сможет пройти круг
                float mag = dX.magnitude();
                // Первая проверка - сможет ли круг вообще долететь до стены
                if (mag < dist - radius)
                {
                    k = 1.0f;
                    return false;
                }
                // Вторая проверка - двигается ли круг по направлению к отрезку
                if ( Vector2f.multiply(dX, dist_v) <= 0)
                {
                    k = 1.0f;
                    return false;
                }

                // Новая позиция шарика, если бы он не столкнулся со стеной
                Vector2f newPosition = Vector2f.sum(position, dX);
                // Точка пересечения прямой, содержащей вектор перемещения и отрезка
                Vector2f intersection;
                float a1 = newPosition.Y - position.Y;
                float b1 = position.X - newPosition.X;
                float c1 = a1 * position.X + b1 * position.Y;
                float a2 = wall.B.Y - wall.A.Y;
                float b2 = wall.A.X - wall.B.X;
                float c2 = a2 * wall.A.X + b2 * wall.A.Y;
                float det = a1 * b2 - a2 * b1;

                intersection = new Vector2f((b2 * c1 - b1 * c2) / det, (a1 * c2 - a2 * c1) / det);

                // Коэффициент пропорциональности
                float coef = (dist - radius) / dist;
                // Расстояние, которое должен пройти круг до столкновения
                float distance = coef *  Vector2f.negative(intersection, position).magnitude();
                // Сможет ли круг пройти это расстояние
                if (mag < distance)
                {
                    k = 1.0f;
                    return false;
                }
                // Точка, в которой, возможно, произошло столкновение
                newPosition = Vector2f.sum(position, Vector2f.multiplyWithNumber(dX.normalize(), distance));
                // Столкновение произошло, когда точки A и B лежат по разные стороны от точки newPosition
                // Т.е. когда уголы newPosition-B-A и newposition-A-B - острые
                Vector2f A = Vector2f.negative(wall.A, Vector2f.multiplyWithNumber(seg_v_unit, radius));
                Vector2f B = Vector2f.sum(wall.B, Vector2f.multiplyWithNumber(seg_v_unit, radius));
                Vector2f AnP = Vector2f.negative(newPosition, A);
                Vector2f BnP = Vector2f.negative(newPosition, B);
                if ((Vector2f.multiply(AnP, Vector2f.negative(B, A)) >= 0.0f) 
                        && (Vector2f.multiply(BnP, Vector2f.negative(A, B)) >= 0.0f))
                {
                    k = distance / mag;
                    return true;
                }
                else
                {
                    k = 1.0f;
                    return false;
                }
            }

            // Обработать столкновение со стеной
            public void collideWithWall(Wall2f wall)
            {
                // Вектор стены
                Vector2f seg_v = Vector2f.negative(wall.B, wall.A);
                // Вектор, перпендикулярный вектору стены
                Vector2f norm_v = null;
                if (Vector2f.multiply(velocity, seg_v) > 0.0f)
                    norm_v = new Vector2f(-seg_v.Y, seg_v.X);
                else
                    norm_v = new Vector2f(seg_v.Y, -seg_v.X);
                norm_v = norm_v.normalize();

                // Отражаем скорость
                velocity = Vector2f.negative(velocity, 
                        Vector2f.multiplyWithNumber(norm_v,
                                2.0f * Vector2f.multiply(velocity, norm_v)
                        )
                );
            }
            
            public void addVelocity(Vector2f v) {
                velocity = Vector2f.sum(velocity, v);
            }
        }
