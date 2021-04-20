package lando.systems.ld48;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Assets implements Disposable {

    public enum Load { ASYNC, SYNC }

    public boolean initialized;

    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public GlyphLayout layout;
    public AssetManager mgr;
    public TextureAtlas atlas;
    public Transitions transitions;

    public Texture pixel;
    public Texture title;

    public Animation<TextureRegion> cat;
    public Animation<TextureRegion> dog;

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

            mgr.load(new AssetDescriptor<>("sprites/sprites.atlas", TextureAtlas.class));
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

        atlas = mgr.get("sprites/sprites.atlas");

        cat = new Animation<>(0.1f, atlas.findRegions("pets/cat"), Animation.PlayMode.LOOP);
        dog = new Animation<>(0.1f, atlas.findRegions("pets/dog"), Animation.PlayMode.LOOP);

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

        initialized = true;
        return 1;
    }


    @Override
    public void dispose() {

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

}
