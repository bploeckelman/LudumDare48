package lando.systems.ld48.entities.bosses.zuck;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld48.Assets;
import lando.systems.ld48.entities.Bullet;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.BossPhase;
import lando.systems.ld48.screens.GameScreen;

public class ZuckTank extends Boss {

    final GameScreen screen;
    final Assets assets;

    final Vector2 position;
    final Rectangle imageBounds;
    final Rectangle collisionBounds;

    boolean alive;
    BossPhase currentPhase;

    int numHits;

    Animation<TextureRegion> animation;
    Animations animations;
    float stateTime;

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
        batch.draw(keyframe, imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height);
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

}
