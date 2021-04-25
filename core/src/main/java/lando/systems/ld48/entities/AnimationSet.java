package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimationSet {
    public Animation<TextureRegion> IdleAnimation;
    public Animation<TextureRegion> MoveAnimation;
    public Animation<TextureRegion> FallAnimation;
    public Animation<TextureRegion> JumpAnimation;
    public Animation<TextureRegion> AttackAnimation;
    public Animation<TextureRegion> DieAnimation;

    public AnimationSet(Animation<TextureRegion> animation) {
        IdleAnimation = MoveAnimation = animation;
    }

    public AnimationSet(Animation<TextureRegion> Idle, Animation<TextureRegion> Move, Animation<TextureRegion> Fall,
                        Animation<TextureRegion> Jump, Animation<TextureRegion> Attack, Animation<TextureRegion> Die) {
        this.IdleAnimation = Idle;
        this.MoveAnimation = Move;
        this.FallAnimation = Fall;
        this.JumpAnimation = Jump;
        this.AttackAnimation = Attack;
        this.DieAnimation = Die;
    }
}
