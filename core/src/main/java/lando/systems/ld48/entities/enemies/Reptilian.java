package lando.systems.ld48.entities.enemies;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld48.entities.EnemyEntity;
import lando.systems.ld48.screens.GameScreen;

public class Reptilian extends EnemyEntity {

    private static final float SCALE = 0.75f;

    private float moveTimer = MathUtils.random(10f, 15f);
    private float turnTimer = MathUtils.random(1f, 3f);

    public Reptilian(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.reptilianAnimation, SCALE, x, y);

        animationSet.MoveAnimation = assets.reptilianMoveAnimation;
        animationSet.FallAnimation = assets.reptilianFallAnimation;
        animationSet.AttackAnimation = assets.reptilianAttackAnimation;
        animationSet.DieAnimation = assets.reptilianDieAnimation;
        maxHorizontalVelocity = 20;
        setJump(assets.reptilianJumpAnimation, 250);
    }
}
