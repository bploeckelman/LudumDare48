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
    private float jumpTime = 1f;
    protected float jumpVelocity = 0f;
    private float jumpKeyHeldTimer = 0f;

    private float attackCD = 0f;
    private float burstCD = 0f;
    protected float attackDuration = 0.5f;
    private float currentHeat = 0f;
    protected float attackHeat = 0.7f;

    private float deathTime = 1f;

    public int id;
    public boolean ignore = false;

    protected MovableEntity(GameScreen screen, Animation<TextureRegion> idle) {
        this(screen, idle, idle);
    }

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

    public void setAttack(Animation<TextureRegion> attackAnimation) {
        animationSet.AttackAnimation = attackAnimation;
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

        // todo: move to updateState
        if (state == State.death) {
            deathTime += dt;
            if (animationSet.DieAnimation != null) {
                keyframe = animationSet.DieAnimation.getKeyFrame(deathTime);
            }
            if (animationSet.DieAnimation == null || deathTime > animationSet.DieAnimation.getAnimationDuration()) {
                this.dead = true;
            }
            return;
        }

        attackCD = Math.max(0, attackCD - dt);
        burstCD = Math.max(0, burstCD - dt);
        currentHeat = Math.max(0, currentHeat - dt);

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
        } else {
            jumpTime = 1f;
        }

        if (state != State.jumping && jumpTime >= 0.2 && state != State.attacking) {
            // stop if entity gets slow enough
            if (Math.abs(velocity.x) < 10f && isGrounded()) {
                velocity.x = 0f;
                state = State.standing;
            }
        }

//        if (state == State.attacking) {
//            attackTime += dt;
//            if (animationSet.AttackAnimation != null) {
//                keyframe = animationSet.AttackAnimation.getKeyFrame(attackTime);
//            }
//            if (animationSet.AttackAnimation == null || attackTime > animationSet.AttackAnimation.getAnimationDuration()) {
//                state = Math.abs(velocity.x) > 10 ? State.walking : State.standing;
//            }
//        }

        if (this.attackCD != 0 && this.attackCD + this.animationSet.AttackAnimation.getAnimationDuration() > this.attackDuration) {
            this.keyframe = animationSet.AttackAnimation.getKeyFrame(attackDuration - attackCD);
        }

        if (state == State.standing && lastState != State.standing) {
            setAnimation(animationSet.IdleAnimation);
        }

        if (state == State.walking && lastState != State.walking) {
            setAnimation(animationSet.MoveAnimation);
        }


        lastState = state;


    }

    public void move(Direction direction, float moveSpeed) {
        float speed = (direction == Direction.left) ? -moveSpeed : moveSpeed;
        this.direction = direction;
        velocity.add(speed, 0);

        if (state != State.jumping && jumpTime >= 0.2 && state != State.attacking && isGrounded()) {
            state = State.walking;
        }
    }

    public void jump() {
        if (state != State.jump && state != State.jumping && state != State.attacking && isGrounded() && animationSet.JumpAnimation != null) {
            screen.game.audio.playSound(Audio.Sounds.jump);
            jumpTime = 0;
            jumpKeyHeldTimer = 0;
            state = State.jumping;
        }
    }

    public void attack() {
        if (this.animationSet.AttackAnimation != null && this.attackCD == 0 && burstCD == 0) {
            screen.game.audio.playSound(Audio.Sounds.attack);
            attackCD = attackDuration;
            new Bullet(this, position);
            this.currentHeat += attackHeat;
            if (this.currentHeat > 2) {
                this.burstCD = currentHeat;
            }
        }
    }

    public void die() {
        screen.game.audio.playSound(Audio.Sounds.death);
        deathTime = 0;
        this.state = State.death;
    }

}
