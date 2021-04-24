package lando.systems.ld48.entities;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;

public class CaptureHandler {

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
                    && Math.abs((enemy.collisionBounds.y) - (player.collisionBounds.y)) <= 2) {
                    nearbyCapturing.add(enemy);
                }
            }
        }
        player.capturing = (player.capturedEnemy != null || nearbyCapturing.size != 0);
        captureTimer = 0f;
    }

    public void updateCapture(float dt, Array<EnemyEntity> enemies) {
        if (!player.capturing) { return; }
        captureTimer += dt;
        if (player.capturedEnemy == null) {
            Array.ArrayIterator<EnemyEntity> e = nearbyCapturing.iterator();
            while (e.hasNext()) {
                EnemyEntity enemy = e.next();
                if (Math.abs((enemy.collisionBounds.x + enemy.collisionBounds.width/2) - (player.collisionBounds.x + player.collisionBounds.width/2)) > 20
                        || Math.abs((enemy.collisionBounds.y) - (player.collisionBounds.y)) > 2
                        || enemy.dead
                        || !enemies.contains(enemy, true)) {
                    e.remove();
                }
            }
            if (nearbyCapturing.size == 0) {
                captureTimer = 0;
                player.capturing = false;
                return;
            }
            if (captureTimer >= 1.5f) {
                captureEnemy(nearbyCapturing.get(0));
                captureTimer = 0;
                player.capturing = false;
            }
        } else {
            if (!player.screen.downPressed) {
                captureTimer = 0;
                player.capturing = false;
                return;
            }
            if (captureTimer >= 1f) {
                uncaptureEnemy();
                captureTimer = 0;
                player.capturing = false;
            }
        }
    }

    private void captureEnemy(EnemyEntity e) {
        player.SetEnemy(e);
    }

    private void uncaptureEnemy() {
        player.SetEnemy(null);
    }

}
