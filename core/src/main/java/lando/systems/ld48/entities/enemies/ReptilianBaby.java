package lando.systems.ld48.entities.enemies;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld48.entities.EnemyEntity;
import lando.systems.ld48.screens.GameScreen;

public class ReptilianBaby extends EnemyEntity {

    private static final float SCALE = 0.5f;

    private float moveTimer = MathUtils.random(10f, 15f);
    private float turnTimer = MathUtils.random(1f, 3f);

    public ReptilianBaby(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.reptilianBabyAnimation, SCALE, x, y);

        animationSet.MoveAnimation = assets.reptilianBabyAnimation;
        animationSet.FallAnimation = assets.reptilianBabyAnimation;
        animationSet.AttackAnimation = assets.reptilianBabyAnimation;
        animationSet.DieAnimation = assets.reptilianBabyAnimation;
        maxHorizontalVelocity = 1f;
        setJump(assets.reptilianBabyAnimation, 50f);
    }
}
