package lando.systems.ld48.entities.bosses.musk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.BossPhase;

public class IdlePhase extends BossPhase {

    private final MuskKrang muskKrang;
    private float timer = 0f;

    public IdlePhase(Boss boss) {
        super(boss, () -> new MissilePhase(boss));
        this.muskKrang = (MuskKrang) boss;

        muskKrang.animation = muskKrang.animations.idleA;
        muskKrang.stateTime = 0f;
        Gdx.app.log("idle phase", "started");
    }

    @Override
    public void update(float dt) {
        timer += dt;
        if (timer > 4f || muskKrang.numHits > 10) {
            complete = true;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
    }

}
