package lando.systems.ld48.entities.bosses.zuck;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.BossPhase;

public class IdlePhase extends BossPhase {

    private final ZuckTank zuck;
    private float timer = 0f;

    public IdlePhase(Boss boss) {
        super(boss, () -> new LazerEyesPhase(boss));
        this.zuck = (ZuckTank) boss;

        zuck.animation = zuck.animations.idleA;
        zuck.stateTime = 0f;
        Gdx.app.log("idle phase", "started");
    }

    @Override
    public void update(float dt) {
        timer += dt;
        if (timer > 5f || zuck.numHits > 10) {
            complete = true;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
    }

}
