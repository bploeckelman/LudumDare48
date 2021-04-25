package lando.systems.ld48.levels;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Back;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import lando.systems.ld48.Assets;
import lando.systems.ld48.entities.Player;
import lando.systems.ld48.screens.GameScreen;

// NOTE: one problem still, captured creatures don't come out of the level transition intact

public class LevelTransition {

    public enum Type { military, organic, alien }

    private Type type;
    private Texture background;
    private LevelDescriptor targetLevel;
    private Assets assets;

    private Player player;
    private TextureRegion pixel;
    private Animation<TextureRegion> platform;
    private float stateTime = 0f;

    // texture scrolling
    private float scroll = 0f;
    private final float scrollSpeed = 0.3f;

    private MutableFloat height = new MutableFloat(0f);
    private MutableFloat fade = new MutableFloat(1f);

    private boolean drawText = false;
    private final String text = ""
            + "You're a ghost...\n\n"
            + "You died.\n\n"
            + "It probably hurt.\n\n"
            + "How did this happen?\n\n"
            + "Infiltrate the deeper state\nand find out\n\n\nif you dare........";

    public LevelTransition(Exit exit, GameScreen screen) {
        this.type = exit.levelTransitionType;
        this.targetLevel = exit.targetLevel;
        switch (type) {
            default:
            case military: background = screen.game.assets.levelTransitionMilitary; break;
            case organic:  background = screen.game.assets.levelTransitionOrganic;  break;
        }

        this.assets = screen.game.assets;

        this.player = screen.player;
        this.pixel = assets.whitePixel;
        this.platform = assets.elevatorPlatformAnimation;

        GameScreen.CameraConstraints.override = true;
        GameScreen.CameraConstraints.targetPos.set(screen.getWorldCamera().viewportWidth / 2f, screen.getWorldCamera().viewportHeight / 2f);
        screen.getWorldCamera().position.set(GameScreen.CameraConstraints.targetPos.x, GameScreen.CameraConstraints.targetPos.y, 0f);
        screen.getWorldCamera().update();

        // setup transition
        final float screenHeight = screen.getWorldCamera().viewportHeight;
        final float introDuration = 2f;
        final float outroDuration = 1.5f;
        final float pauseDuration = 3f;
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
                .push(Tween.call((type, source) -> drawText = true))
                .pushPause(pauseDuration)
                .push(
                        Timeline.createParallel()
                                .push(Tween.to(fade,   -1, outroDuration).target(1f))
                                .push(Tween.to(height, -1, outroDuration).target(screenHeight / 2f).ease(Back.IN))
                )
                .setCallback((type, source) -> goToNextLevel(screen))
                .start(screen.game.tween);
    }

    public void update(float dt) {
        scroll -= dt * scrollSpeed;
        stateTime += dt;
    }

    public void render(SpriteBatch batch, OrthographicCamera camera) {
        // black background
        batch.setColor(0f / 255f, 0f / 255f, 16 / 255f, 1f);
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

        // TODO - pull in typing label lib
        if (drawText) {
            float prevScaleX = assets.pixelFont16.getData().scaleX;
            float prevScaleY = assets.pixelFont16.getData().scaleY;
            assets.pixelFont16.getData().setScale(0.16f);
            float prevLineHeight = assets.pixelFont16.getData().lineHeight;
            assets.pixelFont16.getData().setLineHeight(60f);
            {
                assets.layout.setText(assets.pixelFont16, text, Color.FIREBRICK, (1f / 3f) * camera.viewportWidth, Align.right, false);
                assets.pixelFont16.draw(batch, assets.layout, 0f, camera.viewportHeight / 2f + assets.layout.height / 2f);
            }
            assets.pixelFont16.getData().setScale(prevScaleX, prevScaleY);
            assets.pixelFont16.getData().setLineHeight(prevLineHeight);
        }

        // fade to black overlay
        batch.setColor(0f / 255f, 0f / 255f, 16 / 255f, fade.floatValue());
        batch.draw(pixel, 0, 0, camera.viewportWidth, camera.viewportHeight);
        batch.setColor(Color.WHITE);
    }

    private void goToNextLevel(GameScreen screen) {
        GameScreen.CameraConstraints.override = false;
        screen.loadLevel(targetLevel);
    }

}
