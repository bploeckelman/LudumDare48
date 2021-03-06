package lando.systems.ld48.entities.enemies;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld48.entities.EnemyEntity;
import lando.systems.ld48.screens.GameScreen;

public class Soldier extends EnemyEntity {

    private static final float SCALE = 0.75f;

    private float moveTimer = MathUtils.random(10f, 15f);
    private float turnTimer = MathUtils.random(1f, 3f);

    public Soldier(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.soldierAnimation, SCALE, x, y);

        animationSet.MoveAnimation = assets.soldierMoveAnimation;
        animationSet.FallAnimation = assets.soldierFallAnimation;
        animationSet.AttackAnimation = assets.soldierAttackAnimation;
        animationSet.DieAnimation = assets.soldierDieAnimation;

        setJump(assets.soldierJumpAnimation, 200);
    }
}
