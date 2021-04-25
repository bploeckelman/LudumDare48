package lando.systems.ld48.physics;

import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface PhysicsComponent {
    Vector2 getPosition();
    Vector2 getVelocity();
    Vector2 getAcceleration();
    Shape2D getCollisionBounds();
    Vector3 getImpulse();
    float getBounceScale();
    boolean isGrounded();
    void setGrounded(boolean grounded);

    void update(float dt);
    void render(SpriteBatch batch);
}
