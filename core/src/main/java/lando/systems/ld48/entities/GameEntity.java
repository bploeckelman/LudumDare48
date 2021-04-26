package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld48.Assets;
import lando.systems.ld48.physics.PhysicsComponent;
import lando.systems.ld48.screens.GameScreen;

public class GameEntity implements PhysicsComponent {

    public enum Direction {
        right, left;
        public static Direction random() {
            return MathUtils.randomBoolean() ? right : left;
        }
    }

    // add posses, release/excorsize?, landing, dying states - death is special
    public enum State { standing, walking, jumping, jump, falling, attacking, death }

    protected Assets assets;

    protected GameScreen screen;
    protected TextureRegion keyframe;
    protected Animation<TextureRegion> animation;
    public AnimationSet animationSet;
    public boolean animationPaused = false;

    public State state = State.standing;
    private State lastState = State.standing;

    public Direction direction = Direction.right;

    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();
    public Vector2 acceleration = new Vector2();
    public float bounceScale = 0.8f;
    public boolean ignoreGravity = false;

    public Rectangle imageBounds = new Rectangle();
    public Rectangle collisionBounds = new Rectangle();
    public Circle collisionCircle = new Circle();

    public Vector3 impulse = new Vector3();

    private boolean grounded;

    protected float stateTime;
    protected float maxHorizontalVelocity = 100f;
    private float maxVerticalVelocity = 1200f;
    private Array<Rectangle> tiles = new Array<>();

    public int hitPoints = 100;
    public boolean dead = false;

    protected float renderRotation = 0;

    public float height, width;

    // bullet hacks
    public float bulletTimeToLive = 10;
    public float bulletSpeed = 200;
    public float bulletSize = 5;

    GameEntity(GameScreen screen, Animation<TextureRegion> animation) {
        this(screen, animation.getKeyFrame(0f));
        this.animation = animation;
        animationSet = new AnimationSet(animation);
    }

    protected GameEntity(GameScreen screen, TextureRegion keyframe) {
        this.assets = screen.game.assets;
        this.screen = screen;
        this.animation = null;
        this.keyframe = keyframe;
        this.grounded = false;
        this.stateTime = 0f;
    }

    protected void setAnimation(Animation<TextureRegion> animation) {
        this.animation = animation;
        stateTime = 0;
    }

    protected void initEntity(float x, float y, float width, float height) {
        collisionBounds.set(x, y, width, height);
        imageBounds.set(this.collisionBounds);
        setPosition(x, y);
        this.width = width;
        this.height = height;
    }

    public void changeDirection() {
        setDirection((direction == Direction.left) ? Direction.right : Direction.left);
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void update(float dt) {
        if (dead) {
            removeFromScreen();
            return;
        }

        if (!animationPaused) {
            stateTime += dt;
        }

        if (animation != null) {
            keyframe = animation.getKeyFrame(stateTime);
        }

        if (hitPoints <= 0) {
            // setState -> dying, for now just kill the bastard
            dead = true;
        } else {
            updatePosition(dt);

            // clamp velocity to maximum, horizontal only
            velocity.x = MathUtils.clamp(velocity.x, -maxHorizontalVelocity, maxHorizontalVelocity);

            imageBounds.setPosition(position.x - imageBounds.width / 2f, position.y - collisionBounds.height / 2f);
            collisionBounds.setPosition(position.x - collisionBounds.width / 2f, position.y - collisionBounds.height / 2f);
            collisionCircle.setPosition(position.x, position.y);
            collisionCircle.setRadius(collisionBounds.width / 2f);

            // check y velocity and setState -> falling
            // check x velocity and setState -> walking
        }

        if (state != lastState) {
            updateState(lastState);
            lastState = state;
        }
    }

    // override player or enemy movement
    protected void updatePosition(float dt) { }

    protected void updateState(State lastState) {
        // todo: handle state transitions here
    }

    public void addToScreen(float x, float y) {
        setPosition(x, y);
        screen.physicsEntities.add(this);
    }

    public void removeFromScreen() {
        screen.physicsEntities.removeValue(this, true);
    }

    public void updateBounds() {
        collisionBounds.setPosition(position.x - collisionBounds.width / 2f, position.y - collisionBounds.height / 2f);
    }

    public void centerOn(GameEntity entity) {
        float x = entity.collisionBounds.x + (entity.collisionBounds.width - collisionBounds.width) / 2;
        float y = entity.collisionBounds.y + (entity.collisionBounds.height - collisionBounds.height) / 2;
        setPosition(x, y);
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
        imageBounds.setPosition(x - imageBounds.width / 2f, y - collisionBounds.height / 2f);
        collisionBounds.setPosition(x - collisionBounds.width / 2f, y - collisionBounds.height / 2f);
        collisionCircle.setPosition(x, y);
        collisionCircle.setRadius(collisionBounds.width / 2f);
    }

    public void render(SpriteBatch batch) {
        if (keyframe == null) return;

        float scaleX = (direction == Direction.right) ? 1 : -1;
        float scaleY = 1;

        // squishy in the air
        if (!grounded) {
            scaleX *= .85f;
            scaleY = 1.15f;
        }

        batch.setColor(getEffectColor());
        batch.draw(keyframe, imageBounds.x, imageBounds.y,
                imageBounds.width / 2, imageBounds.height / 2,
                imageBounds.width, imageBounds.height,
                scaleX, scaleY,
                renderRotation);
        batch.setColor(Color.WHITE);
    }

    public void renderDebug(SpriteBatch batch) {
        batch.setColor(Color.YELLOW);
        assets.debugNinePatch.draw(batch, imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height);

        batch.setColor(Color.RED);
        assets.debugNinePatch.draw(batch, collisionBounds.x, collisionBounds.y, collisionBounds.width, collisionBounds.height);

        batch.setColor(Color.WHITE);
    }

    /**
     * Override to specify how this entity should be tinted
     */
    public Color getEffectColor() {
        return Color.WHITE;
    }

    // ------------------------------------------------------------------------
    // Physics component implementation
    // ------------------------------------------------------------------------

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public float getBounceScale() {
        return bounceScale;
    }

    @Override
    public boolean ignoreGravity() {
        return ignoreGravity;
    }

    @Override
    public Vector2 getAcceleration() {
        return acceleration;
    }

    @Override
    public Shape2D getCollisionBounds() {
        updateBounds();
        return collisionBounds;
    }

    @Override
    public Vector3 getImpulse() {
        return impulse;
    }

    @Override
    public boolean isGrounded() {
        return grounded;
    }

    @Override
    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

}
