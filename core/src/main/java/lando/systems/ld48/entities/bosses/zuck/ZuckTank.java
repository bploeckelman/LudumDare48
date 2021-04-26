package lando.systems.ld48.entities.bosses.zuck;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.BossPhase;
import lando.systems.ld48.screens.GameScreen;

public class ZuckTank extends Boss {

    private final GameScreen screen;
    private final Vector2 position;
    private final Rectangle collisionBounds;

    private boolean alive;
    private BossPhase currentPhase;
    private Animation<TextureRegion> animation;
    private Animations animations;
    private float stateTime;

    static class Animations {
        Animation<TextureRegion> missile;
        Animation<TextureRegion> lower;
        Animation<TextureRegion> talk;
        Animation<TextureRegion> idleA;
        Animation<TextureRegion> idleB;
        Animation<TextureRegion> shoot;
        Animation<TextureRegion> ramTell;
        Animation<TextureRegion> ramAct;
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
        float width  = this.animation.getKeyFrames()[0].getRegionWidth();
        float height = this.animation.getKeyFrames()[0].getRegionHeight();

        this.screen = screen;
        this.position = new Vector2(x, y);
        this.collisionBounds = new Rectangle(x - width / 2, y - height / 2, width, height);
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
        if (stateTime > animation.getAnimationDuration()) {
            stateTime = 0f;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion keyframe = animation.getKeyFrame(stateTime);
        batch.draw(keyframe, collisionBounds.x, collisionBounds.y, collisionBounds.width, collisionBounds.height);
    }

    @Override
    public void addToScreen() {
        screen.boss = this;
    }

}
