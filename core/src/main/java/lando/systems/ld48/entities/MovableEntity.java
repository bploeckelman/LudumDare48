package lando.systems.ld48.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.ld48.screens.GameScreen;

public class MovableEntity extends GameEntity {

    public static float JUMP_BONUS = 0.4f;

    private State lastState;

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> moveAnimation;

    private float fallTime = 0;
    private Animation<TextureRegion> fallAnimation;

    private float jumpTime = -1;
    private float jumpVelocity = 0f;
    private float jumpKeyHeldTimer = 0f;
    private Animation<TextureRegion> jumpAnimation;

    public int id;
    public boolean ignore = false;

    protected MovableEntity(GameScreen screen, Animation<TextureRegion> idle, Animation<TextureRegion> move) {
        super(screen, idle);

        idleAnimation = idle;
        moveAnimation = move;

        lastState = state;
    }

    public void setJump(Animation<TextureRegion> jumpAnimation, float jumpVelocity) {
        this.jumpAnimation = jumpAnimation;
        this.jumpVelocity = jumpVelocity;
    }

    public void setFall(Animation<TextureRegion> fallAnimation) {
        this.fallAnimation = fallAnimation;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (ignore) return;
        super.render(batch);
    }

    @Override
    public void update(float dt) {
        if (ignore) return;
        super.update(dt);

        if (velocity.y < -50) {
            state = State.falling;
            jumpTime = -1;
        }

        if (lastState != state) {
            if (state == State.standing) {
                setAnimation(idleAnimation);
            } else if (state == State.walking) {
                setAnimation(moveAnimation);
            }

            fallTime = 0;
            lastState = state;
        }

        updateFall(dt);
        updateJump(dt);
    }

    private void updateFall(float dt) {
        if (state == State.falling && fallAnimation != null) {
            fallTime += dt;
            keyframe = fallAnimation.getKeyFrame(fallTime);
        } else {
            fallTime = 0;
        }
    }

    private void updateJump(float dt) {
        if (jumpTime == -1) return;

        // TODO: consolidate input checking into GameScreen
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            jumpKeyHeldTimer += dt;
        }

        jumpTime += dt;
        if (jumpAnimation != null) {
            keyframe = jumpAnimation.getKeyFrame(jumpTime);
        }

        boolean jumpCompleted = (jumpTime > jumpAnimation.getAnimationDuration());
        if (state == State.jumping && (jumpAnimation == null || jumpCompleted)) {
            float bonusJump = (jumpKeyHeldTimer / jumpAnimation.getAnimationDuration()) * JUMP_BONUS;
            velocity.y = jumpVelocity * (1f + bonusJump);
            state = State.jump;
        }
    }

    public void jump() {
        if (jumpTime == -1 && grounded) {
            jumpTime = 0;
            jumpKeyHeldTimer = 0;
            state = State.jumping;
        }
    }

}
