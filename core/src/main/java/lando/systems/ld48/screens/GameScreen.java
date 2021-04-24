package lando.systems.ld48.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld48.Game;
import lando.systems.ld48.entities.CaptureHandler;
import lando.systems.ld48.entities.EnemyEntity;
import lando.systems.ld48.entities.Player;
import lando.systems.ld48.levels.Level;
import lando.systems.ld48.levels.LevelDescriptor;
import lando.systems.ld48.levels.SpawnEnemy;
import lando.systems.ld48.physics.PhysicsComponent;
import lando.systems.ld48.physics.PhysicsSystem;

public class GameScreen extends BaseScreen {

    public Level level;
    public Player player;
    public CaptureHandler captureHandler;
    public Array<EnemyEntity> enemies;

    public PhysicsSystem physicsSystem;
    public Array<PhysicsComponent> physicsEntities;

    public boolean upPressed = false;
    public boolean rightPressed = false;
    public boolean leftPressed = false;
    public boolean downPressed = false;

    public GameScreen(Game game) {
        super(game);
        loadLevel(LevelDescriptor.test);
    }

    private void loadLevel(LevelDescriptor levelDescriptor) {
        this.level = new Level(levelDescriptor, this);
        this.player = new Player(this, level.getPlayerSpawn());
        this.captureHandler = new CaptureHandler(player);
        this.enemies = new Array<>();
        this.physicsSystem = new PhysicsSystem(this);
        this.physicsEntities = new Array<>();
        this.physicsEntities.add(player);

        // for testing
        for (SpawnEnemy spawner : this.level.getEnemySpawns()) {
            spawner.spawn(this);
        }
        // for testing
    }

    @Override
    public void update(float dt) {

        player.update(dt);
        enemies.forEach(enemy -> enemy.update(dt));
        captureHandler.updateCapture(dt, enemies);
        level.update(dt);
        physicsSystem.update(dt);
    }

    @Override
    public boolean keyDown(int keyCode) {
        switch (keyCode) {
            case Input.Keys.F1:
                DebugFlags.renderFpsDebug = !DebugFlags.renderFpsDebug;
                break;
            case Input.Keys.F2:
                DebugFlags.renderLevelDebug   = !DebugFlags.renderLevelDebug;
                break;
            case Input.Keys.F3:
                DebugFlags.renderPlayerDebug  = !DebugFlags.renderPlayerDebug;
                break;
            case Input.Keys.F4:
                DebugFlags.renderEnemyDebug   = !DebugFlags.renderEnemyDebug;
                break;
            case Input.Keys.F5:
                DebugFlags.renderPhysicsDebug = !DebugFlags.renderPhysicsDebug;
                break;
            case Input.Keys.S:
            case Input.Keys.DOWN:
                if (captureHandler != null) {
                    captureHandler.beginCapture(enemies);
                }
                downPressed = true;
                break;
            case Input.Keys.A:
            case Input.Keys.LEFT:
                leftPressed = true;
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                rightPressed = true;
                break;
            case Input.Keys.W:
            case Input.Keys.UP:
            case Input.Keys.SPACE:
                this.player.jump();
                upPressed = true;
                break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keyCode) {
        switch (keyCode) {
            case Input.Keys.S:
            case Input.Keys.DOWN:
                downPressed = false;
                break;
            case Input.Keys.A:
            case Input.Keys.LEFT:
                leftPressed = false;
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                rightPressed = false;
                break;
            case Input.Keys.W:
            case Input.Keys.UP:
            case Input.Keys.SPACE:
                upPressed = false;
                break;
        }
        return false;
    }

    @Override
    public void render(SpriteBatch batch) {
        // draw world stuff
        batch.setProjectionMatrix(worldCamera.combined);
        {
            batch.begin();
            {
                // draw distant background
            }
            batch.end();

            level.render(Level.LayerType.background, worldCamera);
            level.render(Level.LayerType.collision, worldCamera);

            batch.begin();
            {
                enemies.forEach(enemy -> enemy.render(batch));
                player.render(batch);
                level.renderObjects(batch);
            }
            batch.end();

            level.render(Level.LayerType.foreground, worldCamera);

            batch.begin();
            {
                // draw foreground entity decorations and such
            }
            batch.end();

            batch.begin();
            {
                if (DebugFlags.renderLevelDebug) {
                    level.renderDebug(batch);
                }
                if (DebugFlags.renderPlayerDebug) {
                    player.renderDebug(batch);
                }
                if (DebugFlags.renderEnemyDebug) {
                    enemies.forEach(enemy -> enemy.renderDebug(batch));
                }
                if (DebugFlags.renderPhysicsDebug) {
                    physicsSystem.renderDebug(batch);
                }
            }
            batch.end();
        }

        // draw window space stuff
        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            if (DebugFlags.renderFpsDebug) {
                game.assets.pixelFont16.draw(batch, " fps: " + Gdx.graphics.getFramesPerSecond(), 10f, windowCamera.viewportHeight - 10f);
            }
            // draw overlay ui stuff
        }
        batch.end();
    }

    // ------------------------------------------------------------------------
    // Implementation stuff
    // ------------------------------------------------------------------------

    static class DebugFlags {
        public static boolean renderFpsDebug = true;
        public static boolean renderLevelDebug = false;
        public static boolean renderPlayerDebug = false;
        public static boolean renderEnemyDebug = false;
        public static boolean renderPhysicsDebug = false;
    }

}
