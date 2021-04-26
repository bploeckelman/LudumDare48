package lando.systems.ld48.entities.bosses.zuck;

import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld48.entities.InteractableEntity;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.BossPhase;
import lando.systems.ld48.levels.SpawnInteractable;

public class DeathPhase extends BossPhase {

    final ZuckTank zuck;

    boolean fading = false;
    float timer = 6f;

    float flipDuration = 1f;
    float flipTimer = flipDuration;

    public DeathPhase(Boss boss) {
        super(boss);
        this.zuck = (ZuckTank) boss;

        zuck.animation = zuck.animations.talk;
        zuck.animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        zuck.stateTime = 0f;

        // TODO: flip horizontally repeatedly

        Gdx.app.log("death phase", "started");
    }

    @Override
    public void update(float dt) {
        timer -= dt;
        if (timer <= 0 && !fading) {
            fading = true;
            // spawn particles
            zuck.screen.particles.physics(zuck.position.x, zuck.position.y);
            zuck.screen.particles.smoke(zuck.position.x, zuck.position.y);

            // open door
            for (InteractableEntity interactable : zuck.screen.interactables) {
                if (interactable.type == SpawnInteractable.Type.door && interactable.disabled) {
                    interactable.disabled = false;
                    interactable.interact();
                }
            }

            Tween.to(zuck.alpha, -1, 2f)
                    .target(0f).setCallback(((type, source) -> {
                        zuck.screen.particles.smoke(zuck.position.x, zuck.position.y);
                        zuck.removeFromScreen();
                        complete = true;
                    }))
                    .start(zuck.screen.game.tween);
        } else {
            // flip back and forth crazily
            flipTimer -= dt;
            if (flipTimer <= 0f) {
                zuck.flip = true;
                flipDuration = MathUtils.clamp(flipDuration -= 0.1f, 0.2f, 1f);
                flipTimer = flipDuration;
                zuck.screen.particles.smoke(zuck.position.x, zuck.position.y);
            }
        }
    }
}
