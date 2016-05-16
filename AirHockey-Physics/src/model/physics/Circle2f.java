package model.physics;

import java.awt.Color;
import java.awt.Graphics;

// Круг (базовый класс)
public class Circle2f
{
    // Текущее местоположение круга
    protected Vector2f position;
    // Текущая скорость круга
    protected Vector2f velocity;
    // Текущее ускорение круга
    public float acceleration;
    // Масса круга
    protected float mass;
    // Радиус круга
    protected float radius;

    // Текущее перемещение к новой точке
    public Vector2f dX;
    // Будущее изменение скорости после перемещения
    protected Vector2f dV;

    public Vector2f getPosition()
    {
        return position;
    }
    public Vector2f getVelocity()
    {
        return velocity;
    }
    public float getAcceleration()
    {
        return acceleration;
    }
    public float getMass()
    {
        return mass;
    }
    public float getRadius()
    {
        return radius;
    }

    // Конструктор
    public Circle2f(float x, float y, float r, float m)
    {
        position = new Vector2f(x, y);
        velocity = new Vector2f();
        radius = r;
        mass = m;

        dX = new Vector2f();
        dV = new Vector2f();

        // Ускорение = Mu * N = Mu * m * g
        acceleration = Physics.Mu * Physics.G * mass;
    }

    // Придать импульс кругу
    public void addVelocity(Vector2f v)
    {
        velocity = velocity.add(v);
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
            velocity = velocity.mult(maxVelocity / vMag);
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
        return (position.X - x) * (position.X - x) + (position.Y - y) * (position.Y - y)
            <= radius * radius;
    }

    // Рассчитать перемещение круга за время dt
    public void calculateNextPosition(float dt)
    {
        // Мгновенная скорость изменится после передвижения круга на новую позицию
        // Как изменится скорость dV = -a * dt
        dV = velocity.normalize().mult((-1.0f) * acceleration * dt);

        // Изменение положения
        dX = velocity.mult(dt);
    }

    // Переместить круг на новую позицию
    public void moveToNextPosition(float k)
    {
        // Изменение положения
        dX = dX.mult(k);
        position = position.add(dX);

        // Изменение скорости
        dV = dV.mult(k);
        // Физика предусматривает лишь торможение под действием силы трения
        if (velocity.sqr() > 0.0f)
        {
            // Новая скорость
            Vector2f newVelocity = velocity.add(dV);
            // Если скорость сменила свое направление, то круг должен остановиться
            if (velocity.dot(newVelocity) <= 0.0f)
                stop();
            else
                velocity = newVelocity;
        }

        // Обнулить вектор перемещения и изменения скорости (перемещение совершено)
        dX = new Vector2f();
        dV = new Vector2f();
    }
    public void moveToNextPosition()
    {
        moveToNextPosition(1.0f);
    }

    // Враппер для возвращения значений функций willCollide
    public class WillCollideWrapper
    {
        public boolean willCollide;
        public float k;
        public WillCollideWrapper(boolean wC, float _k)
        {
            willCollide = wC;
            k = _k;
        }
    }

    // Столкнутся ли 2 круга
    // k - часть вектора перемещения до точки соприкосновения
    public WillCollideWrapper willCollide(Circle2f other)
    {
        Vector2f movevec = dX.sub(other.dX);

        // Вектор между центрами кругов
        Vector2f C = other.position.sub(position);
        // Расстояние между кругами
        float lengthC = C.magnitude();

        // Сможет ли вообще первый круг достичь второго в лучшем случае
        float sumRadii = (other.radius + radius);
        float dist = lengthC - sumRadii;
        // Длина вектора перемещения
        float mag = movevec.magnitude();
        if (mag < dist)
        {
            return new WillCollideWrapper(false, 1.0f);
        }

        // Направление перемещения
        Vector2f N = movevec.normalize();

        // D = N * C = |C| * cos(N, C)
        float D = N.dot(C);

        // Движется ли круг по направлению к другому
        if (D <= 0)
        {
            return new WillCollideWrapper(false, 1.0f);
        }
        
        // Квадрат наименьшего расстояния от центра второго круга до вектора перемещения первого
        double F = (lengthC * lengthC) - (D * D);

        // Если F больше суммы радиусов в квадрате - нет столкновения
        double sumRadiiSquared = sumRadii * sumRadii;
        if (F >= sumRadiiSquared)
        {
            return new WillCollideWrapper(false, 1.0f);
        }

        // Часть перемещения от соприкосновения F до точки столкновения
        double T = sumRadiiSquared - F;

        // Если такого треугольника не существует
        if (T < 0)
        {
            return new WillCollideWrapper(false, 1.0f);
        }

        // Дистанция до столкновения
        float distance = D - (float)Math.sqrt(T);
        // Если один круг находится внутри другого
        if (distance <= 0.0f)
        {
            return new WillCollideWrapper(false, 1.0f);
        }

        // Сможет ли круг пройти такое расстояние
        if (mag < distance)
        {
            return new WillCollideWrapper(false, 1.0f);
        }

        // Часть перемещения до столкновения
        return new WillCollideWrapper(true, distance / mag);
    }

    // Столкнется ли круг с отрезком
    // k - часть вектора перемещения до точки соприкосновения
    public WillCollideWrapper willCollide(Wall2f wall)
    {
        if (dX.sqr() == 0.0f)
        {
            return new WillCollideWrapper(false, 1.0f);
        }

        // Вектор из точки A отрезка в центр круга
        Vector2f pt_v = position.sub(wall.A);
        // Нормированный вектор отрезка
        Vector2f seg_v_unit = wall.getDirection();
        // Коэффициент проекции вектора pt_v на отрезок
        float proj = pt_v.dot(seg_v_unit);
        // Вектор-проекция pt_v на отрезок
        Vector2f proj_v = seg_v_unit.mult(proj);
        // Вектор-дистанция от центра круга до прямой, содержащей отрезок
        Vector2f dist_v = wall.A.add(proj_v).sub(position);
        // Кратчайшее расстояние от центра круга до прямой
        float dist = dist_v.magnitude();

        // Какую дистанцию сможет пройти круг
        float mag = dX.magnitude();
        // Первая проверка - сможет ли круг вообще долететь до стены
        if (mag < dist - radius)
        {
            return new WillCollideWrapper(false, 1.0f);
        }
        // Вторая проверка - двигается ли круг по направлению к отрезку
        if (dX.dot(dist_v) <= 0)
        {
            return new WillCollideWrapper(false, 1.0f);
        }

        // Новая позиция шарика, если бы он не столкнулся со стеной
        Vector2f newPosition = position.add(dX);
        // Точка пересечения прямой, содержащей вектор перемещения и отрезка
        Vector2f intersection;
        float a1 = newPosition.Y - position.Y;         float a2 = wall.B.Y - wall.A.Y;
        float b1 = position.X - newPosition.X;         float b2 = wall.A.X - wall.B.X;
        float c1 = a1 * position.X + b1 * position.Y;  float c2 = a2 * wall.A.X + b2 * wall.A.Y;
        float det = a1 * b2 - a2 * b1;

        intersection = new Vector2f((b2 * c1 - b1 * c2) / det, (a1 * c2 - a2 * c1) / det);

        // Коэффициент пропорциональности
        float coef = (dist - radius) / dist;
        // Расстояние, которое должен пройти круг до столкновения
        float distance = coef * intersection.sub(position).magnitude();
        // Сможет ли круг пройти это расстояние
        if (mag < distance)
        {
            return new WillCollideWrapper(false, 1.0f);
        }
        // Точка, в которой, возможно, произошло столкновение
        newPosition = position.add(dX.normalize().mult(distance));
        // Столкновение произошло, когда точки A и B лежат по разные стороны от точки newPosition
        // Т.е. когда углы newPosition-B-A и newposition-A-B - острые
        Vector2f A = wall.A.sub(seg_v_unit.mult(radius));
        Vector2f B = wall.B.add(seg_v_unit.mult(radius));
        Vector2f AnP = newPosition.sub(A);
        Vector2f BnP = newPosition.sub(B);
        if ((AnP.dot(B.sub(A)) >= 0.0f) && (BnP.dot(A.sub(B)) >= 0.0f))
            return new WillCollideWrapper(true, distance / mag);
        else
            return new WillCollideWrapper(false, 1.0f);
    }

    // Обработать столкновение двух кругов, имеющих массу
    public void collideWithNormalMass(Circle2f other)
    {
        // Нормированный вектор из центра 1 шара в центр 2
        Vector2f n = other.position.sub(position).normalize();
        // Найти коэффициент проекции каждого вектора скорости на n
        float a1 = velocity.dot(n);
        float a2 = other.velocity.dot(n);

        // Найти коэффициент столкновения
        //               2(a1 - a2)
        // optimizedP = -----------
        //                m1 + m2
        float optimizedP = (2.0f * (a1 - a2)) / (mass + other.mass);

        // Пересчитать вектора скоростей
        // v1' = v1 - optimizedP * m2 * n
        velocity = velocity.sub(n.mult(optimizedP * other.mass));
        // v2' = v2 + optimizedP * m1 * n
        other.velocity = other.velocity.add(n.mult(optimizedP * mass));
    }

    // Обработать столкновение с кругом бесконечной массы
    public void collideWithInfMass(Circle2f other)
    {
        // Нормированный вектор из центра 1 шара в центр 2
        Vector2f n = other.position.sub(position).normalize();
        // Найти коэффициент проекции каждого вектора скорости на n
        float a1 = velocity.dot(n);
        float a2 = other.velocity.dot(n);

        // Пересчитать вектора скоростей
        // v1' = v1 - 2 * (a1 - a2) * m2 * n
        velocity = velocity.sub(n.mult(2 * (a1 - a2)));
        // v2' = v2
        
        // Нормированный вектор из центра 1 шара в центр 2
        //Vector2f n = other.position.sub(position).normalize();
        //Vector2f n = position.sub(other.position).normalize();
        // Найти коэффициент проекции каждого вектора скорости на n
        //float a1 = velocity.dot(n);
        //float a2 = other.velocity.dot(n);

        // Пересчитать вектора скоростей
        // v1' = v1 - 2 * (a1 - a2) * m2 * n
        //velocity = velocity.sub(n.mult(a1 - a2));
        // v2' = v2
    }

    // Обработать столкновение со стеной
    public void collideWithWall(Wall2f wall)
    {
        // Вектор стены
        Vector2f seg_v = wall.B.sub(wall.A);
        // Вектор, перпендикулярный вектору стены
        Vector2f norm_v = null;
        if (velocity.dot(seg_v) > 0.0f)
            norm_v = new Vector2f(-seg_v.Y, seg_v.X);
        else
            norm_v = new Vector2f(seg_v.Y, -seg_v.X);
        norm_v = norm_v.normalize();

        // Отражаем скорость
        velocity = velocity.sub(norm_v.mult(2.0f * velocity.dot(norm_v)));
    }

    // Враппер для возвращения значений функции isColliding
    public class IsCollidingWrapper
    {
        public boolean isColliding;
        public Vector2f OH;

        public IsCollidingWrapper(boolean iC, Vector2f _OH)
        {
            isColliding = iC;
            OH = _OH;
        }
    }

    // Столкнулся ли круг со стеной (круг пересекает отрезок)
    private IsCollidingWrapper isColliding(Wall2f wall)
    {
        // Вектор стены
        Vector2f AB = wall.B.sub(wall.A);

        // Так как основание H высоты OH лежит на прямой AB, то точку H можно
        // представить в виде:
        // H = A + alfa * AB,
        // где alfa - действительное.
        // Тогда OH = H - O = A + alfa * AB - O = OA + alfa * AB.
        // Так как векторы OH и AB перпендикулярны, а при делении комплексных чисел
        // их аргументы (углы) вычитаются, то частное OH/AB или (OA + alfa * AB)/AB
        // или (OA / AB + alfa) будет иметь аргумент, равный по модулю 90 градусов, т.е.
        // будет числом чисто мнимым. Следовательно (OA / AB).real() + alfa = 0, откуда
        // alfa = -(OA / AB).real().
        // Частное двух векторов - скалярное произведение, деленное на скалярный квадрат
        // делителя.
        float alfa = -wall.A.sub(position).dev(AB);
        // Ближайшая точка отрезка к центру окружности
        Vector2f H = wall.A.add(AB.mult(alfa));
        // Вектор расстояния от центра окружности до отрезка
        Vector2f OH = H.sub(position);

        // Окружность пересекает прямую AB
        if (OH.X * OH.X + OH.Y * OH.Y <= radius * radius)
        {
            // Отрезок пересекает окружность когда расстояние от центра окружности до отрезка меньше
            // радиуса, и точки A и B лежат по разные стороны от прямой, содержащей это расстояние
            // A и B лежат по разные стороны от OH -
            // векторные произведения OH*OA и OH*OB имеют разный знак
            float OHOA = OH.X * (wall.A.Y - position.Y) - OH.Y * (wall.A.X - position.X);
            float OHOB = OH.X * (wall.B.Y - position.Y) - OH.Y * (wall.B.X - position.X);
            if (OHOA * OHOB < 0.0f)
                return new IsCollidingWrapper(true, OH);
            //Отрезок пересекает окружность, если один из его концов лежит внутри окружности
            if ((wall.A.X - position.X) * (wall.A.X - position.X) + (wall.A.Y - position.Y) * (wall.A.Y - position.Y) <= radius * radius)
                return new IsCollidingWrapper(true, OH);
            if ((wall.B.X - position.X) * (wall.B.X - position.X) + (wall.B.Y - position.Y) * (wall.B.Y - position.Y) <= radius * radius)
                return new IsCollidingWrapper(true, OH);
        }

        return new IsCollidingWrapper(false, null);
    }

    // Столкнулся ли круг с кругом
    public boolean isColliding(Circle2f circle)
    {
        // Вектор между центрами кругов
        Vector2f dist = circle.position.sub(position);

        // Минимальное расстояние между кругами
        float sqrRadius = (radius + circle.radius) * (radius + circle.radius);

        if (dist.sqr() <= sqrRadius)
            return true;
        else
            return false;
    }

    // Отодвинуть круг из стены и отразить его скорость
    public void spreadAndCollide(Wall2f wall)
    {
        // Проверка на пересечение
        IsCollidingWrapper collision = isColliding(wall);
        // Расстояние от центра круга до стены
        Vector2f OH = collision.OH;
        // Обработка столкновения
        if (isMoving() && collision.isColliding)
        {
            // Вектор номали для стены
            Vector2f N = new Vector2f(wall.A.Y - wall.B.Y, wall.B.X - wall.A.X);
            // Коэффициент проекции скорости на нормаль
            float alfa = (velocity.X * N.X + velocity.Y * N.Y) / (N.X * N.X + N.Y * N.Y);
            // Проекция скорости на вектор нормали
            Vector2f ON = new Vector2f(N.X * alfa, N.Y * alfa);

            // OB = -v * beta
            // beta = |OA| / |ON|
            // |OA| = R - |OH|
            // Вычисляем отношение |OA| / |ON|
            float beta = (radius - OH.magnitude()) / ON.magnitude();
            // Вектор для смещения круга от стены
            Vector2f OB = velocity.mult(-beta);
            //Убираем круг от стены
            position = position.add(OB);

            //Отражаем вектор скорости
            velocity.sub(ON.mult(2.0f));
        }
    }

    // Раздвинуть круги друг из друга и изменить их скорости, как при столкновении
    public void spreadAndCollide(Circle2f circle)
    {
        if ((isMoving() || circle.isMoving()) && (isColliding(circle)))
        {
            // Вектор расстояния между кругами
            Vector2f delta = circle.position.sub(position);
            float d = delta.magnitude();
            // Минимальная дистанция, на которую нужно раздвинуть круги после пересечения
            float alfa = (radius + circle.radius - d) / d;
            Vector2f mtd = new Vector2f(delta.X * alfa, delta.Y * alfa);

            // Раздвинуть круги, основываясь на их массе
            position.X -= mtd.X * (circle.mass / (mass + circle.mass));
            position.Y -= mtd.Y * (circle.mass / (mass + circle.mass));
            circle.position.X += mtd.X * (mass / (mass + circle.mass));
            circle.position.Y += mtd.Y * (mass / (mass + circle.mass));

            // Перейдем в относительную систему координат и решим задачу о столкновении
            // двигающегося со скоростью vRes круга и неподвижного круга
            Vector2f vRes = velocity.sub(circle.velocity);
            // Нормировка вектора дистанции
            float mtdN = mtd.magnitude();
            Vector2f mtdNorm = new Vector2f(mtd.X / mtdN, mtd.Y / mtdN);

            // float vn = vRes.X * mtd.X + vRes.Y * mtd.Y;
            float vn = vRes.X * mtdNorm.X + vRes.Y * mtdNorm.Y;

            // Окружности пересекаются, но двигаются уже друг от друга
            if (vn < 0.0f) return;

            // Импульс столкновения
            float i = -(1f + Physics.Restitution) * vn * (mass * circle.mass) / (mass + circle.mass);
            Vector2f impulse = new Vector2f(mtdNorm.X * i, mtdNorm.Y * i);

            // Обмен моментами
            velocity.X += impulse.X / mass;
            velocity.Y += impulse.Y / mass;
            circle.velocity.X -= impulse.X / circle.mass;
            circle.velocity.Y -= impulse.Y / circle.mass;
        }
    }

    // Отодвинуть круг от другого в направлении скорости, если они пересекаются
    public void pushAwayFrom(Circle2f circle)
    {
        // Вектор между центрами кругов
        Vector2f dist_v = position.sub(circle.position);
        // Минимальное расстояние между кругами
        float sqrRadius = (radius + circle.radius) * (radius + circle.radius);

        // Проверка на пересечение
        if (dist_v.sqr() > sqrRadius)
            return;

        // Направление, куда нужно передвинуть
        Vector2f N = velocity.normalize();

        // На сколько нужно передвинуть круг
        // (a + x)^2 - a^2 = sumR^2 - dist^2
        // x = -a + sqrt(a^2 + sumR^2 - dist^2)
        float a = dist_v.dot(N);
        float x = -a + (float)Math.sqrt(a * a + sqrRadius - dist_v.sqr());

        // Сдвинуть круг
        position.add(N.mult(x));
    }
}
