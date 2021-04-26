package lando.systems.ld48.levels;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Back;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import lando.systems.ld48.Assets;
import lando.systems.ld48.Audio;
import lando.systems.ld48.entities.Player;
import lando.systems.ld48.screens.EndScreen;
import lando.systems.ld48.screens.GameScreen;
import lando.systems.ld48.ui.typinglabel.TypingLabel;

// NOTE: one problem still, captured creatures don't come out of the level transition intact

public class LevelTransition {

    public enum Type { military, organic, alien, ending }

    private Type type;
    private Texture background;
    private LevelDescriptor targetLevel;
    private Assets assets;
    private GameScreen screen;
    private Audio.Musics music;

    private Player player;
    private TextureRegion pixel;
    private Animation<TextureRegion> platform;
    private float stateTime = 0f;

    // texture scrolling
    private float scroll = 0f;
    private final float scrollSpeed = 0.3f;

    private MutableFloat height = new MutableFloat(0f);
    private MutableFloat fade = new MutableFloat(1f);
    private boolean introComplete = false;
    private boolean outroStarted = false;

    private TypingLabel typingLabel;

    public LevelTransition(Exit exit, GameScreen screen) {
        this.type = exit.levelTransitionType;
        this.targetLevel = exit.targetLevel;
        switch (type) {
            default:
            //TODO: update transition text to fit the lore, update transitionText as necessary. May need to move transitionText to more appropriate place
            case military: {
                background = screen.game.assets.levelTransitionMilitary;
                music = Audio.Musics.level1elevator;
            } break;
            case organic: {
                background = screen.game.assets.levelTransitionOrganic;
                music = Audio.Musics.level2elevator;
            } break;
            case alien: {
                background = screen.game.assets.levelTransitionAlien;
                music = Audio.Musics.level3elevator;
            } break;
            case ending: {
                background = screen.game.assets.levelTransitionMilitary;
                music = Audio.Musics.level1boss;
            } break;
        }
        screen.game.audio.playMusic(music, true);

        this.screen = screen;
        this.assets = screen.game.assets;

        this.player = screen.player;
        this.pixel = assets.whitePixel;
        this.platform = assets.elevatorPlatformAnimation;

        GameScreen.CameraConstraints.override = true;
        GameScreen.CameraConstraints.targetPos.set(screen.getWorldCamera().viewportWidth / 2f, screen.getWorldCamera().viewportHeight / 2f);
        screen.getWorldCamera().position.set(GameScreen.CameraConstraints.targetPos.x, GameScreen.CameraConstraints.targetPos.y, 0f);
        screen.getWorldCamera().update();

        final float screenWidth  = screen.getWorldCamera().viewportWidth;
        final float screenHeight = screen.getWorldCamera().viewportHeight;

        this.typingLabel = new TypingLabel(assets.pixelFont16, assets.strings.get(exit.transitionString), 90, screenHeight - 90);
        this.typingLabel.setWidth((1f / 3f) * screenWidth);
        this.typingLabel.setFontScale(.16f);
        this.typingLabel.setLineAlign(Align.left);
        this.typingLabel.setX(0);
        this.typingLabel.setY(screenHeight / 2f + typingLabel.getHeight() / 2f - 15);

        // setup transition
        final float introDuration = 2f;
        Timeline.createSequence()
                .push(
                        Timeline.createParallel()
                                .push(Tween.set(fade, -1).target(1f))
                                .push(Tween.set(height, -1).target(-screenHeight / 2f))
                )
                .push(
                        Timeline.createParallel()
                                .push(Tween.to(fade,   -1, introDuration).target(0f))
                                .push(Tween.to(height, -1, introDuration).target(0).ease(Back.OUT))
                )
                .setCallback((type, source) -> introComplete = true)
                .start(screen.game.tween);
    }

    public void update(float dt) {
        scroll -= dt * scrollSpeed;
        stateTime += dt;

        typingLabel.update(dt);
        if (!typingLabel.hasEnded() && Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            typingLabel.skipToTheEnd();
        }

        if (typingLabel.hasEnded() && introComplete && !outroStarted) {
            outroStarted = true;
            final float outroDuration = 1.5f;
            final float screenHeight = screen.getWorldCamera().viewportHeight;
            Timeline.createSequence()
                    .push(
                            Timeline.createParallel()
                                    .push(Tween.to(fade,   -1, outroDuration).target(1f))
                                    .push(Tween.to(height, -1, outroDuration).target(screenHeight / 2f).ease(Back.IN))
                    )
                    .setCallback((type, source) -> goToNextLevel(screen))
                    .start(screen.game.tween);
        }
    }

    public void render(SpriteBatch batch, OrthographicCamera camera) {
        // black background
        batch.setColor(Color.BLACK);
        batch.draw(pixel, 0, 0, camera.viewportWidth, camera.viewportHeight);
        batch.setColor(Color.WHITE);

        // scrolling background
        float wCenter = camera.viewportWidth / 2f;
        float hCenter = camera.viewportHeight / 2f;
        float w = background.getWidth();
        float h = background.getHeight();
        float x = wCenter - w / 2f;
        float y = 0;
        float u = 0;
        float v = scroll;
        float u2 = 1;
        float v2 = 1 + scroll;
        batch.draw(background, x, y, w, h, u, v, u2, v2);

        // offset controlled by tween in ctor
        float transitionHeightOffset = height.floatValue();

        TextureRegion platformKeyframe = platform.getKeyFrame(stateTime);
        float platformX = wCenter - platformKeyframe.getRegionWidth() / 2f;
        float platformY = hCenter - platformKeyframe.getRegionHeight() - player.imageBounds.height / 2f - transitionHeightOffset;
        batch.draw(platformKeyframe, platformX, platformY);

        TextureRegion playerKeyframe = player.animationSet.IdleAnimation.getKeyFrame(stateTime);
        float playerX = wCenter - playerKeyframe.getRegionWidth() / 2f;
        float playerY = hCenter - playerKeyframe.getRegionHeight() / 2f - transitionHeightOffset;
        batch.draw(playerKeyframe, playerX, playerY);

        typingLabel.render(batch);

        // fade to black overlay
        batch.setColor(0f / 255f, 0f / 255f, 16 / 255f, fade.floatValue());
        batch.draw(pixel, 0, 0, camera.viewportWidth, camera.viewportHeight);
        batch.setColor(Color.WHITE);
    }

    private void goToNextLevel(GameScreen screen) {
        switch (type) {
            default:
            case military: {
                music = Audio.Musics.level1;
            } break;
            case organic: {
                music = Audio.Musics.level2;
            } break;
            case alien: {
                music = Audio.Musics.level3;
            } break;
            case ending: {
                music = Audio.Musics.level1boss;
            } break;
        }
        screen.game.audio.playMusic(music, true);

        if (this.type == LevelTransition.Type.ending && this.targetLevel == LevelDescriptor.ending) {
            screen.game.setScreen(new EndScreen(screen.game));
        } else {
            GameScreen.CameraConstraints.override = false;
            screen.loadLevel(targetLevel);
        }
    }

}
