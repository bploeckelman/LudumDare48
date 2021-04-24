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
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Time.pause_for(1f);
            backgroundAlpha = 1f;
        }
        if (Gdx.input.justTouched()) {
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        {
            float width  = worldCamera.viewportWidth;
            float height = worldCamera.viewportHeight;

            batch.setColor(Color.SKY);
            batch.draw(game.assets.pixel, 0, 0, width, height);

            batch.setColor(1f, 1f, 1f, backgroundAlpha);
            batch.draw(game.assets.title, 0, 0, width, height);

            batch.setColor(Color.WHITE);
            TextureRegion catKeyFrame = cat.getKeyFrame(stateTime);
            TextureRegion dogKeyFrame = dog.getKeyFrame(stateTime);
            if (!catKeyFrame.isFlipX()) {
                catKeyFrame.flip(true, false);
            }
            float left  = (1f / 3f) * width - catKeyFrame.getRegionWidth() / 2f;
            float right = (2f / 3f) * width - dogKeyFrame.getRegionWidth() / 2f;
            float y     = (1f / 4f) * height;
            batch.draw(catKeyFrame, left, y);
            batch.draw(dogKeyFrame, right, y);
        }
        batch.end();

        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            float width  = windowCamera.viewportWidth;
            float height = windowCamera.viewportHeight;
            float targetWidth = (1f / 3f) * width;
            game.assets.layout.setText(game.assets.pixelFont16, "Is Game,\n\ntouch to play!", Color.SALMON, targetWidth, Align.center, true);

            float x = targetWidth;
            float y = (2f / 3f) * height;
            game.assets.pixelFont16.draw(batch, game.assets.layout, x, y);
        }
        batch.end();
    }

}
