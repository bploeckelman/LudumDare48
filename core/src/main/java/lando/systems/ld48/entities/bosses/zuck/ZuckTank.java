package lando.systems.ld48.entities.bosses.zuck;

import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld48.Assets;
import lando.systems.ld48.entities.Bullet;
import lando.systems.ld48.entities.Player;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.BossPhase;
import lando.systems.ld48.screens.GameScreen;

public class ZuckTank extends Boss {

    final GameScreen screen;
    final Assets assets;

    final Vector2 position;
    final Rectangle imageBounds;
    final Rectangle collisionBounds;

    final Array<Missile> missiles;

    boolean alive;
    BossPhase currentPhase;

    Animation<TextureRegion> animation;
    Animations animations;
    float stateTime;
    MutableFloat alpha = new MutableFloat(1f);

    int numHits;
    final int numHitsToBeKilled = 10;

    boolean flip = false;

    static class Animations {
        public Animation<TextureRegion> missile;
        public Animation<TextureRegion> lower;
        public Animation<TextureRegion> talk;
        public Animation<TextureRegion> idleA;
        public Animation<TextureRegion> idleB;
        public Animation<TextureRegion> shoot;
        public Animation<TextureRegion> ramTell;
        public Animation<TextureRegion> ramAct;
    }

    public ZuckTank(GameScreen screen, float x, float y) {
        this.animations = new Animations();
        this.animations.missile = screen.game.assets.zuckTankMissileAnimation;
        this.animations.lower   = screen.game.assets.zuckTankLowerAnimation;
        this.animations.talk    = screen.game.assets.zuckTankTalkAnimation;
        this.animations.idleA   = screen.game.assets.zuckTankIdleAAnimation;
        this.animations.idleB   = screen.game.assets.zuckTankIdleBAnimation;
        this.animations.shoot   = screen.game.assets.zuckTankShootAnimation;
        this.animations.ramTell = screen.game.assets.zuckTankRamTellAnimation;
        this.animations.ramAct  = screen.game.assets.zuckTankRamActAnimation;

        this.stateTime = 0f;
        this.animation = animations.idleA;

        this.screen = screen;
        this.assets = screen.game.assets;
        this.position = new Vector2();
        this.imageBounds = new Rectangle();
        this.collisionBounds = new Rectangle();
        this.missiles = new Array<>();

        setPosition(x, y);

        this.currentPhase = new IdlePhase(this);

        this.alive = true;
    }

    public void setPosition(float x, float y) {
        float scale = 1f;
        float width  = scale * animation.getKeyFrames()[0].getRegionWidth();
        float height = scale * animation.getKeyFrames()[0].getRegionHeight();

        position.set(x, y);
        imageBounds.set(x - width / 2, y - height / 2, width, height);
        float margin = (1f / 3f) * imageBounds.width;
        collisionBounds.set(imageBounds.x + margin, imageBounds.y, imageBounds.width - 2f * margin, imageBounds.height);
    }

    @Override
    public void update(float dt) {
        if (!alive) return;

        stateTime += dt;

        // check for hits
        for (Bullet bullet : screen.bullets) {
            if (collisionBounds.contains(bullet.position)) {
                numHits++;
                screen.particles.smoke(bullet.position.x, bullet.position.y);
                bullet.dead = true;
                screen.bullets.removeValue(bullet, true);
                screen.physicsEntities.removeValue(bullet, true);
            }
        }

        // check for missiles hitting player or going out of bounds or something?
        Player player = screen.player;
        for (int i = missiles.size - 1; i >= 0; i--) {
            Missile missile = missiles.get(i);
            missile.update(dt);
            if (Intersector.overlaps(missile.bounds, player.collisionBounds)) {
                player.hitPoints -= 5;
                screen.particles.physics(missile.bounds.x, missile.bounds.y);
                missiles.removeIndex(i);
            }
            if (missile.lifetime <= 0) {
                missiles.removeIndex(i);
            }
        }

        if (numHits == numHitsToBeKilled && !(currentPhase instanceof DeathPhase)) {
            currentPhase = new DeathPhase(this);
        }

        if (currentPhase != null) {
            currentPhase.update(dt);
            if (currentPhase.isComplete()) {
                currentPhase = currentPhase.nextPhase();
                if (currentPhase == null) {
                    alive = false;
                    // TODO: play a sound (victory)
                    removeFromScreen();
                }
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion keyframe = animation.getKeyFrame(stateTime);
        // handle death throes
        if (flip) {
            flip = false;
            for (TextureRegion frame : animation.getKeyFrames()) {
                frame.flip(true, false);
            }
        }
        batch.setColor(1f, 1f, 1f, alpha.floatValue());
        batch.draw(keyframe, imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height);
        batch.setColor(Color.WHITE);

        missiles.forEach(missile -> missile.draw(batch));

        if (currentPhase != null) {
            currentPhase.render(batch);
        }
    }

    @Override
    public void renderDebug(SpriteBatch batch) {
        batch.setColor(Color.YELLOW);
        assets.debugNinePatch.draw(batch, imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height);
        batch.setColor(Color.RED);
        assets.debugNinePatch.draw(batch, collisionBounds.x, collisionBounds.y, collisionBounds.width, collisionBounds.height);
        batch.setColor(Color.WHITE);
    }

    @Override
    public void addToScreen() {
        screen.boss = this;
    }

    @Override
    public void removeFromScreen() {
        if (screen.boss == this) {
            screen.boss = null;
        }
    }

    static class Missile {
        public Circle bounds = new Circle();
        public Vector2 velocity = new Vector2();
        public float speed = 300f;
        public TextureRegion keyframe = null;
        public Animation<TextureRegion> missileAnim = null;
        public float stateTime = 0f;
        public float lifetime = 5f;
        public Missile(ZuckTank zuck, float velX, float velY) {
            bounds.set(zuck.position.x, zuck.position.y, 22f);
            velocity.set(velX, velY).nor();
            missileAnim = zuck.animations.missile;
        }
        public void update(float dt) {
            lifetime -= dt;
            stateTime += dt;
            keyframe = missileAnim.getKeyFrame(stateTime);
            float speedX = speed * velocity.x * dt;
            float speedY = speed * velocity.y * dt;
            bounds.setPosition(bounds.x + speedX, bounds.y + speedY);
        }
        public void draw(SpriteBatch batch) {
            if (keyframe == null) return;
            batch.draw(keyframe, bounds.x - keyframe.getRegionWidth() / 2f, bounds.y - keyframe.getRegionHeight() / 2f);
        }
    }

}
