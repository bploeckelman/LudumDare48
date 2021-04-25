package lando.systems.ld48.stuff;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld48.Assets;

public class Progress {

    private Assets assets;

    public Progress(Assets assets) {
        this.assets = assets;
    }

    public void draw(SpriteBatch batch, float progress, float x, float y, float width, float height) {

        batch.setColor(Color.BLACK);
        batch.draw(assets.whitePixel, x - 1, y - 1, width + 2, height + 2);
        batch.setColor(Color.WHITE);
        batch.draw(assets.whitePixel, x, y, width, height);
        batch.setColor(Color.RED);
        batch.draw(assets.whitePixel, x, y, width * progress, height);
        batch.setColor(Color.WHITE);
    }
}
