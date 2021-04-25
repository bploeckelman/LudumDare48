package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.ld48.screens.GameScreen;

public class EnemyEntity extends MovableEntity {

    public float removeTime = 2f;
    public boolean captured = false;
    public boolean targeted = false;

    public float scale = 1f;

    protected EnemyEntity(GameScreen screen, Animation<TextureRegion> animation, float x, float y) {
        this(screen, animation, 1f, x, y);
    }

    protected EnemyEntity(GameScreen screen, Animation<TextureRegion> animation, float scale, float x, float y) {
        super(screen, animation);
        this.scale = scale;
        initEntity(x, y, keyframe.getRegionWidth() * scale, keyframe.getRegionHeight() * scale);

        // todo - should depend on the type of enemy, override in subclasses
        maxHorizontalVelocity = 10;
    }

    @Override
    protected void initEntity(float x, float y, float width, float height) {
        imageBounds.set(x, y, width, height);
        float paddingX = (1f / 2f) * width;
        collisionBounds.set(x + paddingX / 2f, y, width - paddingX, height);
        setPosition(x, y);
    }

    public void addToScreen(float x, float y) {
        setPosition(x, y);
        screen.enemies.add(this);
        screen.physicsEntities.add(this);
    }

    public void removeFromScreen() {
        screen.enemies.removeValue(this, true);
        screen.physicsEntities.removeValue(this, true);
    }

    public EnemyEntity capture() {
        targeted = false;
        captured = true;
        return this;
    }

    // don't override this
    @Override
    public void update(float dt) {
        if (captured) { return; }

        super.update(dt);
        updateEntity(dt);

        if (dead) {
            removeTime -= dt;
            if (removeTime < 0) {
                removeFromScreen();
            }
        }
    }

    // override this
    protected void updateEntity(float dt) { }

    @Override
    public void render(SpriteBatch batch) {
        if (captured) { return; }
        super.render(batch);
    }

    @Override
    public Color getEffectColor() {
        if (targeted) { return Color.RED; }

        if (dead) {
            if ((int)(removeTime * 30) % 2 == 0 ) { return Color.BLACK; }
        }
        return super.getEffectColor();
    }
}
