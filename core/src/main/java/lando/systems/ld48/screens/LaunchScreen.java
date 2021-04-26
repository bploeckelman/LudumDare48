package lando.systems.ld48.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld48.Game;

public class LaunchScreen extends BaseScreen {

    public LaunchScreen(Game game) {
        super(game);
    }

    private boolean hasSetScreen = false;

    @Override
    public void update(float dt) {
        if (Gdx.input.justTouched() && !hasSetScreen) {
            game.setScreen(new TitleScreen(game));
            hasSetScreen = true;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            batch.draw(game.assets.title, 0, 0, windowCamera.viewportWidth, windowCamera.viewportHeight);
        }
        batch.end();
    }

}
