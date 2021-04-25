package lando.systems.ld48.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld48.Audio;
import lando.systems.ld48.screens.GameScreen;

public class CaptureHandler {

    private final float CAPTURE_TIME = 1.0f;
    private final float RELEASE_TIME = 0.5f;

    private Player player;
    private GameScreen screen;

    private Array<EnemyEntity> nearbyCapturing = new Array<>();
    private float captureTimer = 0f;

    public CaptureHandler(Player p, GameScreen screen) {
        player = p;
        this.screen = screen;
    }

    public void beginCapture(Array<EnemyEntity> enemies) {
        if (player.capturing) { return; }
        if (player.capturedEnemy == null) {
            screen.game.audio.playSound(Audio.Sounds.capture);
            nearbyCapturing.clear();
            for (EnemyEntity enemy : enemies) {
                if (Math.abs((enemy.collisionBounds.x + enemy.collisionBounds.width/2) - (player.collisionBounds.x + player.collisionBounds.width/2)) <= 20
                    && Math.abs((enemy.collisionBounds.y) - (player.collisionBounds.y)) <= 10) {
                    nearbyCapturing.add(enemy);
                    enemy.targeted = true;
                    // this makes the array unnecessary, but if we want multiple possible targets, remove break and
                    // untarget all enemies when capturing
                    break;
                }
            }
        }
        captureTimer = 0f;
        player.capturing = (player.capturedEnemy != null || nearbyCapturing.size != 0);
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

    private void captureEnemy(EnemyEntity enemy) {
        enemy.removeFromScreen();
        player.possess(enemy);
        screen.particles.physics(enemy.position.x, enemy.position.y);
    }

    private void uncaptureEnemy() {
        screen.game.audio.playSound(Audio.Sounds.uncapture);
        player.possess(null);
        screen.particles.smoke(player.position.x, player.position.y);
    }

}
