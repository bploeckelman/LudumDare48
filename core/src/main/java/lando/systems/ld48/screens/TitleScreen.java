package lando.systems.ld48.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import lando.systems.ld48.Game;
import lando.systems.ld48.utils.Time;

public class TitleScreen extends BaseScreen {

    float stateTime = 0f;
    float backgroundAlpha = 0f;
    Animation<TextureRegion> cat;
    Animation<TextureRegion> dog;

    private boolean hasSetScreen = false;

    public TitleScreen(Game game) {
        super(game);
        cat = game.assets.cat;
        dog = game.assets.dog;
    }

    @Override
    public void alwaysUpdate(float dt) {
        backgroundAlpha -= dt;
        if (backgroundAlpha < 0f) {
            backgroundAlpha = 0f;
        }
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
        if ((Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) && !hasSetScreen) {
            game.setScreen(new GameScreen(game));
            hasSetScreen = true;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        {
            float width  = worldCamera.viewportWidth;
            float height = worldCamera.viewportHeight;

            batch.setColor(Color.WHITE);
            batch.draw(game.assets.title, 0, 0, width, height);
        }
        batch.end();

        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            float width  = windowCamera.viewportWidth;
            float height = windowCamera.viewportHeight;
            float targetWidth = (1f / 3f) * width - 75f;
            float x = width - targetWidth - 20f;
            float y = (1f / 3f) * height - 110f;

            game.assets.layout.setText(game.assets.pixelFont16, "Click to start your adventure!", Color.WHITE, targetWidth, Align.center, true);
            batch.setColor(Color.WHITE);
            game.assets.roundedBoxNinePatch.draw(batch, x - 7.5f, y - 120f, targetWidth + 15f,  100f);
            batch.setColor(64f / 255f, 64f / 255f, 64f / 255f, 0.5f);
            batch.draw(game.assets.whitePixel, x - 7.5f, y - 120f, targetWidth + 15f, 100f);
            batch.setColor(Color.WHITE);
            game.assets.pixelFont16.draw(batch, game.assets.layout, x, y - 35f);
        }
        batch.end();
    }

}
