package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.ld48.Audio;
import lando.systems.ld48.screens.GameScreen;

public class MovableEntity extends GameEntity {

    public static float JUMP_BONUS = 0.4f;


    private float fallTime = 0;

    public boolean jumpHeld = false;
    private float jumpTime = 1f;
    private float jumpVelocity = 0f;
    private float jumpKeyHeldTimer = 0f;

    private float attackTime = 1f;

    private float deathTime = 1f;

    public int id;
    public boolean ignore = false;

    protected MovableEntity(GameScreen screen, AnimationSet animSet) {
        super(screen, animSet);
    }

    public void setJump(float jumpVelocity) {
        this.jumpVelocity = jumpVelocity;
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

        if (state == State.dying) {
            deathTime += dt;
            if (animationSet.DieAnimation == null || deathTime > animationSet.DieAnimation.getAnimationDuration()) {
                this.dead = true;
            }
            return;
        }

        if (velocity.y < -50 || state == State.falling) {
            state = State.falling;
        } else {
            fallTime = 0;
        }

        if (state == State.jump || state == State.jumping) {
            jumpTime += dt;
            if (jumpHeld) {
                jumpKeyHeldTimer += dt;
            }
            if (state == State.jumping && jumpTime > 0.15f) {
                float bonusJump = Math.min(jumpKeyHeldTimer / 0.15f, 1) * JUMP_BONUS;
                velocity.y = jumpVelocity * (1f + bonusJump);
                state = State.jump;
            }
        } else {
            jumpTime = 1f;
        }

        if (state != State.jumping && jumpTime >= 0.2 && state != State.attacking) {
            // stop if entity gets slow enough
            if (Math.abs(velocity.x) < 10f && grounded) {
                velocity.x = 0f;
                state = State.idling;
            }
        }

        if (state == State.attacking) {
            attackTime += dt;
            if (animationSet.getAnimation(State.attacking) == null || attackTime > animationSet.getAnimation(State.attacking).getAnimationDuration()) {
                state = Math.abs(velocity.x) > 10 ? State.moving : State.idling;
            }
        }


        lastState = state;


    }

    public void move(Direction direction, float moveSpeed) {
        float speed = (direction == Direction.left) ? -moveSpeed : moveSpeed;
        this.direction = direction;
        velocity.add(speed, 0);

        if (state != State.jumping && jumpTime >= 0.2 && state != State.attacking && grounded) {
            state = State.moving;
        }
    }

    public void jump() {
        if (state != State.jump && state != State.jumping && state != State.attacking && grounded) {
            screen.game.audio.playSound(Audio.Sounds.jump);
            jumpTime = 0;
            jumpKeyHeldTimer = 0;
            state = State.jumping;
        }
    }

    public void attack() {
        if (this.state == State.idling || this.state == State.moving) {
            screen.game.audio.playSound(Audio.Sounds.attack);
            attackTime = 0;
            state = State.attacking;
        }
    }

    public void die() {
        screen.game.audio.playSound(Audio.Sounds.death);
        deathTime = 0;
        this.state = State.dying;
    }

}
