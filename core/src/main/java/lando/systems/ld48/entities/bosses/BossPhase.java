package lando.systems.ld48.entities.bosses;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// TODO : make abstract class
public interface BossPhase {
    void update(float dt);
    void render(SpriteBatch batch);
    boolean isComplete();
    BossPhase nextPhase();
}
