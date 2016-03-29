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
public class Ball2f {
            // Позиция
            private Vector2f location;
            // Мгновенная скорость
            private Vector2f v;
            // Ускорение (вспомогательная переменная)
            private float a;
            // Радиус
            private float r;
            // Масса (для закона сохранения импульса)
            private float m;

            // Конструктор
            public Ball2f(float _x, float _y, float _r, float _m)
            {
                location = new Vector2f(_x, _y);
                v = new Vector2f(0.0f, 0.0f);
                r = _r;
                m = _m;

                a = Physics.Mu * Physics.G;
            }

            // Находится ли точка внутри круга
            public boolean contains(Vector2f p)
            {
                return (location.X - p.X) * (location.X - p.X) + (location.Y - p.Y) * (location.Y - p.Y) <= r * r;
            }

            // Переместить круг в заданную точку (управление кругом)
            public void moveTo(Vector2f p, float dt)
            {
                // Обновляем скорость круга
                v.X = (p.X - location.X) / dt;
                v.Y = (p.Y - location.Y) / dt;

                correctVelocity(dt);

                // Обновляем положение круга
                location.X = p.X;
                location.Y = p.Y;
            }
            public void moveTo(float x, float y, float dt)
            {
                moveTo(new Vector2f(x, y), dt);
            }

            // Передвижение круга за время dt (естественное движение)
            public void move(float dt)
            {
                // Абсолютное значение скорости
                float velocity = v.magnitude();
                // Только если шар двигается
                if (v.X != 0.0f)
                {
                    // Вычисляем новые координаты круга
                    location.X += v.X * dt - (a / 2f) * dt * dt;
                    // Вычисляем новое значение скорости круга по X
                    float vX = v.X - ((a * v.X) / velocity) * dt;
                    // Если скорость поменяла свой знак, то круг должен остановиться
                    v.X = (v.X * vX < 0.0f) ? 0.0f : vX;
                }
                if (v.Y != 0.0f)
                {
                    // Вычисляем новые координаты круга
                    location.Y += v.Y * dt - (a / 2f) * dt * dt;
                    // Вычисляем новое значение скорости круга по Y
                    float vY = v.Y - ((a * v.Y) / velocity) * dt;
                    // Если скорость поменяла свой знак, то круг должен остановиться
                    v.Y = (v.Y * vY < 0.0f) ? 0.0f : vY;
                }
            }

            // Придать импульс кругу
            public void addVelocity(float _vX, float _vY)
            {
                v.X += _vX;
                v.Y += _vY;
            }

            // Остановить круг
            public void stop()
            {
                v.X = 0.0f;
                v.Y = 0.0f;
            }

            // Движется ли круг
            public boolean isMoving()
            {
                return ((v.X != 0.0f) || (v.Y != 0.0f));
            }

            // Опредилить максимальную безопасную скорость для круга (чтобы он не проходил сквозь стену)
            // в зависимости от периода обращения к нему
            public float maxVelocity(float dt)
            {
                return (2 * r / dt - a * dt / 2f);
            }

            // Скорректировать скорость, чтобы она не превысила максимальную
            public void correctVelocity(float dt)
            {
                float currentVelocity = v.magnitude();
                float maxVelocity = this.maxVelocity(dt);

                if (currentVelocity > maxVelocity)
                {
                    float alfa = maxVelocity / currentVelocity;
                    v.X *= alfa;
                    v.Y *= alfa;
                }
            }

            // Столкнулся ли круг со стеной
            private boolean colliding(Wall2f wall, Vector2f OH)
            {
                // Так как основание H высоты OH лежит на прямой AB, то точку H можно
                // представить в виде:
                // H = A + alfa * AB,
                // где alfa - действительное.
                // Тогда OH = H - O = A + alfa * AB - O = OA + alfa * AB.
                // Так как векторы OH и AB перпендикулярны, а при делении комплексных чисел
                // их аргументы (углы) вычитаются, то частное OH/AB или (OA + alfa * AB)/AB
                // или (OA / AB + alfa) будет иметь аргумент, равный по модулю 90 градусов, т.е.
                // будет числом чисто мнимым. Следовательно (OA / AB).real() + alfa = 0, откуда
                // alfa = -(AB / BC).real().
                // Частное двух векторов - скалярное произведение, деленное на скалярный квадрат
                // делителя.
                float alfa = -((wall.A.X - location.X) * (wall.B.X - wall.A.X) + (wall.A.Y - location.Y) * (wall.B.Y - wall.A.Y)) /
                              ((wall.B.X - wall.A.X) * (wall.B.X - wall.A.X) + (wall.B.Y - wall.A.Y) * (wall.B.Y - wall.A.Y));
                // Ближайшая точка отрезка к центру окружности
                Vector2f H = new Vector2f(wall.A.X + alfa * (wall.B.X - wall.A.X), wall.A.Y + alfa * (wall.B.Y - wall.A.Y));
                // Вектор расстояния от центра окружности до отрезка
                OH = new Vector2f(H.X - location.X, H.Y - location.Y);

                // Окружность пересекает прямую AB
                if (OH.X * OH.X + OH.Y * OH.Y <= r * r)
                {
                    // Отрезок пересекает окружность когда расстояние от центра окружности до отрезка меньше
                    // радиуса, и точки A и B лежат по разные стороны от прямой, содержащей это расстояние
                    // A и B лежат по разные стороны от OH -
                    // векторные произведения OH*OA и OH*OB имеют разный знак
                    float OHOA = OH.X * (wall.A.Y - location.Y) - OH.Y * (wall.A.X - location.X);
                    float OHOB = OH.X * (wall.B.Y - location.Y) - OH.Y * (wall.B.X - location.X);
                    if (OHOA * OHOB < 0.0f)
                        return true;
                    //Отрезок пересекает окружность, если один из его концов лежит внутри окружности
                    if ((wall.A.X - location.X) * (wall.A.X - location.X) + (wall.A.Y - location.Y) * (wall.A.Y - location.Y) <= r * r)
                        return true;
                    if ((wall.B.X - location.X) * (wall.B.X - location.X) + (wall.B.Y - location.Y) * (wall.B.Y - location.Y) <= r * r)
                        return true;
                }

                return false;
            }

            // Обработать столкновение со стеной
            public void collide(Wall2f wall)
            {
                // Расстояние от центра круга до стены
                Vector2f OH = null;
                // Обработка столкновения
                if (isMoving() && colliding(wall, OH))
                {
                    // Вектор номали для стены
                    Vector2f N = new Vector2f(wall.A.Y - wall.B.Y, wall.B.X - wall.A.X);
                    // Коэффициент проекции скорости на нормаль
                    float alfa = (v.X * N.X + v.Y * N.Y) / (N.X * N.X + N.Y * N.Y);
                    // Проекция скорости на вектор нормали
                    Vector2f ON = new Vector2f(N.X * alfa, N.Y * alfa);

                    // OB = -v * beta
                    // beta = |OA| / |ON|
                    // |OA| = R - |OH|
                    // Вычисляем отношение |OA| / |ON|
                    float beta = (r - OH.magnitude()) / ON.magnitude();
                    // Вектор для смещения круга от стены
                    Vector2f OB = new Vector2f(-v.X * beta, -v.Y * beta);
                    //Убираем круг от стены
                    location.X += OB.X;
                    location.Y += OB.Y;

                    //Отражаем вектор скорости
                    v.X += -2f * ON.X;
                    v.Y += -2f * ON.Y;

                    //Учитываем коэффициент упругости
                    v.X *= Physics.Restitution;
                    v.Y *= Physics.Restitution;
                }
            }

            // Столкнулся ли круг с кругом
            public boolean colliding(Ball2f circle)
            {
                // Вектор между центрами кругов
                Vector2f dist = new Vector2f(circle.location.X - location.X, circle.location.Y - location.Y);

                // Минимальное расстояние между кругами
                float sqrRadius = (r + circle.r) * (r + circle.r);

                if (dist.X * dist.X + dist.Y * dist.Y <= sqrRadius)
                    return true;
                else
                    return false;
            }

            // Обработать столкновение круга с другим кругом
            public void collide(Ball2f circle)
            {
                if ((isMoving() || circle.isMoving()) && (colliding(circle)))
                {
                    // Вектор расстояния между кругами
                    Vector2f delta = new Vector2f(circle.location.X - location.X, circle.location.Y - location.Y);
                    float d = delta.magnitude();
                    // Минимальная дистанция, на которую нужно раздвинуть круги после пересечения
                    float alfa = (r + circle.r - d) / d;
                    Vector2f mtd = new Vector2f(delta.X * alfa, delta.Y * alfa);

                    // Раздвинуть круги, основываясь на их массе
                    location.X -= mtd.X * (circle.m / (m + circle.m));
                    location.Y -= mtd.Y * (circle.m / (m + circle.m));
                    circle.location.X += mtd.X * (m / (m + circle.m));
                    circle.location.Y += mtd.Y * (m / (m + circle.m));

                    // Перейдем в относительную систему координат и решим задачу о столкновении
                    // двигающегося со скоростью vRes круга и неподвижного круга
                    Vector2f vRes = new Vector2f(v.X - circle.v.X, v.Y - circle.v.Y);
                    // Нормировка вектора дистанции
                    float mtdN = mtd.magnitude(); ;
                    Vector2f mtdNorm = new Vector2f(mtd.X / mtdN, mtd.Y / mtdN);

                    // float vn = vRes.X * mtd.X + vRes.Y * mtd.Y;
                    float vn = vRes.X * mtdNorm.X + vRes.Y * mtdNorm.Y;

                    // Окружности пересекаются, но двигаются уже друг от друга
                    if (vn < 0.0f) return;

                    // Импульс столкновения
                    float i = -(1f + Physics.Restitution) * vn * (m * circle.m) / (m + circle.m);
                    Vector2f impulse = new Vector2f(mtdNorm.X * i, mtdNorm.Y * i);

                    // Обмен моментами
                    v.X += impulse.X / m;
                    v.Y += impulse.Y / m;
                    circle.v.X -= impulse.X / circle.m;
                    circle.v.Y -= impulse.Y / circle.m;
                }
            }

            // Обработать столкновение круга с другим кругом бесконечной массы
            public void collideOneWay(Ball2f circle)
            {
                if ((isMoving() || circle.isMoving()) && (colliding(circle)))
                {
                    // Вектор расстояния между кругами
                    Vector2f delta = new Vector2f(circle.location.X - location.X, circle.location.Y - location.Y);
                    float d = delta.magnitude();
                    // Минимальная дистанция, на которую нужно раздвинуть круги после пересечения
                    float alfa = (r + circle.r - d) / d;
                    Vector2f mtd = new Vector2f(delta.X * alfa, delta.Y * alfa);

                    // Раздвинуть круги, основываясь на их массе
                    location.X -= mtd.X;
                    location.Y -= mtd.Y;

                    // Перейдем в относительную систему координат и решим задачу о столкновении
                    // двигающегося со скоростью vRes круга и неподвижного круга
                    Vector2f vRes = new Vector2f(v.X - circle.v.X, v.Y - circle.v.Y);
                    // Нормировка вектора дистанции
                    float mtdN = mtd.magnitude(); ;
                    Vector2f mtdNorm = new Vector2f(mtd.X / mtdN, mtd.Y / mtdN);

                    // float vn = vRes.X * mtd.X + vRes.Y * mtd.Y;
                    float vn = vRes.X * mtdNorm.X + vRes.Y * mtdNorm.Y;

                    // Окружности пересекаются, но двигаются уже друг от друга
                    if (vn < 0.0f) return;

                    // Импульс столкновения
                    float i = -(1f + Physics.Restitution) * vn;
                    Vector2f impulse = new Vector2f(mtdNorm.X * i, mtdNorm.Y * i);

                    // Обмен моментами
                    v.X += impulse.X / m;
                    v.Y += impulse.Y / m;
                }
            }

            // Отнести круг в сторону относительно круга circle, если они пересекаются
            public void spread(Ball2f circle)
            {
                if (colliding(circle))
                {
                    //Вектор расстояния между кругами
                    Vector2f delta = new Vector2f(circle.location.X - location.X, circle.location.Y - location.Y);
                    float d = delta.magnitude();
                    //Минимальная дистанция, на которую нужно раздвинуть круги после пересечения
                    float alfa = (r + circle.r - d) / d;
                    Vector2f mtd = new Vector2f(delta.X * alfa, delta.Y * alfa);

                    //Отодвишуть шар.
                    location.X -= mtd.X;
                    location.Y -= mtd.Y;
                }
            }

            //Получить вектор скорости
            public Vector2f getVelocity()
            {
                return new Vector2f(v.X, v.Y);
            }
}
