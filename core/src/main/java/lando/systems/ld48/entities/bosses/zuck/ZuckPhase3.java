package lando.systems.ld48.entities.bosses.zuck;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.BossPhase;

public class ZuckPhase3 extends BossPhase {

    private final ZuckTank zuck;
    private float timer = 0f;

    public ZuckPhase3(Boss boss) {
        super(boss, () -> new LazerEyesPhase(boss));
        this.zuck = (ZuckTank) boss;

        zuck.animation = zuck.animations.idleB;
        zuck.stateTime = 0f;
    }

    @Override
    public void update(float dt) {
        timer += dt;
        if (timer > 5f) {
            complete = true;
        }
    }

    @Override
    public void render(SpriteBatch batch) {

    }

}
