package lando.systems.ld48.entities.bosses;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Boss {
    public abstract void update(float dt);
    public abstract void render(SpriteBatch batch);
    public abstract void addToScreen();
}
