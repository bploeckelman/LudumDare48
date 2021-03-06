package lando.systems.ld48.levels;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld48.Assets;

public class SpawnPlayer {

    public Vector2 pos;
    public float size = Level.TILE_SIZE;
    public TextureRegion texture;

    public SpawnPlayer(float x, float y, Assets assets) {
        this.pos = new Vector2(x, y);
        this.texture = assets.whitePixel;
    }

    public void render(SpriteBatch batch) {
        batch.setColor(0f, 1f, 0f, 0.5f);
        batch.draw(texture, pos.x, pos.y, size, size);
        batch.setColor(1f, 1f, 1f, 1f);
    }

}
