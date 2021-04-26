package lando.systems.ld48.entities.bosses;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class BossPhase {
    public interface NextPhase {
        BossPhase get();
    }

    protected Boss boss;
    protected boolean complete;
    protected NextPhase nextPhase;

    public BossPhase(Boss boss) {
        this(boss, () -> null);
    }

    public BossPhase(Boss boss, NextPhase nextPhase) {
        this.boss = boss;
        this.complete = false;
        this.nextPhase = nextPhase;
    }

    public abstract void update(float dt);
    public abstract void render(SpriteBatch batch);

    public boolean isComplete() {
        return complete;
    }
    public BossPhase nextPhase() {
        return nextPhase.get();
    }
}
