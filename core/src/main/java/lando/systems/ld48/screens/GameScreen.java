package lando.systems.ld48.screens;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import lando.systems.ld48.Audio;
import lando.systems.ld48.Game;
import lando.systems.ld48.entities.*;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.levels.*;
import lando.systems.ld48.levels.backgrounds.ParallaxBackground;
import lando.systems.ld48.levels.backgrounds.TextureRegionParallaxLayer;
import lando.systems.ld48.particles.Particles;
import lando.systems.ld48.physics.PhysicsComponent;
import lando.systems.ld48.physics.PhysicsSystem;
import lando.systems.ld48.ui.Modal;
import lando.systems.ld48.utils.Calc;

public class GameScreen extends BaseScreen {

    public Level level;
    public Player player;
    public Boss boss;
    public LevelTransition levelTransition;
    public ParallaxBackground background;
    public CaptureHandler captureHandler;
    public Array<EnemyEntity> enemies;
    public Array<PickupEntity> pickups;
    public Array<InteractableEntity> interactables;
    public Array<Bullet> bullets;

    public PhysicsSystem physicsSystem;
    public Array<PhysicsComponent> physicsEntities;

    public boolean upPressed = false;
    public boolean rightPressed = false;
    public boolean leftPressed = false;
    public boolean downPressed = false;
    public boolean shiftPressed = false;

    private Rectangle overlapRectangle;

    public Modal generalModal;

    public GameScreen(Game game) {
        super(game);
        this.player = new Player(this, 0, 0);
        // ROSSMAN: swap comments for boss testing
//        this.levelTransition = new LevelTransition(new Exit(LevelTransition.Type.alien, LevelDescriptor.musk_arena, "introText"), this);
        this.levelTransition = new LevelTransition(new Exit(LevelTransition.Type.purgatory, LevelDescriptor.introduction, "exposition"), this);

        Timeline.createSequence()
                .pushPause(15f)
                .push(Tween.call((type, source) -> showDoorTutorial()))
                .start(game.tween);
    }

    public void loadLevel(LevelDescriptor levelDescriptor) {
        this.boss = null;
        this.level = new Level(levelDescriptor, this);
        this.levelTransition = null;

        resetPlayer(level.getPlayerSpawn());

        this.captureHandler = new CaptureHandler(player, this);
        this.enemies = new Array<>();
        this.pickups = new Array<>();
        this.interactables = new Array<>();
        this.bullets = new Array<>();
        this.physicsSystem = new PhysicsSystem(this);
        this.physicsEntities = new Array<>();
        this.physicsEntities.add(player);
        this.overlapRectangle = new Rectangle();

        TiledMapTileLayer collisionLayer = level.getLayer(Level.LayerType.collision).tileLayer;
        float levelWidth = collisionLayer.getWidth() * collisionLayer.getTileWidth();
        float levelHeight = collisionLayer.getHeight() * collisionLayer.getTileHeight();
        Vector2 scrollRatio = new Vector2(0.75f, 1.0f);
        TextureRegion backTexture;
        switch (levelDescriptor) {
            case introduction: backTexture = game.assets.sunsetBackground; break;
            case military:     backTexture = game.assets.coreBackground; break;
            case zuck_arena:   backTexture = game.assets.coreBackground; break;
            case alien:        backTexture = game.assets.desertBackground; break;
            case reptilian:    backTexture = game.assets.desertBackground; break;
            case musk_arena:   backTexture = game.assets.coreBackground; break;
            default:           backTexture = game.assets.desertBackground; break;
        }
        TextureRegionParallaxLayer parallax = new TextureRegionParallaxLayer(backTexture, levelWidth, levelHeight, scrollRatio);
        this.background = new ParallaxBackground(parallax);

        // immediately spawn stuff, probably not enough time to get clever spawning setup
        for (SpawnBoss spawner : this.level.getBossSpawns()) {
            spawner.spawn(this);
        }
        for (SpawnEnemy spawner : this.level.getEnemySpawns()) {
            spawner.spawn(this);
        }
        for (SpawnPickup spawner : this.level.getPickupSpawns()) {
            spawner.spawn(this);
        }
        for (SpawnInteractable spawner : this.level.getInteractableSpawns()) {
            spawner.spawn(this);
        }

        // make sure the camera is setup correctly for when we get here from a level transition
        CameraConstraints.update(worldCamera, player, level);

        // immediately move to starting coords instead of lerping
        worldCamera.position.x = CameraConstraints.targetPos.x;
        worldCamera.position.y = CameraConstraints.targetPos.y;
        worldCamera.update();

        // NOTE: happens in LevelTransition now
//        game.audio.playMusic(Audio.Musics.level3elevator);
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
            if (player.isOffScreen) {
                player.updateOffScreen(dt);
                return;
            }

            if (handleModal(dt)) { return; }

            // stash player's current position pre-update in case we need to walk it back
            float playerPrevPosX = Calc.floor(player.position.x);
            float playerPrevPosY = Calc.floor(player.position.y);

            // loop in reverse so we don't get off when entity is removed
            for (int i = physicsEntities.size - 1; i >= 0; i--) {
                physicsEntities.get(i).update(dt);
            }

            captureHandler.updateCapture(dt, enemies);
            level.update(dt);
            physicsSystem.update(dt);
            particles.update(dt);
            if (boss != null) {
                boss.update(dt);
            }
            checkBulletCollisions();

            CameraConstraints.update(worldCamera, player, level);

            // pickup pickup-able entities
            if (player.capturedEnemy != null) {
                for (int i = pickups.size - 1; i >= 0; i--) {
                    PickupEntity pickup = pickups.get(i);
                    if (player.collisionBounds.overlaps(pickup.collisionBounds)) {
                        // TODO: make a counter for the hud or something
                        particles.pickup(pickup.position.x, pickup.position.y, pickup.type);
                        game.audio.playSound(Audio.Sounds.coin);
                        pickup.removeFromScreen();
                    }
                }
            }

            // interact with interactable entities
            for (InteractableEntity interactable : interactables) {
                if (player.collisionBounds.overlaps(interactable.collisionBounds)) {
                    // can't interact if we're a ghost
                    if (player.capturedEnemy != null) {
                        interactable.interact();
                    }

                    // special case to keep players from passing through doors
                    // when a door completes its 'interaction' (ie opens) it gets removed, so players can pass then
                    // NOTE - this is super sketch, literally everything in the physics system would be made simpler
                    //  if we operated only on integer boundaries and carried over remainders through frames
                    if (interactable.type == SpawnInteractable.Type.door) {
                        Intersector.intersectRectangles(player.collisionBounds, interactable.collisionBounds, overlapRectangle);
                        boolean onYourLeft = (player.collisionBounds.x < interactable.collisionBounds.x);
                        float sign = onYourLeft ? -1 : 1;
                        float playerSeparationPosX = player.position.x + sign * overlapRectangle.width;
                        player.setPosition(playerSeparationPosX, playerPrevPosY);
                        player.collisionBounds.setPosition(
                                playerSeparationPosX - Calc.floor(player.collisionBounds.width / 2f),
                                playerPrevPosY - Calc.floor(player.collisionBounds.height / 2f));
                        player.stop();
                        showDoorTutorial();
                    }
                }
            }

            // check for level exit
            // todo - this is abrupt, probably want to trigger an interaction animation like moving the player and making them face front, then spawning a particle system or something
            // todo - this is also a little dumb, probably want a more robust way to check for 'mostly overlapped' (vs 'any overlap' vs 'fully contained')
            if (player.capturedEnemy != null) {
                if (Intersector.intersectRectangles(player.collisionBounds, level.getExit().bounds, overlapRectangle)) {
                    // lol, player got smaller so overlaps weren't counted anymore
                    float minOverlapArea = 200f;
                    if (overlapRectangle.area() >= minOverlapArea) {
                        startLevelTransition(level.getExit());
                    }
                }
            }
        }
    }

    public boolean doorTutorialShown = false;
    private void showDoorTutorial() {
        if (doorTutorialShown) { return; }

        doorTutorialShown = true;

        Timeline.createSequence()
                .pushPause(1f)
                .push(Tween.call((type, source) -> generalModal = new Modal(game.assets, game.assets.strings.get("tutorial"), getWindowCamera())))
                .start(game.tween);
    }

    private boolean handleModal(float dt) {
        if (generalModal != null) {
            generalModal.update(dt);
            if (generalModal.isComplete()) {
                generalModal = null;
            }
            return true;
        }
        return false;
    }

    private void checkBulletCollisions() {
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (b.owner == player) {
                for (EnemyEntity enemy : enemies) {
                    if (enemy.collisionBounds.contains(b.getPosition())) {
                        enemy.adjustHitpoints(-b.damage);
                        bullets.removeValue(b, true);
                        physicsEntities.removeValue(b, true);
                    }
                }
            } else {
                // enemy bullet
                if (player.capturedEnemy != null && player.collisionBounds.contains(b.position)){
                    player.adjustHitpoints(-b.damage);
                    bullets.removeValue(b, true);
                    physicsEntities.removeValue(b, true);
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
                    // draw all but player - that goes on top
                    physicsEntities.forEach(entity -> {
                        if (entity != player) {
                            entity.render(batch);
                        }
                    });
                    particles.draw(batch, Particles.Layer.middle);
                    if (boss != null) {
                        boss.render(batch);
                    }
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
                        if (boss != null) {
                            boss.renderDebug(batch);
                        }
                    }
                    if (DebugFlags.renderPickupDebug) {
                        pickups.forEach(pickup -> pickup.renderDebug(batch));
                    }
                    if (DebugFlags.renderInteractDebug) {
                        interactables.forEach(interactable -> interactable.renderDebug(batch));
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
            if (player.isOffScreen){
                player.renderOffScreenMessage(batch);
            }

            if (generalModal != null) {
                generalModal.render(batch);
            }
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
            case Input.Keys.F1: DebugFlags.renderFpsDebug      = !DebugFlags.renderFpsDebug;      break;
            case Input.Keys.F2: DebugFlags.renderLevelDebug    = !DebugFlags.renderLevelDebug;    break;
            case Input.Keys.F3: DebugFlags.renderPlayerDebug   = !DebugFlags.renderPlayerDebug;   break;
            case Input.Keys.F4: DebugFlags.renderEnemyDebug    = !DebugFlags.renderEnemyDebug;    break;
            case Input.Keys.F5: DebugFlags.renderPickupDebug   = !DebugFlags.renderPickupDebug;   break;
            case Input.Keys.F6: DebugFlags.renderInteractDebug = !DebugFlags.renderInteractDebug; break;
            case Input.Keys.F7: DebugFlags.renderPhysicsDebug  = !DebugFlags.renderPhysicsDebug;  break;
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
                shiftPressed = true;
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
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                shiftPressed = false;
                break;
        }
        return false;
    }

//    @Override
//    public boolean buttonDown(Controller controller, int buttonCode) {
//        if (buttonCode == Xbox.A || buttonCode == Xbox.X) {
//            shiftPressed = true;
//        }
//        if (buttonCode == Xbox.B) {
//            if (captureHandler != null) {
//                captureHandler.beginCapture(enemies);
//            }
//            downPressed = true;
//        }
//        if (buttonCode == Xbox.Y) {
//            this.player.jump();
//            upPressed = true;
//        }
//        return false;
//    }
//
//
//    @Override
//    public boolean buttonUp(Controller controller, int buttonCode) {
//        if (buttonCode == Xbox.A || buttonCode == Xbox.X) {
//            shiftPressed = false;
//        }
//        if (buttonCode == Xbox.B) {
//            downPressed = false;
//        }
//        if (buttonCode == Xbox.Y) {
//            upPressed = false;
//        }
//        return false;
//    }
//
//    @Override
//    public boolean axisMoved(Controller controller, int axisCode, float value) {
//        if (axisCode == Xbox.L_STICK_HORIZONTAL_AXIS) {
//            float deadZone = 0.4f;
//            leftPressed  = (value < -deadZone);
//            rightPressed = (value > deadZone);
//        }
//        return false;
//    }

    // ------------------------------------------------------------------------
    // Implementation stuff
    // ------------------------------------------------------------------------

    static class DebugFlags {
        public static boolean renderFpsDebug = false;
        public static boolean renderLevelDebug = false;
        public static boolean renderPlayerDebug = false;
        public static boolean renderEnemyDebug = false;
        public static boolean renderPickupDebug = false;
        public static boolean renderInteractDebug = false;
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

            if (player.isGrounded()) {
                if (playerY > targetPos.y + marginVert) {
                    targetPos.y = playerY - marginVert;
                }
            } else if (player.capturedEnemy == null) {
                // follow closely while ghostly
                targetPos.y = playerY;
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
//
//    /** Mappings for the lando.systems.ld48.screens.GameScreen.Xbox series of controllers.
//     *
//     * See <a href="https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/360_controller.svg/450px-360_controller.svg.png">this
//     * image</a> which describes each button and axes.
//     *
//     * All codes are for buttons expect the L_STICK_XXX, R_STICK_XXX, L_TRIGGER and R_TRIGGER codes, which are axes.
//     *
//     * @author badlogic */
//    public static class Xbox {
//        // Buttons
//        public static final int A;
//        public static final int B;
//        public static final int X;
//        public static final int Y;
//        public static final int GUIDE;
//        public static final int L_BUMPER;
//        public static final int R_BUMPER;
//        public static final int BACK;
//        public static final int START;
//        public static final int DPAD_UP;
//        public static final int DPAD_DOWN;
//        public static final int DPAD_LEFT;
//        public static final int DPAD_RIGHT;
//        public static final int L_STICK;
//        public static final int R_STICK;
//
//        // Axes
//        /** left trigger, -1 if not pressed, 1 if pressed **/
//        public static final int L_TRIGGER;
//        /** right trigger, -1 if not pressed, 1 if pressed **/
//        public static final int R_TRIGGER;
//        /** left stick vertical axis, -1 if up, 1 if down **/
//        public static final int L_STICK_VERTICAL_AXIS;
//        /** left stick horizontal axis, -1 if left, 1 if right **/
//        public static final int L_STICK_HORIZONTAL_AXIS;
//        /** right stick vertical axis, -1 if up, 1 if down **/
//        public static final int R_STICK_VERTICAL_AXIS;
//        /** right stick horizontal axis, -1 if left, 1 if right **/
//        public static final int R_STICK_HORIZONTAL_AXIS;
//
//        static {
//            if (SharedLibraryLoader.isWindows) {
//                if(Gdx.graphics.getType() == Graphics.GraphicsType.LWJGL3) {
//                    A = 0;
//                    B = 1;
//                    X = 2;
//                    Y = 3;
//                    GUIDE = -1;
//                    L_BUMPER = 4;
//                    R_BUMPER = 5;
//                    BACK = 6;
//                    START = 7;
//                    DPAD_UP = -1;
//                    DPAD_DOWN = -1;
//                    DPAD_LEFT = -1;
//                    DPAD_RIGHT = -1;
//                    L_TRIGGER = 4;
//                    R_TRIGGER = 5;
//                    L_STICK_VERTICAL_AXIS = 1;
//                    L_STICK_HORIZONTAL_AXIS = 0;
//                    L_STICK = 8;
//                    R_STICK_VERTICAL_AXIS = 3;
//                    R_STICK_HORIZONTAL_AXIS = 2;
//                    R_STICK = 9;
//                } else {
//                    A = 0;
//                    B = 1;
//                    X = 2;
//                    Y = 3;
//                    GUIDE = -1;
//                    L_BUMPER = 4;
//                    R_BUMPER = 5;
//                    BACK = 6;
//                    START = 7;
//                    DPAD_UP = -1;
//                    DPAD_DOWN = -1;
//                    DPAD_LEFT = -1;
//                    DPAD_RIGHT = -1;
//                    L_TRIGGER = 4; // 0..1
//                    R_TRIGGER = 4; // 0..-1
//                    L_STICK_VERTICAL_AXIS = 0;
//                    L_STICK_HORIZONTAL_AXIS = 1;
//                    L_STICK = 8;
//                    R_STICK_VERTICAL_AXIS = 2;
//                    R_STICK_HORIZONTAL_AXIS = 3;
//                    R_STICK = 9;
//                }
//            } else if (SharedLibraryLoader.isLinux) {
//                A = 0;
//                B = 1;
//                X = 2;
//                Y = 3;
//                GUIDE = 8;
//                L_BUMPER = 4;
//                R_BUMPER = 5;
//                BACK = 6;
//                START = 7;
//                DPAD_UP = -1;
//                DPAD_DOWN = -1;
//                DPAD_LEFT = -1;
//                DPAD_RIGHT = -1;
//                L_TRIGGER = 2;
//                R_TRIGGER = 5;
//                L_STICK_VERTICAL_AXIS = 1;
//                L_STICK_HORIZONTAL_AXIS = 0;
//                L_STICK = 9;
//                R_STICK_VERTICAL_AXIS = 4;
//                R_STICK_HORIZONTAL_AXIS = 3;
//                R_STICK = 10;
//            } else if (SharedLibraryLoader.isMac) {
//                A = 11;
//                B = 12;
//                X = 13;
//                Y = 14;
//                GUIDE = 10;
//                L_BUMPER = 8;
//                R_BUMPER = 9;
//                BACK = 5;
//                START = 4;
//                DPAD_UP = 0;
//                DPAD_DOWN = 1;
//                DPAD_LEFT = 2;
//                DPAD_RIGHT = 3;
//                L_TRIGGER = 0;
//                R_TRIGGER = 1;
//                L_STICK_VERTICAL_AXIS = 3;
//                L_STICK_HORIZONTAL_AXIS = 2;
//                L_STICK = -1;
//                R_STICK_VERTICAL_AXIS = 5;
//                R_STICK_HORIZONTAL_AXIS = 4;
//                R_STICK = -1;
//            } else if (SharedLibraryLoader.isAndroid) {
//                A = 96;
//                B = 97;
//                X = 99;
//                Y = 100;
//                GUIDE = 110;
//                L_BUMPER = 102;
//                R_BUMPER = 103;
//                L_TRIGGER = 2;
//                R_TRIGGER = 5;
//                BACK = 109;
//                START = 108;
//                DPAD_UP = -1;
//                DPAD_DOWN = -1;
//                DPAD_LEFT = -1;
//                DPAD_RIGHT = -1;
//                L_STICK_VERTICAL_AXIS = 1;
//                L_STICK_HORIZONTAL_AXIS = 0;
//                L_STICK = 106;
//                R_STICK_VERTICAL_AXIS = 4;
//                R_STICK_HORIZONTAL_AXIS = 3;
//                R_STICK = 107;
//            } else {
//                A = -1;
//                B = -1;
//                X = -1;
//                Y = -1;
//                GUIDE = -1;
//                L_BUMPER = -1;
//                R_BUMPER = -1;
//                L_TRIGGER = -1;
//                R_TRIGGER = -1;
//                BACK = -1;
//                START = -1;
//                DPAD_UP = -1;
//                DPAD_DOWN = -1;
//                DPAD_LEFT = -1;
//                DPAD_RIGHT = -1;
//                L_STICK_VERTICAL_AXIS = -1;
//                L_STICK_HORIZONTAL_AXIS = -1;
//                L_STICK = -1;
//                R_STICK_VERTICAL_AXIS = -1;
//                R_STICK_HORIZONTAL_AXIS = -1;
//                R_STICK = -1;
//            }
//        }
//    }

}
