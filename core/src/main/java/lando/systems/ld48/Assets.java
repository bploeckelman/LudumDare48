package lando.systems.ld48;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.I18NBundle;

public class Assets implements Disposable {

    public enum Load { ASYNC, SYNC }

    public boolean initialized;

    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public GlyphLayout layout;
    public AssetManager mgr;
    public TextureAtlas atlas;
    public Transitions transitions;
    public Particles particles;
    public I18NBundle strings;

    public BitmapFont pixelFont16;

    public Texture pixel;
    public Texture title;
    public Texture levelTransitionMilitary;
    public Texture levelTransitionOrganic;
    public Texture levelTransitionAlien;

    public TextureRegion whitePixel;
    public TextureRegion sunsetBackground;
    public TextureRegion desertBackground;

    public TextureRegion bullet;

    public Animation<TextureRegion> cat;
    public Animation<TextureRegion> dog;

    public Animation<TextureRegion> elevatorPlatformAnimation;

    public Animation<TextureRegion> playerAnimation;
    public Animation<TextureRegion> playerMoveAnimation;
    //public Animation<TextureRegion> playerAttackAnimation;
    //public Animation<TextureRegion> playerJumpAnimation;
    //public Animation<TextureRegion> playerFallAnimation;
    //public Animation<TextureRegion> playerDieAnimation;

    // enemas
    public Animation<TextureRegion> grayAnimation;
    public Animation<TextureRegion> grayMoveAnimation;
    public Animation<TextureRegion> grayAttackAnimation;
    public Animation<TextureRegion> grayJumpAnimation;
    public Animation<TextureRegion> grayFallAnimation;
    public Animation<TextureRegion> grayDieAnimation;

    public Animation<TextureRegion> soldierAnimation;
    public Animation<TextureRegion> soldierMoveAnimation;
    public Animation<TextureRegion> soldierAttackAnimation;
    public Animation<TextureRegion> soldierJumpAnimation;
    public Animation<TextureRegion> soldierFallAnimation;
    public Animation<TextureRegion> soldierDieAnimation;

    // zuck tank - intellij doesn't like zuck..
    public Animation<TextureRegion> zuckTankMissileAnimation;
    public Animation<TextureRegion> zuckTankLowerAnimation;
    public Animation<TextureRegion> zuckTankTalkAnimation;
    public Animation<TextureRegion> zuckTankIdleAAnimation;
    public Animation<TextureRegion> zuckTankIdleBAnimation;
    public Animation<TextureRegion> zuckTankShootAnimation;
    public Animation<TextureRegion> zuckTankRamTellAnimation;
    public Animation<TextureRegion> zuckTankRamActAnimation;

    // pickups
    public Animation<TextureRegion> dogeCoinAnimation;
    public Animation<TextureRegion> bitCoinAnimation;

    // interactables
    public Animation<TextureRegion> leverAnimation;
    public Animation<TextureRegion> doorAnimation;

    // misc
    public Animation<TextureRegion> downArrowsAnimation;

    public NinePatch debugNinePatch;
    public NinePatch roundedBoxNinePatch;

    public Sound attackSound;
    public Sound bulletHitSound;
    public Sound captureSound;
    public Sound coinSound;
    public Sound deathSound;
    public Sound doorSound;
    public Sound exampleSound;
    public Sound jumpSound;
    public Sound leverSound;
    public Sound missileSound;
    public Sound pew1Sound;
    public Sound pew2Sound;
    public Sound pew3Sound;
    public Sound pew4Sound;
    public Sound uncaptureSound;
    public Sound zuckRamSound;
    public Sound zuckTank1Sound;
    public Sound zuckTank2Sound;
    public Sound zuckTank3Sound;

    public Music exampleMusic;
    public Music introMusic;
    public Music level1Music;
    public Music level1ElevatorMusic;
    public Music level1BossMusic;
    public Music level2Music;
    public Music level2ElevatorMusic;
    public Music level3Music;
    public Music level3ElevatorMusic;

    public Assets() {
        this(Load.SYNC);
    }

    public Assets(Load load) {
        initialized = false;

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        layout = new GlyphLayout();

        mgr = new AssetManager();
        {
            mgr.load(new AssetDescriptor<>("images/pixel.png", Texture.class));
            mgr.load(new AssetDescriptor<>("images/title.png", Texture.class));
            mgr.load(new AssetDescriptor<>("images/elevator-military.png", Texture.class));
            mgr.load(new AssetDescriptor<>("images/elevator-organic.png", Texture.class));
            mgr.load(new AssetDescriptor<>("images/elevator-alien.png", Texture.class));

            mgr.load(new AssetDescriptor("fonts/chevyray-rise-16.fnt", BitmapFont.class));

            mgr.load(new AssetDescriptor<>("sprites/sprites.atlas", TextureAtlas.class));

            mgr.load("i18n/strings", I18NBundle.class);

            mgr.load("audio/sound/attack.ogg", Sound.class);
            mgr.load("audio/sound/bullethit.ogg", Sound.class);
            mgr.load("audio/sound/capture.ogg", Sound.class);
            mgr.load("audio/sound/coin.ogg", Sound.class);
            mgr.load("audio/sound/die.ogg", Sound.class);
            mgr.load("audio/sound/dooropen.ogg", Sound.class);
            mgr.load("audio/sound/example.wav", Sound.class);
            mgr.load("audio/sound/jump.ogg", Sound.class);
            mgr.load("audio/sound/lever.ogg", Sound.class);
            mgr.load("audio/sound/missile.ogg", Sound.class);
            mgr.load("audio/sound/pew.ogg", Sound.class);
            mgr.load("audio/sound/pew1.ogg", Sound.class);
            mgr.load("audio/sound/pew2.ogg", Sound.class);
            mgr.load("audio/sound/pew3.ogg", Sound.class);
            mgr.load("audio/sound/pew4.ogg", Sound.class);
            mgr.load("audio/sound/popout.ogg", Sound.class);
            mgr.load("audio/sound/uncapture.ogg", Sound.class);
            mgr.load("audio/sound/zuckram.ogg", Sound.class);
            mgr.load("audio/sound/zucktank1.ogg", Sound.class);
            mgr.load("audio/sound/zucktank2.ogg", Sound.class);
            mgr.load("audio/sound/zucktank3.ogg", Sound.class);

            mgr.load("audio/music/intro.ogg", Music.class);
            mgr.load("audio/music/level1.ogg", Music.class);
            mgr.load("audio/music/level1-boss.ogg", Music.class);
            mgr.load("audio/music/level2.ogg", Music.class);
            mgr.load("audio/music/level3.ogg", Music.class);
            mgr.load("audio/music/elevator-to-level1.ogg", Music.class);
            mgr.load("audio/music/elevator-to-level2.ogg", Music.class);
            mgr.load("audio/music/elevator-to-level3.ogg", Music.class);
        }

        if (load == Load.SYNC) {
            mgr.finishLoading();
            updateLoading();
        }
    }

    public float updateLoading() {
        if (!mgr.update()) return mgr.getProgress();
        if (initialized) return 1f;

        pixel = mgr.get("images/pixel.png");
        title = mgr.get("images/title.png");

        levelTransitionMilitary = mgr.get("images/elevator-military.png");
        levelTransitionOrganic  = mgr.get("images/elevator-organic.png");
        levelTransitionAlien    = mgr.get("images/elevator-alien.png");
        levelTransitionMilitary .setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        levelTransitionOrganic  .setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        levelTransitionAlien    .setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        pixelFont16 = mgr.get("fonts/chevyray-rise-16.fnt");

        atlas = mgr.get("sprites/sprites.atlas");

        whitePixel = new TextureRegion(pixel);
        sunsetBackground = new TextureRegion(atlas.findRegion("backgrounds/sunset"));
        desertBackground = new TextureRegion(atlas.findRegion("backgrounds/desert"));

        bullet = new TextureRegion(atlas.findRegion("pets/cat"));

        cat = new Animation<>(0.1f, atlas.findRegions("pets/cat"), Animation.PlayMode.LOOP);
        dog = new Animation<>(0.1f, atlas.findRegions("pets/dog"), Animation.PlayMode.LOOP);

        elevatorPlatformAnimation = new Animation<>(0.1f, atlas.findRegions("world/elevator-platform"), Animation.PlayMode.LOOP);

        playerAnimation       = new Animation<>(0.1f, atlas.findRegions("player/ghost-idle"),  Animation.PlayMode.LOOP);
        playerMoveAnimation   = new Animation<>(0.1f, atlas.findRegions("player/ghost-move"),   Animation.PlayMode.LOOP);
//        playerAttackAnimation = new Animation<>(0.1f, atlas.findRegions("player/ghost-move"), Animation.PlayMode.NORMAL);
//        playerJumpAnimation   = new Animation<>(0.03f, atlas.findRegions("player/ghost-jump"),  Animation.PlayMode.NORMAL);
//        playerFallAnimation   = new Animation<>(0.1f, atlas.findRegions("player/ghost-fall"),  Animation.PlayMode.NORMAL);
//        playerDieAnimation    = new Animation<>(0.1f, atlas.findRegions("player/ghost-move"),   Animation.PlayMode.NORMAL);

        grayAnimation       = new Animation<>(0.1f, atlas.findRegions("enemies/gray/gray-idle"),  Animation.PlayMode.LOOP);
        grayMoveAnimation   = new Animation<>(0.1f, atlas.findRegions("enemies/gray/gray-run"),   Animation.PlayMode.LOOP);
        grayAttackAnimation = new Animation<>(0.07f, atlas.findRegions("enemies/gray/gray-shoot"), Animation.PlayMode.NORMAL);
        grayJumpAnimation   = new Animation<>(0.03f, atlas.findRegions("enemies/gray/gray-jump"),  Animation.PlayMode.NORMAL);
        grayFallAnimation   = new Animation<>(0.1f, atlas.findRegions("enemies/gray/gray-jump"),  Animation.PlayMode.REVERSED);
        grayDieAnimation    = new Animation<>(0.1f, atlas.findRegions("enemies/gray/gray-idle"),   Animation.PlayMode.NORMAL);

        soldierAnimation       = new Animation<>(0.1f, atlas.findRegions("enemies/soldier/soldier-idle"),  Animation.PlayMode.LOOP);
        soldierMoveAnimation   = new Animation<>(0.1f, atlas.findRegions("enemies/soldier/soldier-run"),   Animation.PlayMode.LOOP);
        soldierAttackAnimation = new Animation<>(0.04f, atlas.findRegions("enemies/soldier/soldier-shoot"), Animation.PlayMode.NORMAL);
        soldierJumpAnimation   = new Animation<>(0.03f, atlas.findRegions("enemies/soldier/soldier-jump"),  Animation.PlayMode.NORMAL);
        soldierFallAnimation   = new Animation<>(0.1f, atlas.findRegions("enemies/soldier/soldier-jump"),  Animation.PlayMode.REVERSED);
        soldierDieAnimation    = new Animation<>(0.1f, atlas.findRegions("enemies/soldier/soldier-idle"),   Animation.PlayMode.NORMAL);

        zuckTankMissileAnimation = new Animation<>(0.3f, atlas.findRegions("bosses/zuck-tank/zuck-tank-missile/zuck-missile"), Animation.PlayMode.NORMAL);
        zuckTankLowerAnimation   = new Animation<>(0.2f, atlas.findRegions("bosses/zuck-tank/zuck-tank-lower/zuck-tank-lower"), Animation.PlayMode.NORMAL);
        zuckTankTalkAnimation    = new Animation<>(0.2f, atlas.findRegions("bosses/zuck-tank/zuck-tank-talk/zuck-tank-talk"), Animation.PlayMode.LOOP);
        zuckTankIdleAAnimation   = new Animation<>(0.2f, atlas.findRegions("bosses/zuck-tank/zuck-tank-idle-a/zuck-tank-idle-a"), Animation.PlayMode.LOOP_PINGPONG);
        zuckTankIdleBAnimation   = new Animation<>(0.2f, atlas.findRegions("bosses/zuck-tank/zuck-tank-idle-b/zuck-tank-idle-b"), Animation.PlayMode.LOOP_PINGPONG);
        zuckTankShootAnimation   = new Animation<>(0.2f, atlas.findRegions("bosses/zuck-tank/zuck-tank-shoot/zuck-tank-shoot"), Animation.PlayMode.NORMAL);
        zuckTankRamTellAnimation = new Animation<>(0.2f, atlas.findRegions("bosses/zuck-tank/zuck-tank-ram-tell/zuck-tank-ram-tell"), Animation.PlayMode.NORMAL);
        zuckTankRamActAnimation  = new Animation<>(0.1f, atlas.findRegions("bosses/zuck-tank/zuck-tank-ram-state/zuck-tank-ram-state"), Animation.PlayMode.LOOP);

        dogeCoinAnimation  = new Animation<>(0.066f, atlas.findRegions("pickups/dogecoin/doge-coin"), Animation.PlayMode.LOOP);
        bitCoinAnimation  = new Animation<>(0.066f, atlas.findRegions("pickups/bitcoin/bitcoin-coin"), Animation.PlayMode.LOOP);

        leverAnimation = new Animation<>(0.2f, atlas.findRegions("interactables/lever"), Animation.PlayMode.NORMAL);
        doorAnimation  = new Animation<>(0.2f, atlas.findRegions("interactables/door"),  Animation.PlayMode.NORMAL);

        downArrowsAnimation  = new Animation<>(0.2f, atlas.findRegions("world/elevator-indicator"),  Animation.PlayMode.LOOP);

        debugNinePatch = new NinePatch(atlas.findRegion("debug-patch"), 6, 6, 6, 6);
        roundedBoxNinePatch = new NinePatch(atlas.findRegion("round_box"), 14, 14, 14, 14);

        transitions = new Transitions();
        transitions.blinds     = loadShader("shaders/transitions/default.vert", "shaders/transitions/blinds.frag");
        transitions.fade       = loadShader("shaders/transitions/default.vert", "shaders/transitions/dissolve.frag");
        transitions.radial     = loadShader("shaders/transitions/default.vert", "shaders/transitions/radial.frag");
        transitions.doom       = loadShader("shaders/transitions/default.vert", "shaders/transitions/doomdrip.frag");
        transitions.pixelize   = loadShader("shaders/transitions/default.vert", "shaders/transitions/pixelize.frag");
        transitions.doorway    = loadShader("shaders/transitions/default.vert", "shaders/transitions/doorway.frag");
        transitions.crosshatch = loadShader("shaders/transitions/default.vert", "shaders/transitions/crosshatch.frag");
        transitions.ripple     = loadShader("shaders/transitions/default.vert", "shaders/transitions/ripple.frag");
        transitions.heart      = loadShader("shaders/transitions/default.vert", "shaders/transitions/heart.frag");
        transitions.stereo     = loadShader("shaders/transitions/default.vert", "shaders/transitions/stereo.frag");
        transitions.circleCrop = loadShader("shaders/transitions/default.vert", "shaders/transitions/circlecrop.frag");
        transitions.cube       = loadShader("shaders/transitions/default.vert", "shaders/transitions/cube.frag");
        transitions.dreamy     = loadShader("shaders/transitions/default.vert", "shaders/transitions/dreamy.frag");

        attackSound = mgr.get("audio/sound/attack.ogg", Sound.class);
        bulletHitSound = mgr.get("audio/sound/bullethit.ogg", Sound.class);
        captureSound = mgr.get("audio/sound/capture.ogg", Sound.class);
        coinSound = mgr.get("audio/sound/coin.ogg", Sound.class);
        deathSound = mgr.get("audio/sound/die.ogg", Sound.class);
        doorSound = mgr.get("audio/sound/dooropen.ogg", Sound.class);
        exampleSound = mgr.get("audio/sound/example.wav", Sound.class);
        jumpSound = mgr.get("audio/sound/jump.ogg", Sound.class);
        leverSound = mgr.get("audio/sound/lever.ogg", Sound.class);
        missileSound = mgr.get("audio/sound/missile.ogg", Sound.class);
        pew1Sound = mgr.get("audio/sound/pew1.ogg", Sound.class);
        pew2Sound = mgr.get("audio/sound/pew2.ogg", Sound.class);
        pew3Sound = mgr.get("audio/sound/pew3.ogg", Sound.class);
        pew4Sound = mgr.get("audio/sound/pew4.ogg", Sound.class);
        uncaptureSound = mgr.get("audio/sound/uncapture.ogg", Sound.class);
        zuckRamSound = mgr.get("audio/sound/zuckram.ogg", Sound.class);
        zuckTank1Sound = mgr.get("audio/sound/zucktank1.ogg", Sound.class);
        zuckTank2Sound = mgr.get("audio/sound/zucktank2.ogg", Sound.class);
        zuckTank3Sound = mgr.get("audio/sound/zucktank3.ogg", Sound.class);

        exampleMusic = mgr.get("audio/music/intro.ogg", Music.class);
        introMusic = mgr.get("audio/music/intro.ogg", Music.class);
        level1Music = mgr.get("audio/music/level1.ogg", Music.class);
        level1ElevatorMusic = mgr.get("audio/music/elevator-to-level1.ogg", Music.class);
        level1BossMusic = mgr.get("audio/music/level1-boss.ogg", Music.class);
        level2Music = mgr.get("audio/music/level2.ogg", Music.class);
        level2ElevatorMusic = mgr.get("audio/music/elevator-to-level2.ogg", Music.class);
        level3Music = mgr.get("audio/music/level3.ogg", Music.class);
        level3ElevatorMusic = mgr.get("audio/music/elevator-to-level3.ogg", Music.class);

        Transitions.shaders = new Array<>();
        Transitions.shaders.addAll(
                transitions.blinds,
                transitions.fade,
                transitions.radial,
                transitions.doom,
                transitions.pixelize,
                transitions.doorway,
                transitions.crosshatch,
                transitions.ripple,
                transitions.heart,
                transitions.stereo,
                transitions.circleCrop,
                transitions.cube,
                transitions.dreamy
        );

        particles = new Particles();
        particles.circle  = atlas.findRegion("particles/circle");
        particles.sparkle = atlas.findRegion("particles/sparkle");
        particles.smoke   = atlas.findRegion("particles/smoke");
        particles.ring    = atlas.findRegion("particles/ring");

        strings = mgr.get("i18n/strings", I18NBundle.class);

        initialized = true;
        return 1;
    }


    @Override
    public void dispose() {
        transitions.blinds.dispose();
        transitions.fade.dispose();
        transitions.radial.dispose();
        transitions.doom.dispose();
        transitions.pixelize.dispose();
        transitions.doorway.dispose();
        transitions.crosshatch.dispose();
        transitions.ripple.dispose();
        transitions.heart.dispose();
        transitions.stereo.dispose();
        transitions.circleCrop.dispose();
        transitions.cube.dispose();
        transitions.dreamy.dispose();
        mgr.dispose();
        shapes.dispose();
        batch.dispose();
    }

    private static ShaderProgram loadShader(String vertSourcePath, String fragSourcePath) {
        ShaderProgram.pedantic = false;
        ShaderProgram shaderProgram = new ShaderProgram(
                Gdx.files.internal(vertSourcePath),
                Gdx.files.internal(fragSourcePath));

        if (!shaderProgram.isCompiled()) {
            // Gdx.app.error("LoadShader", "compilation failed:\n" + shaderProgram.getLog());
            throw new GdxRuntimeException("LoadShader: compilation failed:\n" + shaderProgram.getLog());
        } else if (Config.debugShaders){
             Gdx.app.setLogLevel(Gdx.app.LOG_INFO);
             Gdx.app.debug("LoadShader", "ShaderProgram compilation log: " + shaderProgram.getLog());
        }

        return shaderProgram;
    }

    public static class Transitions {
        public ShaderProgram blinds;
        public ShaderProgram fade;
        public ShaderProgram radial;
        public ShaderProgram doom;
        public ShaderProgram pixelize;
        public ShaderProgram doorway;
        public ShaderProgram crosshatch;
        public ShaderProgram ripple;
        public ShaderProgram heart;
        public ShaderProgram stereo;
        public ShaderProgram circleCrop;
        public ShaderProgram cube;
        public ShaderProgram dreamy;
        public static Array<ShaderProgram> shaders;
    }

    public static class Particles {
        public TextureRegion circle;
        public TextureRegion sparkle;
        public TextureRegion smoke;
        public TextureRegion ring;
    }

}
