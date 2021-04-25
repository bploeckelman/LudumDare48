package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import lando.systems.ld48.screens.GameScreen;

public class EnemyEntity extends MovableEntity {

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

    @Override
    public void addToScreen(float x, float y) {
        super.addToScreen(x, y);
        screen.enemies.add(this);
    }

    @Override
    public void removeFromScreen() {
        super.removeFromScreen();
        screen.enemies.removeValue(this, true);
    }

    public void reset(Rectangle bounds) {
        targeted = false;
        float x = bounds.x + bounds.width / 2;
        float y = bounds.y + bounds.height / 2 + (bounds.height - imageBounds.height) / 2;
        addToScreen(x, y);
    }

    @Override
    public Color getEffectColor() {
        if (targeted) { return Color.RED; }

        return super.getEffectColor();
    }

}
