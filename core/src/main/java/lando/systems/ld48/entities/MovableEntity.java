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

    public AnimationSet animationSet = new AnimationSet();

    private float fallTime = 0;

    private float jumpTime = -1;
    private float jumpVelocity = 0f;
    private float jumpKeyHeldTimer = 0f;

    public int id;
    public boolean ignore = false;

    protected MovableEntity(GameScreen screen, Animation<TextureRegion> idle, Animation<TextureRegion> move) {
        super(screen, idle);

        animationSet.IdleAnimation = idle;
        animationSet.MoveAnimation = move;

        lastState = state;
    }

    public void setJump(Animation<TextureRegion> jumpAnimation, float jumpVelocity) {
        animationSet.JumpAnimation = jumpAnimation;
        this.jumpVelocity = jumpVelocity;
    }

    public void setFall(Animation<TextureRegion> fallAnimation) {
        animationSet.FallAnimation = fallAnimation;
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
                setAnimation(animationSet.IdleAnimation);
            } else if (state == State.walking) {
                setAnimation(animationSet.MoveAnimation);
            }

            fallTime = 0;
            lastState = state;
        }

        updateFall(dt);
        updateJump(dt);
    }

    private void updateFall(float dt) {
        if (state == State.falling && animationSet.FallAnimation != null) {
            fallTime += dt;
            keyframe = animationSet.FallAnimation.getKeyFrame(fallTime);
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
        if (animationSet.JumpAnimation != null) {
            keyframe = animationSet.JumpAnimation.getKeyFrame(jumpTime);
        }

        boolean jumpCompleted = (jumpTime > animationSet.JumpAnimation.getAnimationDuration());
        if (state == State.jumping && (animationSet.JumpAnimation == null || jumpCompleted)) {
            float bonusJump = (jumpKeyHeldTimer / animationSet.JumpAnimation.getAnimationDuration()) * JUMP_BONUS;
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
