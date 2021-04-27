package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld48.Game;
import lando.systems.ld48.levels.SpawnPlayer;
import lando.systems.ld48.screens.GameScreen;
import lando.systems.ld48.stuff.Progress;
import lando.systems.ld48.ui.Modal;

public class Player extends MovableEntity {

    public final static float SCALE = 0.75f;
    public static int offScreenCount = 0;

    private final float horizontalSpeed = 20f;

    private float deathTime = -1;

    public EnemyEntity capturedEnemy = null;
    public boolean capturing = false;
    public float captureProgress = 0;
    private Progress captureProgressBar;
    private Progress overheatBar;

    private AnimationSet defaultAnimationSet;
    public boolean isOffScreen;
    private Modal fellOffScreenModal;

    private float invulnTimer = 0f;

    public Player(GameScreen screen, SpawnPlayer spawn) {
        this(screen, spawn.pos.x, spawn.pos.y);
    }

    public Player(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.playerAnimation, screen.game.assets.playerMoveAnimation);

//        setJump(screen.game.assets.playerJumpAnimation, 200f);
        setFall(screen.game.assets.playerAnimation);

        initEntity(x, y, keyframe.getRegionWidth() * SCALE, keyframe.getRegionHeight() * SCALE);

        id = MoveEntityIds.player;

        hitPoints = 150;

        defaultAnimationSet = animationSet;

        captureProgressBar = new Progress(assets);
        overheatBar = new Progress(assets, true);
    }

    @Override
    protected void initEntity(float x, float y, float width, float height) {
        imageBounds.set(x, y, width, height);
        float paddingX = (1f / 2f) * width;
        collisionBounds.set(x + paddingX / 2f, y, width - paddingX, height);
        setPosition(x, y);
    }

    @Override
    public float getGravityModifier() {
        if (capturedEnemy != null) { return 1f; }
        if (this.screen.downPressed) { return 0.5f; }
        if (this.screen.upPressed) { return 0f; }
        return 0.1f;
    }

    @Override
    public void update(float dt) {
        // death takes priority
        boolean isDead = updateDeath(dt);
        if (isDead) return;

        this.jumpHeld = this.screen.upPressed;
        if (this.hitPoints <= 0){
            this.hitPoints = 150;
            possess(null);
            velocity.set(0, 30);
        }

        if (this.screen.upPressed && this.capturedEnemy == null) { this.velocity.set(this.velocity.x, Math.max(this.velocity.y, Math.min(this.velocity.y + 90 * dt, 60))); }

        invulnTimer -= dt;

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

        if (this.screen.shiftPressed) {
            this.attack();
        }

        checkIfFellOffscreen();
    }

    @Override
    public void render(SpriteBatch batch) {
        if (invulnTimer <= 0 || invulnTimer % 0.15 < 0.075) {
            super.render(batch);
        }

        if (captureProgress > 0) {
            captureProgressBar.draw(batch, captureProgress, imageBounds.x,
                    imageBounds.y + imageBounds.height + 5, imageBounds.width, 2);
        }

        if (this.currentHeat > 0) {
            overheatBar.draw(batch, Math.min(currentHeat/2, 1), imageBounds.x - 5,
                    imageBounds.y, 1, imageBounds.height);
        }
    }

    public void renderOffScreenMessage(SpriteBatch batch) {
        if (fellOffScreenModal != null){
            fellOffScreenModal.render(batch);
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
            isOffScreen = true;
            if (offScreenCount < 2){
                fellOffScreenModal = new Modal(screen.game.assets, screen.game.assets.strings.get("fallOffScreen"), screen.getWindowCamera());
            }
            offScreenCount++;
        }
    }

    public void updateOffScreen(float dt) {
        if (fellOffScreenModal != null){
            fellOffScreenModal.update(dt);
            if (fellOffScreenModal.isComplete()) {
                fellOffScreenModal = null;
            }
        }
        else {
            isOffScreen = false;
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

    @Override
    public void adjustHitpoints(int value) {
        if (value < 0 && this.invulnTimer > 0) { return; }
        this.hitPoints += value;
        if (value < 0) {
            invulnTimer = 0.75f;
        }
    }

    private boolean updateDeath(float dt) {
//        if (deathTime == -1 || !isGrounded()) return false;
//
//        Animation<TextureRegion> deathAnimation = assets.playerDieAnimation;
//        float duration = deathAnimation.getAnimationDuration();
//        float deathAnimTime = 7f;
//
//        deathTime += dt;
//        keyframe = deathAnimation.getKeyFrame(deathTime);
//        if (deathTime > deathAnimTime) {
//            dead = false;
//            deathTime = -1;
//        }

        return false;
    }

    public void possess(EnemyEntity enemy) {
        // prevent the 'tutorial' popup if you've figured how to possess someone
        screen.doorTutorialShown = true;
        if (capturedEnemy != null) {
            capturedEnemy.reset(imageBounds);
        }

        capturedEnemy = enemy;

        float scale = SCALE;
        if (capturedEnemy != null) {
            animationSet = capturedEnemy.animationSet;
            jumpVelocity = (capturedEnemy.jumpVelocity > 0) ? capturedEnemy.jumpVelocity : 150;
            damage = capturedEnemy.damage;
            bulletSize = capturedEnemy.bulletSize;
            bulletSpeed = capturedEnemy.bulletSpeed;
            scale = capturedEnemy.scale;
        } else {
            animationSet = defaultAnimationSet;
            jumpVelocity = 0;
            damage = 0;
            bulletSize = 0;
            bulletSpeed = 0;
            attackHeat = 0;
        }

        setGrounded(false);
        state = State.standing;
        setAnimation(animationSet.IdleAnimation);

        float height = keyframe.getRegionHeight() * scale;

        float y = position.y + (height - imageBounds.height)/2;

        initEntity(position.x, y, keyframe.getRegionWidth() * scale, height);

    }
}
