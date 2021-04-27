package lando.systems.ld48.entities.bosses.musk;

import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld48.entities.InteractableEntity;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.BossPhase;
import lando.systems.ld48.levels.SpawnInteractable;

public class DeathPhase extends BossPhase {

    final MuskKrang muskKrang;

    boolean fading = false;
    float timer = 6f;

    float flipDuration = 1f;
    float flipTimer = flipDuration;

    public DeathPhase(Boss boss) {
        super(boss);
        this.muskKrang = (MuskKrang) boss;

        muskKrang.animation = muskKrang.animations.idleB;
        muskKrang.animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        muskKrang.stateTime = 0f;

        // TODO: flip horizontally repeatedly

        Gdx.app.log("death phase", "started");
    }

    @Override
    public void update(float dt) {
        timer -= dt;
        if (timer <= 0 && !fading) {
            fading = true;
            // spawn particles
            muskKrang.screen.particles.physics(muskKrang.position.x, muskKrang.position.y);
            muskKrang.screen.particles.smoke(muskKrang.position.x, muskKrang.position.y);

            // open door
            for (InteractableEntity interactable : muskKrang.screen.interactables) {
                if (interactable.type == SpawnInteractable.Type.door && interactable.disabled) {
                    interactable.disabled = false;
                    interactable.interact();
                }
            }

            Tween.to(muskKrang.alpha, -1, 2f)
                    .target(0f).setCallback(((type, source) -> {
                        muskKrang.screen.particles.smoke(muskKrang.position.x, muskKrang.position.y);
                        muskKrang.removeFromScreen();
                        complete = true;
                    }))
                    .start(muskKrang.screen.game.tween);
        } else {
            // flip back and forth crazily
            flipTimer -= dt;
            if (flipTimer <= 0f) {
                muskKrang.flip = true;
                flipDuration = MathUtils.clamp(flipDuration -= 0.1f, 0.2f, 1f);
                flipTimer = flipDuration;
                muskKrang.screen.particles.smoke(muskKrang.position.x, muskKrang.position.y);
            }
        }
    }
}
