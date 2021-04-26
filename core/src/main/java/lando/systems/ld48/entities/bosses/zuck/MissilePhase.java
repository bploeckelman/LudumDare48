package lando.systems.ld48.entities.bosses.zuck;

import com.badlogic.gdx.Gdx;
import lando.systems.ld48.entities.Player;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.BossPhase;

public class MissilePhase extends BossPhase {

    final ZuckTank zuck;
    final float timeBetweenShots = 0.5f;

    int numShots = 5;
    float timer = timeBetweenShots;

    public MissilePhase(Boss boss) {
        super(boss, () -> new IdlePhase(boss));
        this.zuck = (ZuckTank) boss;

        zuck.animation = zuck.animations.talk;
        zuck.stateTime = 0f;

        Gdx.app.log("missile phase", "started");
    }

    @Override
    public void update(float dt) {
        timer -= dt;
        if (timer <= 0f) {
            timer = timeBetweenShots;

            Player player = zuck.screen.player;
            float zuckMouthX = zuck.position.x - 50f;
            float zuckMouthY = zuck.position.y - 10f;
            float velX = player.position.x - zuckMouthX;
            float velY = player.position.y - zuckMouthY;

            ZuckTank.Missile missile = new ZuckTank.Missile(zuck, velX, velY);
            zuck.missiles.add(missile);
            numShots--;

            Gdx.app.log("missile phase", "shoot your shot");
        }
        if (numShots == 0) {
            complete = true;
        }
    }

}
