package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld48.Config;
import lando.systems.ld48.levels.SpawnInteractable;
import lando.systems.ld48.screens.GameScreen;

public class EnemyEntity extends MovableEntity {

    public boolean targeted = false;

    public float scale = 1f;
    private float attackDelay = 0;
    public float dirChangeCD = 0.2f;

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
        this.direction = Direction.left;
        attackDelay = 3f + MathUtils.random(3f);

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

    Array<Rectangle> tiles = new Array<>();
    Vector2 shootRay = new Vector2();
    Rectangle testRectangle = new Rectangle();
    @Override
    public void update(float dt) {
        super.update(dt);
        attackDelay -= dt;
        dirChangeCD -= dt;
        boolean flip = false;


        screen.level.getTiles(collisionBounds.x, collisionBounds.y, collisionBounds.x + collisionBounds.width, collisionBounds.y - 4, tiles);
        if (tiles.size == 0) {
            velocity.x = 0;
            setGrounded(false);
        }

        if (direction == Direction.left){
            screen.level.getTiles(collisionBounds.x, collisionBounds.y, collisionBounds.x - 10, collisionBounds.y + collisionBounds.height, tiles);
            if (tiles.size > 0) {
                velocity.x = 0;
                flip = true;
            }
            screen.level.getTiles(collisionBounds.x, collisionBounds.y - 15, collisionBounds.x - 10, collisionBounds.y - 1, tiles);
            if (tiles.size == 0) {
                velocity.x = 0;
                flip = true;
            }
            testRectangle.set(collisionBounds.x - 10, collisionBounds.y, collisionBounds.width, collisionBounds.height);
            for (InteractableEntity interactable : screen.interactables){
                if (interactable.type == SpawnInteractable.Type.door){
                    if (interactable.collisionBounds.overlaps(testRectangle)){
                        velocity.x = 0;
                        flip = true;
                    }
                }
            }
        } else {
            screen.level.getTiles(collisionBounds.x + collisionBounds.width, collisionBounds.y, collisionBounds.x +collisionBounds.width + 10, collisionBounds.y + collisionBounds.height, tiles);
            if (tiles.size > 0) {
                velocity.x = 0;
                flip = true;
            }
            screen.level.getTiles(collisionBounds.x + collisionBounds.width, collisionBounds.y - 15, collisionBounds.x +collisionBounds.width + 10, collisionBounds.y - 1, tiles);
            if (tiles.size == 0) {
                velocity.x = 0;
                flip = true;
            }
            testRectangle.set(collisionBounds.x + 10, collisionBounds.y, collisionBounds.width, collisionBounds.height);
            for (InteractableEntity interactable : screen.interactables){
                if (interactable.type == SpawnInteractable.Type.door){
                    if (interactable.collisionBounds.overlaps(testRectangle)){
                        velocity.x = 0;
                        flip = true;
                    }
                }
            }
        }

        if (flip && dirChangeCD < 0) {
            changeDirection();
        }

        if (!targeted) {
            move(direction, 15f);

            if (direction == Direction.left) {
                shootRay.set(position.x - 200, position.y);
            } else {
                shootRay.set(position.x + 200, position.y);
            }
            if (attackDelay <= 0 && screen.player.capturedEnemy != null && Intersector.intersectSegmentRectangle(position, shootRay, screen.player.collisionBounds)) {
                attack();
                attackDelay += MathUtils.random(3f) + 2f;
            }
        }
    }

    Rectangle hpRect = new Rectangle();
    Color hpColor = new Color();
    @Override
    public void render(SpriteBatch batch){
        super.render(batch);
        hpRect.set(imageBounds.x, imageBounds.y + imageBounds.height + 2, imageBounds.width, 2);
        float hpPercent = (float)hitPoints/maxHitpoints;
        // TODO this will work or can be juiced up if there is time
        batch.setColor(Color.BLACK);
        batch.draw(assets.whitePixel, hpRect.x, hpRect.y, hpRect.width, hpRect.height);
        hpColor.fromHsv(hpPercent * 120, 1f, 1f);
        hpColor.a = 1f;
        batch.setColor(hpColor);
        batch.draw(assets.whitePixel, hpRect.x, hpRect.y, hpRect.width * hpPercent, hpRect.height);

        batch.setColor(Color.WHITE);
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
