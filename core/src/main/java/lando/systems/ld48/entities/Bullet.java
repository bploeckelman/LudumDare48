package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Pool;
import lando.systems.ld48.physics.PhysicsComponent;
import lando.systems.ld48.screens.GameScreen;

public class Bullet extends GameEntity {

    public float bulletSpeed;
    public GameEntity owner;
    public float timeToLive;


    public Bullet(GameEntity owner, Vector2 pos, Vector2 v) {
        super(owner.screen, owner.assets.cat);
        this.owner = owner;
        this.bulletSpeed = owner.bulletSpeed;
        this.timeToLive = owner.bulletTimeToLive;
        this.addToScreen(pos.x, pos.y);
        this.velocity.set(v);
        v.nor();
        initEntity(pos.x, pos.y, keyframe.getRegionWidth() * 0.15f, keyframe.getRegionHeight() * 0.15f);
    }

    public void update(float dt) {
        super.update(dt);
        if (velocity.len() < bulletSpeed / 2 || velocity.y != 0) { dead = true; }
        velocity.set(velocity.x * bulletSpeed, 0);
    }

    public boolean isGrounded() {
        return true;
    }

    public boolean checkCollision(GameEntity entity) {
        return (position.dst(entity.position) < entity.collisionBounds.width / 2);
    }

}

