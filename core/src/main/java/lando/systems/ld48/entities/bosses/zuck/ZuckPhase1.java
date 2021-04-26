package lando.systems.ld48.entities.bosses.zuck;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld48.entities.bosses.BossPhase;

public class ZuckPhase1 implements BossPhase {

    private final ZuckTank zuck;
    private boolean complete;
    private float timer = 0f;

    // TODO: pass in Callback for nextPhase so it can be changed when transitioning between phases
    public ZuckPhase1(ZuckTank zuck) {
        this.zuck = zuck;
        this.complete = false;

        zuck.animation = zuck.animations.idleA;
        zuck.stateTime = 0f;
    }

    @Override
    public void update(float dt) {
        timer += dt;
        if (timer > 10f || zuck.numHits > 4) {
            complete = true;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public BossPhase nextPhase() {
        return new ZuckPhase2(zuck);
    }

}
