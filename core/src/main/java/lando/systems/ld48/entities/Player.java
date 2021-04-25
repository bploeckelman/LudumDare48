package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld48.levels.SpawnPlayer;
import lando.systems.ld48.screens.GameScreen;
import lando.systems.ld48.stuff.Progress;

public class Player extends MovableEntity {

    public final static float SCALE = 0.75f;

    private final float horizontalSpeed = 20f;

    private float deathTime = -1;

    public EnemyEntity capturedEnemy = null;
    public boolean capturing = false;
    public float captureProgress = 0;
    private Progress captureProgressBar;

    private AnimationSet defaultAnimationSet;

    public Player(GameScreen screen, SpawnPlayer spawn) {
        this(screen, spawn.pos.x, spawn.pos.y);
    }

    public Player(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.playerAnimation, screen.game.assets.playerMoveAnimation);

        setJump(screen.game.assets.playerJumpAnimation, 200f);
        setFall(screen.game.assets.playerFallAnimation);

        initEntity(x, y, keyframe.getRegionWidth() * SCALE, keyframe.getRegionHeight() * SCALE);

        id = MoveEntityIds.player;

        defaultAnimationSet = animationSet;

        captureProgressBar = new Progress(assets);
    }

    @Override
    protected void initEntity(float x, float y, float width, float height) {
        imageBounds.set(x, y, width, height);
        float paddingX = (1f / 2f) * width;
        collisionBounds.set(x + paddingX / 2f, y, width - paddingX, height);
        setPosition(x, y);
    }

    @Override
    public void update(float dt) {
        // death takes priority
        boolean isDead = updateDeath(dt);
        if (isDead) return;

        this.jumpHeld = this.screen.upPressed;

        super.update(dt);

        // this is the animation of starting to jump
        if (state != State.jumping) {
            // Check for and apply horizontal movement
            if (this.screen.leftPressed) {
                move(Direction.left);
            } else if (this.screen.rightPressed) {
                move(Direction.right);
            }
        }

        checkIfFellOffscreen();
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);

        if (captureProgress > 0) {
            captureProgressBar.draw(batch, captureProgress, imageBounds.x,
                    imageBounds.y + imageBounds.height + 5, imageBounds.width, 2);
        }
    }

    private final Color spookyTransparent = new Color(1f, 1f, 1f, 0.5f);
    @Override
    public Color getEffectColor() {
        if (capturedEnemy == null) {
            return spookyTransparent;
        } else {
            return Color.WHITE;
        }
    }

    // todo - if player fell offscreen,
    //  pause and trigger a popup saying "you died, but you're a ghost, so you can't die, so you're fine, start over"
    //  when the player dismisses it, unpause and either launch them back onscreen or respawn them at the start
    private void checkIfFellOffscreen() {
        float offStage = -collisionBounds.height;
        if (position.y < offStage) {
            Array<Rectangle> tiles = new Array<>();
            screen.level.getTiles(position.x, position.y, position.x, 10000, tiles);

            Rectangle rect;
            float y = 0;
            for (int i = 0; i < tiles.size; i++) {
                rect = tiles.get(i);
                if (rect.y == y) {
                    y += rect.height;
                } else {
                    if (y == 0) {
                        // respawn at start
                        setPosition(screen.level.getPlayerSpawn().pos.x, screen.level.getPlayerSpawn().pos.y);
                    } else {
                        // launch back onscreen
                        setPosition(position.x, y + collisionBounds.height / 2);
                        velocity.set(0, 400);
                    }
                    break;
                }
            }
        }
    }

    private void move(Direction direction) {
        move(direction, horizontalSpeed);
    }

    @Override
    public void changeDirection() {
        // noop so it doesn't flip rapidly when pushing against a wall.
    }

    private boolean updateDeath(float dt) {
        if (deathTime == -1 || !grounded) return false;

        Animation<TextureRegion> deathAnimation = assets.playerDieAnimation;
        float duration = deathAnimation.getAnimationDuration();
        float deathAnimTime = 7f;

        deathTime += dt;
        keyframe = deathAnimation.getKeyFrame(deathTime);
        if (deathTime > deathAnimTime) {
            dead = false;
            deathTime = -1;
        }

        return true;
    }

    public void possess(EnemyEntity enemy) {
        if (capturedEnemy != null) {
            capturedEnemy.reset(imageBounds);
        }

        capturedEnemy = enemy;
        animationSet = (capturedEnemy != null) ? capturedEnemy.animationSet : defaultAnimationSet;

        state = State.standing;
        animation = animationSet.IdleAnimation;
    }
}
