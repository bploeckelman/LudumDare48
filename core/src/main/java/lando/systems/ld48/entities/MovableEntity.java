package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.ld48.Audio;
import lando.systems.ld48.screens.GameScreen;

public class MovableEntity extends GameEntity {

    public static float JUMP_BONUS = 0.4f;

    private State lastState;

    private float fallTime = 0;

    public boolean jumpHeld = false;
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

        if (velocity.y < -50 || state == State.falling) {
            state = State.falling;
            fallTime += dt;
            if (animationSet.FallAnimation != null) {
                keyframe = animationSet.FallAnimation.getKeyFrame(fallTime);
            }
        } else {
            fallTime = 0;
        }

        if (state == State.jump || state == State.jumping) {
            jumpTime += dt;
            if (jumpHeld) {
                jumpKeyHeldTimer += dt;
            }
            if (animationSet.JumpAnimation != null) {
                keyframe = animationSet.JumpAnimation.getKeyFrame(jumpTime);
            }
            if (state == State.jumping && (animationSet.JumpAnimation == null || jumpTime > animationSet.JumpAnimation.getAnimationDuration())) {
                float bonusJump = animationSet.JumpAnimation == null ? 0 : Math.min(jumpKeyHeldTimer / animationSet.JumpAnimation.getAnimationDuration(), 1) * JUMP_BONUS;
                velocity.y = jumpVelocity * (1f + bonusJump);
                state = State.jump;
            }
        }

        if (state == State.standing && lastState != State.standing) {
            setAnimation(animationSet.IdleAnimation);
        }

        if (state == State.walking && lastState != State.walking) {
            setAnimation(animationSet.MoveAnimation);
        }

        lastState = state;


    }

    public void jump() {
        if (state != State.jump && state != State.jumping && grounded) {
            screen.game.audio.playSound(Audio.Sounds.jump);
            jumpTime = 0;
            jumpKeyHeldTimer = 0;
            state = State.jumping;
        }
    }

}
