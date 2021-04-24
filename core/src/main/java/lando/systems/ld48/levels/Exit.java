package lando.systems.ld48.levels;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld48.Assets;

public class Exit {

    public Vector2 pos;
    public TextureRegion texture;
    public Rectangle bounds;
    public float size = Level.TILE_SIZE;

    public Exit(float x, float y, Assets assets) {
        this.pos = new Vector2(x, y);
        this.texture = assets.whitePixel;
        this.bounds = new Rectangle(pos.x, pos.y, size, size);
    }

    public void render(SpriteBatch batch) {
        batch.setColor(0f, 0f, 1f, 0.5f);
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        batch.setColor(1f, 1f, 1f, 1f);
    }

}
