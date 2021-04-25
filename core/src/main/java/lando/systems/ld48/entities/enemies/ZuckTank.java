package lando.systems.ld48.entities.enemies;

import lando.systems.ld48.entities.EnemyEntity;
import lando.systems.ld48.screens.GameScreen;

public class ZuckTank extends EnemyEntity {

    public ZuckTank(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.zuckTankIdleAAnimation, x, y);
    }

}
