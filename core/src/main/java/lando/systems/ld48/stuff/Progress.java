package lando.systems.ld48.stuff;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld48.Assets;

public class Progress {

    private Assets assets;
    private boolean vertical;

    public Progress(Assets assets) {
        this.assets = assets;
    }

    public Progress(Assets assets, boolean vertical) {
        this(assets);
        this.vertical = vertical;
    }
    public void draw(SpriteBatch batch, float progress, float x, float y, float width, float height) {
        this.draw(batch, progress, x, y, width, height, false);
    }
    public void draw(SpriteBatch batch, float progress, float x, float y, float width, float height, boolean asHit) {
        batch.setColor(Color.BLACK);
        batch.draw(assets.whitePixel, x - 1, y - 1, width + 2, height + 2);
        batch.setColor(Color.WHITE);
        batch.draw(assets.whitePixel, x, y, width, height);
        batch.setColor(asHit ? Color.GREEN : Color.RED);
        if (this.vertical) {
            batch.draw(assets.whitePixel, x, y, width, height * progress);
        } else {
            batch.draw(assets.whitePixel, x, y, width * progress, height);
        }
        batch.setColor(Color.WHITE);
    }
}
