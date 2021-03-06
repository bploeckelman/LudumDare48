package lando.systems.ld48.entities.enemies;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld48.entities.EnemyEntity;
import lando.systems.ld48.screens.GameScreen;

public class Gray extends EnemyEntity {

    private static final float SCALE = 0.75f;

    protected float attackDuration = 0.3f;
    protected float attackHeat = 0.45f;

    private float moveTimer = MathUtils.random(10f, 15f);
    private float turnTimer = MathUtils.random(1f, 3f);

    public Gray(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.grayAnimation, SCALE, x, y);

        animationSet.MoveAnimation = assets.grayMoveAnimation;
        animationSet.FallAnimation = assets.grayFallAnimation;
        animationSet.AttackAnimation = assets.grayAttackAnimation;
        animationSet.DieAnimation = assets.grayDieAnimation;

        setJump(assets.grayJumpAnimation, 250);
    }
}