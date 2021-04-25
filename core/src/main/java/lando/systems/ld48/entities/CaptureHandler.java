package lando.systems.ld48.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class CaptureHandler {

    private final float CAPTURE_TIME = 1.0f;
    private final float RELEASE_TIME = 0.5f;

    private Player player;

    private Array<EnemyEntity> nearbyCapturing = new Array<>();
    private float captureTimer = 0f;

    public CaptureHandler(Player p) {
        player = p;
    }

    public void beginCapture(Array<EnemyEntity> enemies) {
        if (player.capturing) { return; }
        if (player.capturedEnemy == null) {
            nearbyCapturing = new Array<>();
            for (EnemyEntity enemy : enemies) {
                if (Math.abs((enemy.collisionBounds.x + enemy.collisionBounds.width/2) - (player.collisionBounds.x + player.collisionBounds.width/2)) <= 20
                    && Math.abs((enemy.collisionBounds.y) - (player.collisionBounds.y)) <= 10) {
                    nearbyCapturing.add(enemy);
                    enemy.targeted = true;
                }
            }
        }
        player.capturing = (player.capturedEnemy != null || nearbyCapturing.size != 0);
        captureTimer = 0f;
    }

    public void updateCapture(float dt, Array<EnemyEntity> enemies) {
        if (!player.capturing) { return; }

        captureTimer += dt;

        float maxTime = CAPTURE_TIME;

        player.captureProgress = 0;

        if (player.capturedEnemy == null) {
            Array.ArrayIterator<EnemyEntity> e = nearbyCapturing.iterator();
            while (e.hasNext()) {
                EnemyEntity enemy = e.next();
                if (Math.abs((enemy.collisionBounds.x + enemy.collisionBounds.width/2) - (player.collisionBounds.x + player.collisionBounds.width/2)) > 20
                        || Math.abs((enemy.collisionBounds.y) - (player.collisionBounds.y)) > 10
                        || enemy.dead
                        || !enemies.contains(enemy, true)) {
                    enemy.targeted = false;
                    e.remove();
                }
            }
            if (nearbyCapturing.size == 0) {
                captureTimer = 0;
                player.capturing = false;
                return;
            }
            if (captureTimer >= CAPTURE_TIME) {
                captureEnemy(nearbyCapturing.get(0));
                captureTimer = 0;
                player.capturing = false;
            }
        } else {
            maxTime = RELEASE_TIME;
            if (!player.screen.downPressed) {
                captureTimer = 0;
                player.capturing = false;
                return;
            }
            if (captureTimer >= RELEASE_TIME) {
                uncaptureEnemy();
                captureTimer = 0;
                player.capturing = false;
            }
        }

        player.captureProgress = MathUtils.clamp(captureTimer / maxTime, 0, 1);
    }

    private void captureEnemy(EnemyEntity e) {
        player.SetEnemy(e);
    }

    private void uncaptureEnemy() {
        player.SetEnemy(null);
    }

}
