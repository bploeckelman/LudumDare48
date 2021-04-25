package lando.systems.ld48.screens;

import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld48.Audio;
import lando.systems.ld48.Game;
import lando.systems.ld48.entities.CaptureHandler;
import lando.systems.ld48.entities.EnemyEntity;
import lando.systems.ld48.entities.Player;
import lando.systems.ld48.levels.*;
import lando.systems.ld48.levels.backgrounds.ParallaxBackground;
import lando.systems.ld48.levels.backgrounds.TextureRegionParallaxLayer;
import lando.systems.ld48.particles.Particles;
import lando.systems.ld48.physics.PhysicsComponent;
import lando.systems.ld48.physics.PhysicsSystem;

public class GameScreen extends BaseScreen {

    public Level level;
    public Player player;
    public LevelTransition levelTransition;
    public ParallaxBackground background;
    public CaptureHandler captureHandler;
    public Array<EnemyEntity> enemies;

    public PhysicsSystem physicsSystem;
    public Array<PhysicsComponent> physicsEntities;

    public boolean upPressed = false;
    public boolean rightPressed = false;
    public boolean leftPressed = false;
    public boolean downPressed = false;

    private Rectangle exitOverlapRectangle;

    public GameScreen(Game game) {
        super(game);
        loadLevel(LevelDescriptor.introduction);
    }

    public void loadLevel(LevelDescriptor levelDescriptor) {
        this.level = new Level(levelDescriptor, this);
        this.levelTransition = null;

        resetPlayer(level.getPlayerSpawn());

        this.captureHandler = new CaptureHandler(player, this);
        this.enemies = new Array<>();
        this.physicsSystem = new PhysicsSystem(this);
        this.physicsEntities = new Array<>();
        this.physicsEntities.add(player);
        this.exitOverlapRectangle = new Rectangle();

        TiledMapTileLayer collisionLayer = level.getLayer(Level.LayerType.collision).tileLayer;
        float levelWidth = collisionLayer.getWidth() * collisionLayer.getTileWidth();
        float levelHeight = collisionLayer.getHeight() * collisionLayer.getTileHeight();
        Vector2 scrollRatio = new Vector2(0.75f, 1.0f);
        TextureRegion backTexture;
        switch (levelDescriptor) {
            default:
            case test:  backTexture = game.assets.sunsetBackground; break;
            case test2: backTexture = game.assets.desertBackground; break;
            case test3: backTexture = game.assets.desertBackground; break;
        }
        this.background = new ParallaxBackground(new TextureRegionParallaxLayer(backTexture, levelWidth, levelHeight, scrollRatio));

        // immediately spawn enemies rather than spawning when they're about to come onscreen
        // todo - this is for testing, make spawning smarter
        for (SpawnEnemy spawner : this.level.getEnemySpawns()) {
            spawner.spawn(this);
        }

        // make sure the camera is setup correctly for when we get here from a level transition
        CameraConstraints.update(worldCamera, player, level);

        // immediately move to starting coords instead of lerping
        worldCamera.position.x = CameraConstraints.targetPos.x;
        worldCamera.position.y = CameraConstraints.targetPos.y;
        worldCamera.update();

        game.audio.playMusic(Audio.Musics.level1boss);
    }

    private void resetPlayer(SpawnPlayer spawn) {
        if (this.player == null) {
            this.player = new Player(this, spawn);
        }
        this.player.setPosition(spawn.pos.x, spawn.pos.y);
    }

    @Override
    public void update(float dt) {
        if (levelTransition != null) {
            levelTransition.update(dt);
        } else {
            player.update(dt);
            enemies.forEach(enemy -> enemy.update(dt));
            captureHandler.updateCapture(dt, enemies);
            level.update(dt);
            physicsSystem.update(dt);
            particles.update(dt);

            CameraConstraints.update(worldCamera, player, level);

            // todo - this is abrupt, probably want to trigger an interaction animation like moving the player and making them face front, then spawning a particle system or something
            // todo - this is also a little dumb, probably want a more robust way to check for 'mostly overlapped' (vs 'any overlap' vs 'fully contained')
            if (player.capturedEnemy != null) {
                Intersector.intersectRectangles(player.collisionBounds, level.getExit().bounds, exitOverlapRectangle);
                if (exitOverlapRectangle.area() > 500f) {
                    startLevelTransition(level.getExit());
                }
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // draw world stuff
        batch.setProjectionMatrix(worldCamera.combined);
        {
            if (levelTransition != null) {
                batch.begin();
                {
                    levelTransition.render(batch, worldCamera);
                }
                batch.end();
            } else {
                batch.begin();
                {
                    background.render(batch, worldCamera);
                    particles.draw(batch, Particles.Layer.background);
                }
                batch.end();

                level.render(Level.LayerType.background, worldCamera);
                level.render(Level.LayerType.collision, worldCamera);

                batch.begin();
                {
                    enemies.forEach(enemy -> enemy.render(batch));
                    particles.draw(batch, Particles.Layer.middle);
                    player.render(batch);
                    level.renderObjects(batch);
                    particles.draw(batch, Particles.Layer.foreground);
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
    // InputProcessor overrides
    // ------------------------------------------------------------------------

    @Override
    public boolean keyDown(int keyCode) {
        switch (keyCode) {
            // ----------------------
            case Input.Keys.F1: DebugFlags.renderFpsDebug     = !DebugFlags.renderFpsDebug;     break;
            case Input.Keys.F2: DebugFlags.renderLevelDebug   = !DebugFlags.renderLevelDebug;   break;
            case Input.Keys.F3: DebugFlags.renderPlayerDebug  = !DebugFlags.renderPlayerDebug;  break;
            case Input.Keys.F4: DebugFlags.renderEnemyDebug   = !DebugFlags.renderEnemyDebug;   break;
            case Input.Keys.F5: DebugFlags.renderPhysicsDebug = !DebugFlags.renderPhysicsDebug; break;
            // ----------------------
            case Input.Keys.S:
            case Input.Keys.DOWN:
                if (captureHandler != null) {
                    captureHandler.beginCapture(enemies);
                }
                downPressed = true;
                break;
            // ----------------------
            case Input.Keys.A:
            case Input.Keys.LEFT:
                leftPressed = true;
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                rightPressed = true;
                break;
            // ----------------------
            case Input.Keys.W:
            case Input.Keys.UP:
            case Input.Keys.SPACE:
                this.player.jump();
                upPressed = true;
                break;
            // ----------------------
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                this.player.attack();
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

    // ------------------------------------------------------------------------
    // Implementation stuff
    // ------------------------------------------------------------------------

    static class DebugFlags {
        public static boolean renderFpsDebug = false;
        public static boolean renderLevelDebug = false;
        public static boolean renderPlayerDebug = false;
        public static boolean renderEnemyDebug = false;
        public static boolean renderPhysicsDebug = false;
    }

    public static class CameraConstraints {
        public static boolean override = false;

        public static float marginHoriz = 40f;
        public static float marginVert = 20;
        public static float marginVertJump = 150f;

        public static float zoomMin = 0.1f;
        public static float zoomMax = 2.0f;

        public static float lerpScalePan = 0.2f;
        public static float lerpScaleZoom = 0.02f;

        public static Vector2 targetPos = new Vector2();
        public static MutableFloat targetZoom = new MutableFloat(1f);

        public static void update(OrthographicCamera camera, Player player, Level level) {
            float playerX = player.position.x + player.collisionBounds.width / 2f;
            if (playerX < targetPos.x - marginHoriz) targetPos.x = playerX + marginHoriz;
            if (playerX > targetPos.x + marginHoriz) targetPos.x = playerX - marginHoriz;

            float playerY = player.position.y + player.collisionBounds.height / 2f;
            if (playerY < targetPos.y - marginVert) {
                targetPos.y = playerY + marginVert;
            }

            if (player.grounded) {
                if (playerY > targetPos.y + marginVert) {
                    targetPos.y = playerY - marginVert;
                }
            } else {
                if (playerY > targetPos.y + marginVertJump) {
                    targetPos.y = playerY - marginVertJump;
                }
            }

            TiledMapTileLayer collisionTileLayer = level.getLayer(Level.LayerType.collision).tileLayer;
            float collisionLayerWidth      = collisionTileLayer.getWidth();
            float collisionLayerHeight     = collisionTileLayer.getHeight();
            float collisionLayerTileWidth  = collisionTileLayer.getTileWidth();
            float collisionLayerTileHeight = collisionTileLayer.getTileHeight();

            float cameraLeftEdge = camera.viewportWidth / 2f;
            targetPos.x = MathUtils.clamp(targetPos.x, cameraLeftEdge, collisionLayerWidth * collisionLayerTileWidth - cameraLeftEdge);

            float cameraVertEdge = camera.viewportHeight / 2f;
            targetPos.y = MathUtils.clamp(targetPos.y, cameraVertEdge, collisionLayerHeight * collisionLayerTileHeight - cameraVertEdge);

    //        targetZoom.setValue(1 + Math.abs(player.velocity.y / 2000f));

            // update actual camera position/zoom unless overridden for special effects
            if (!override) {
                camera.zoom = MathUtils.lerp(camera.zoom, targetZoom.floatValue(), lerpScaleZoom);
                camera.zoom = MathUtils.clamp(camera.zoom, zoomMin, zoomMax);

                camera.position.x = MathUtils.lerp(camera.position.x, targetPos.x, lerpScalePan);
                camera.position.y = MathUtils.lerp(camera.position.y, targetPos.y, lerpScalePan);
                camera.update();
            }
        }
    }

    private void startLevelTransition(Exit exit) {
        if (levelTransition != null) return;

        // todo - trigger a tween that locks input, fades to black, starts transition
        levelTransition = new LevelTransition(exit, this);
    }

}
