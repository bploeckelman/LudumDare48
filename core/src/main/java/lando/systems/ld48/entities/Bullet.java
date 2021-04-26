package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.*;
import lando.systems.ld48.Audio;

public class Bullet extends GameEntity {

    public float bulletSpeed;
    public int damage;
    public GameEntity owner;
    private float yInit;

    public Bullet(GameEntity owner, Vector2 pos) {
        super(owner.screen, owner.assets.cat);

        this.gravityModifier = 0;
        this.owner = owner;
        maxHorizontalVelocity = 400;
        direction = owner.direction;
        this.bulletSpeed = owner.bulletSpeed * ((direction == Direction.left) ? -1 : 1);
        this.damage = owner.damage;
        this.velocity.set(this.bulletSpeed, 0);
        initEntity(pos.x, pos.y, keyframe.getRegionWidth() * 0.15f, keyframe.getRegionHeight() * 0.15f);

        this.addToScreen(pos.x, pos.y);
        yInit = pos.y;
    }

    @Override
    public void update(float dt) {
        velocity.set(bulletSpeed, 0);
        renderRotation += 270 * dt;
        super.update(dt);
        if (this.position.y != yInit) { this.dead = true; }
    }

    // cheap, but ok.
    private int count = 0;
    @Override
    public void onCollision() {
        if (this.dead) { return; }
        if (count++ > 2) {
            dead = true;
            screen.game.audio.playSound(Audio.Sounds.bulletHit);
        }
    }

    @Override
    public void addToScreen(float x, float y) {
        super.addToScreen(x, y);
        screen.bullets.add(this);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!this.dead) { super.render(batch); }
    }

}
