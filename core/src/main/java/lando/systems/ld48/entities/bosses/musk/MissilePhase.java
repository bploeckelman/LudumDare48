package lando.systems.ld48.entities.bosses.musk;

import com.badlogic.gdx.Gdx;
import lando.systems.ld48.Audio;
import lando.systems.ld48.entities.Player;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.BossPhase;

public class MissilePhase extends BossPhase {

    final MuskKrang muskKrang;
    final float timeBetweenShots = 1f;

    int numShots = 1;
    float timer = timeBetweenShots;

    public MissilePhase(Boss boss) {
        super(boss, () -> new IdlePhase(boss));
        this.muskKrang = (MuskKrang) boss;

        muskKrang.animation = muskKrang.animations.shootA;
        muskKrang.stateTime = 0f;

        muskKrang.screen.game.audio.playSound(Audio.Sounds.laser);

        Gdx.app.log("missile phase", "started");
    }

    @Override
    public void update(float dt) {
        timer -= dt;
        if (timer <= 0f) {
            timer = timeBetweenShots;

            Player player = muskKrang.screen.player;
            float zuckMouthX = muskKrang.position.x + 90f;
            float zuckMouthY = muskKrang.position.y + 55f;
            float velX = player.position.x - zuckMouthX;
            float velY = player.position.y - zuckMouthY;

            MuskKrang.Missile missile = new MuskKrang.Missile(muskKrang, muskKrang.animations.missileA, velX, velY);
            muskKrang.missiles.add(missile);
            numShots--;

            Gdx.app.log("missile phase", "shoot your shot");
        }
        if (numShots == 0) {
            complete = true;
        }
    }

}
