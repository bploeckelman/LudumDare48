package lando.systems.ld48.entities;

import com.badlogic.gdx.math.*;

public class Bullet extends GameEntity {

    public float bulletSpeed;
    public GameEntity owner;

    public Bullet(GameEntity owner, Vector2 pos) {
        super(owner.screen, owner.assets.cat);

        this.ignoreGravity = true;
        this.owner = owner;
        maxHorizontalVelocity = 400;
        direction = owner.direction;
        this.bulletSpeed = owner.bulletSpeed * ((direction == Direction.left) ? -1 : 1);
        this.velocity.set(this.bulletSpeed, 0);
        initEntity(pos.x, pos.y, keyframe.getRegionWidth() * 0.15f, keyframe.getRegionHeight() * 0.15f);

        this.addToScreen(pos.x, pos.y);
    }

    public void update(float dt) {
        velocity.set(bulletSpeed, 0);
        super.update(dt);
    }
}
