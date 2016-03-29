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
public class Mallet extends Circle2f{
    public boolean captured;

            public Mallet(float x, float y, float r)
            {
                super(x, y, r, 1.0f);
                captured = false;
            }

            public void moveTo(float x, float y, float dt)
            {
                Vector2f newPosition = new Vector2f(x, y);

                dX = Vector2f.negative(newPosition, position);

                velocity = Vector2f.divideByNumber(dX, dt);
            }
}
