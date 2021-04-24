package lando.systems.ld48;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import lando.systems.ld48.screens.BaseScreen;
import lando.systems.ld48.screens.LaunchScreen;
import lando.systems.ld48.screens.TitleScreen;
import lando.systems.ld48.utils.Time;
import lando.systems.ld48.utils.accessors.*;

public class Game extends ApplicationAdapter {

    public Assets assets;
    public Audio audio;
    public TweenManager tween;

    private BaseScreen screen;
    private ScreenTransition screenTransition;

    @Override
    public void create() {
        Time.init();

        assets = new Assets();

        audio = new Audio();

        tween = new TweenManager();
        Tween.setWaypointsLimit(4);
        Tween.setCombinedAttributesLimit(4);
        Tween.registerAccessor(Color.class, new ColorAccessor());
        Tween.registerAccessor(Rectangle.class, new RectangleAccessor());
        Tween.registerAccessor(Vector2.class, new Vector2Accessor());
        Tween.registerAccessor(Vector3.class, new Vector3Accessor());
        Tween.registerAccessor(OrthographicCamera.class, new CameraAccessor());

        screenTransition = new ScreenTransition(Config.windowWidth, Config.windowHeight);

        if (Config.showLaunchScreen || Gdx.app.getType() == Application.ApplicationType.WebGL) {
            setScreen(new LaunchScreen(this));
        } else {
            setScreen(new TitleScreen(this));
        }
    }

    public void update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        // update global timer
        Time.delta = Gdx.graphics.getDeltaTime();

        // update code that always runs (regardless of pause)
        screen.alwaysUpdate(Time.delta);

        // handle a pause
        if (Time.pause_timer > 0) {
            Time.pause_timer -= Time.delta;
            if (Time.pause_timer <= -0.0001f) {
                Time.delta = -Time.pause_timer;
            } else {
                // skip updates if we're paused
                return;
            }
        }
        Time.millis += Time.delta;
        Time.previous_elapsed = Time.elapsed_millis();

        // update systems
        tween.update(Time.delta);
        screen.update(Time.delta);
    }

    @Override
    public void render() {
        update();

        // render the active screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (!screenTransition.active) {
            screen.render(assets.batch);
        } else {
            screenTransition.updateAndRender(Time.delta, assets.batch, screen);
        }
    }

    @Override
    public void dispose() {
        screenTransition.dispose();
        assets.dispose();
    }

    public BaseScreen getScreen() {
        return screen;
    }

    public void setScreen(BaseScreen newScreen) {
        setScreen(newScreen, null);
    }
    public void setScreen(BaseScreen newScreen, ShaderProgram transitionShader) {
        // only one active transition at a time
        if (screenTransition.active || screenTransition.next != null) {
            return;
        }

        if (screen == null) {
            screen = newScreen;
        } else {
            if (transitionShader == null) {
                screenTransition.shader = Assets.Transitions.shaders.random();
            } else {
                screenTransition.shader = transitionShader;
            }

            Timeline.createSequence()
                    .pushPause(0.1f)
                    .push(Tween.call((i, baseTween) -> {
                        screenTransition.active = true;
                        screenTransition.percent.setValue(0f);
                        screenTransition.next = newScreen;
                    }))
                    .push(Tween.to(screenTransition.percent, -1, screenTransition.duration).target(1))
                    .push(Tween.call((i, baseTween) -> {
                        screen = screenTransition.next;
                        screenTransition.next = null;
                        screenTransition.active = false;
                    }))
                    .start(tween);
        }
    }

    // ------------------------------------------------------------------------

    static class ScreenTransition implements Disposable {

        boolean active = false;
        float duration = 0.5f;
        BaseScreen next;
        ShaderProgram shader;
        MutableFloat percent;
        Texture sourceTexture;
        Texture destTexture;
        FrameBuffer sourceFramebuffer;
        FrameBuffer destFramebuffer;

        public ScreenTransition(int windowWidth, int windowHeight) {
            next = null;
            shader = null;
            percent = new MutableFloat(0f);
            sourceFramebuffer = new FrameBuffer(Pixmap.Format.RGBA8888, windowWidth, windowHeight, false);
            destFramebuffer   = new FrameBuffer(Pixmap.Format.RGBA8888, windowWidth, windowHeight, false);
            sourceTexture = sourceFramebuffer.getColorBufferTexture();
            destTexture   = destFramebuffer.getColorBufferTexture();
        }

        @Override
        public void dispose() {
            sourceTexture.dispose();
            destTexture.dispose();
            sourceFramebuffer.dispose();
            destFramebuffer.dispose();
        }

        public void updateAndRender(float delta, SpriteBatch batch, BaseScreen screen) {
            // update the next screen
            next.update(delta);

            // render the next screen to a buffer
            destFramebuffer.begin();
            {
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                next.render(batch);
            }
            destFramebuffer.end();

            // render the current screen to a buffer
            sourceFramebuffer.begin();
            {
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                screen.render(batch);
            }
            sourceFramebuffer.end();

            batch.setShader(shader);
            batch.begin();
            {
                shader.setUniformf("u_percent", percent.floatValue());

                sourceTexture.bind(1);
                shader.setUniformi("u_texture1", 1);

                destTexture.bind(0);
                shader.setUniformi("u_texture", 0);

                // TODO - this only works cleanly if source and dest equal window size,
                //  if one screen has a different size it ends up either too big or too small during the transition
                batch.setColor(Color.WHITE);
                batch.draw(destTexture, 0, 0, Config.windowWidth, Config.windowHeight);
            }
            batch.end();
            batch.setShader(null);
        }

    }

}