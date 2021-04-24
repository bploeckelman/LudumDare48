package lando.systems.ld48.entities.enemies;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld48.entities.EnemyEntity;
import lando.systems.ld48.screens.GameScreen;

public class Mummy extends EnemyEntity {

    private static final float SCALE = 0.75f;

    private float moveTimer = MathUtils.random(10f, 15f);
    private float turnTimer = MathUtils.random(1f, 3f);

    public Mummy(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.mummyAnimation, SCALE, x, y);
    }

    // NOTE: movement is janky, but this is temporary test stuff so no worries

    @Override
    public void update(float dt) {
        super.update(dt);
        moveTimer -= dt;
        turnTimer -= dt;

        float moveSpeed = 10f;

        if (turnTimer < 0) {
            if (Math.abs(screen.player.position.x - position.x) < 100f) {
                direction = (screen.player.position.x < position.x) ? Direction.left : Direction.right;
            } else {
                direction = MathUtils.randomBoolean() ? Direction.left : Direction.right;
            }
            turnTimer = MathUtils.random(1f, 3f);
        }
        if (moveTimer > 1f) {
            velocity.x = direction == Direction.left ? -moveSpeed : moveSpeed;
        }
//        else {
//            velocity.x = direction == Direction.left ? -moveSpeed / 2f : moveSpeed / 2f;
//        }
        if (moveTimer < 0f) {
            moveTimer = MathUtils.random(10f, 15f);
        }
    }

}
