package lando.systems.ld48.entities.bosses.musk;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld48.entities.Player;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.BossPhase;
import lando.systems.ld48.utils.accessors.Vector2Accessor;

public class RamPhase extends BossPhase {

    private final MuskKrang muskKrang;
    private Vector2 zoom;
    private float timer;
    private float iFramesTimer;
    private float returnPosX;
    private boolean isZoomZoom;

    public RamPhase(Boss boss) {
        super(boss, () -> new MissilePhase(boss));
        this.muskKrang = (MuskKrang) boss;
        this.iFramesTimer = 0f;
        this.zoom = new Vector2();
        this.isZoomZoom = false;

        muskKrang.animation = muskKrang.animations.idleB;
        muskKrang.stateTime = 0f;
        this.timer = muskKrang.animation.getAnimationDuration() * 2f;

        Gdx.app.log("ram phase", "started");
    }

    @Override
    public void update(float dt) {
        timer -= dt;
        if (timer <= 0) {
            zoom.x = muskKrang.screen.player.position.x;

//            if (muskKrang.animation == muskKrang.animations.idleB) {
//                Gdx.app.log("ram phase", "lower");
//                muskKrang.animation = muskKrang.animations.lower;
//                muskKrang.stateTime = 0f;
//                timer = muskKrang.animation.getAnimationDuration();
//            }
//            else if (muskKrang.animation == muskKrang.animations.lower) {
//                Gdx.app.log("ram phase", "ram tell");
//                muskKrang.animation = muskKrang.animations.ramTell;
//                muskKrang.stateTime = 0f;
//                timer = muskKrang.animation.getAnimationDuration();
//            }
//            else if (muskKrang.animation == muskKrang.animations.ramTell) {
//                Gdx.app.log("ram phase", "ram act");
//                muskKrang.animation = muskKrang.animations.ramAct;
//                muskKrang.stateTime = 0f;
//                timer = 4f;
//
//                zoom.x = muskKrang.position.x;
//                returnPosX = muskKrang.position.x;
//                Timeline.createSequence()
//                        .push(Tween.call((type, source) -> isZoomZoom = true))
//                        .push(Tween.to(zoom, Vector2Accessor.X, (1f / 4f) * timer).target(returnPosX - 250))
//                        .push(Tween.to(zoom, Vector2Accessor.X, (3f / 4f) * timer).target(returnPosX))
//                        .push(Tween.call((type, source) -> {
//                            isZoomZoom = false;
//                            complete = true;
//                        }))
//                        .start(muskKrang.screen.game.tween);
 //           }
        }

//        if (isZoomZoom) {
//            muskKrang.setPosition(zoom.x, muskKrang.position.y);
//
//            if (iFramesTimer == 0f) {
//                Player player = muskKrang.screen.player;
//                if (muskKrang.collisionBounds.contains(player.collisionBounds)) {
//                    player.hitPoints -= 4;
//                    muskKrang.screen.particles.interact(player.position.x, player.position.y);
//                    iFramesTimer = 2f;
//                }
//            }
//
//            iFramesTimer -= dt;
//            if (iFramesTimer < 0f) {
//                iFramesTimer = 0f;
//            }
//        }
    }

}
