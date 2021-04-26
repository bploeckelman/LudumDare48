package lando.systems.ld48.entities.bosses.zuck;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld48.Audio;
import lando.systems.ld48.entities.Player;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.BossPhase;
import lando.systems.ld48.utils.accessors.Vector2Accessor;

public class RamPhase extends BossPhase {

    private final ZuckTank zuck;
    private Vector2 zoom;
    private float timer;
    private float iFramesTimer;
    private float returnPosX;
    private boolean isZoomZoom;

    public RamPhase(Boss boss) {
        super(boss, () -> new MissilePhase(boss));
        this.zuck = (ZuckTank) boss;
        this.iFramesTimer = 0f;
        this.zoom = new Vector2();
        this.isZoomZoom = false;

        zuck.animation = zuck.animations.idleB;
        zuck.stateTime = 0f;
        this.timer = zuck.animation.getAnimationDuration() * 2f;

        Gdx.app.log("ram phase", "started");
        zuck.screen.game.audio.playSound(Audio.Sounds.zuckRam);
    }

    @Override
    public void update(float dt) {
        timer -= dt;
        if (timer <= 0) {
            zoom.x = zuck.screen.player.position.x;

            if (zuck.animation == zuck.animations.idleB) {
                Gdx.app.log("ram phase", "lower");
                zuck.animation = zuck.animations.lower;
                zuck.stateTime = 0f;
                timer = zuck.animation.getAnimationDuration();
            }
            else if (zuck.animation == zuck.animations.lower) {
                Gdx.app.log("ram phase", "ram tell");
                zuck.animation = zuck.animations.ramTell;
                zuck.stateTime = 0f;
                timer = zuck.animation.getAnimationDuration();
            }
            else if (zuck.animation == zuck.animations.ramTell) {
                Gdx.app.log("ram phase", "ram act");
                zuck.animation = zuck.animations.ramAct;
                zuck.stateTime = 0f;
                timer = 4f;

                zoom.x = zuck.position.x;
                returnPosX = zuck.position.x;
                Timeline.createSequence()
                        .push(Tween.call((type, source) -> isZoomZoom = true))
                        .push(Tween.to(zoom, Vector2Accessor.X, (1f / 4f) * timer).target(returnPosX - 250))
                        .push(Tween.to(zoom, Vector2Accessor.X, (3f / 4f) * timer).target(returnPosX))
                        .push(Tween.call((type, source) -> {
                            isZoomZoom = false;
                            complete = true;
                        }))
                        .start(zuck.screen.game.tween);
            }
        }

        if (isZoomZoom) {
            zuck.setPosition(zoom.x, zuck.position.y);

            if (iFramesTimer == 0f) {
                Player player = zuck.screen.player;
                if (zuck.collisionBounds.contains(player.collisionBounds)) {
                    player.hitPoints -= 4;
                    zuck.screen.particles.interact(player.position.x, player.position.y);
                    iFramesTimer = 2f;
                }
            }

            iFramesTimer -= dt;
            if (iFramesTimer < 0f) {
                iFramesTimer = 0f;
            }
        }
    }

}
