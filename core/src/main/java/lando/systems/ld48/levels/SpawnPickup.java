package lando.systems.ld48.levels;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld48.Assets;
import lando.systems.ld48.entities.PickupEntity;
import lando.systems.ld48.screens.GameScreen;

public class SpawnPickup {

    public enum Type { dogecoin, bitcoin }

    public Vector2 pos;
    public float size = Level.TILE_SIZE;
    public TextureRegion texture;
    public SpawnPickup.Type type;

    public SpawnPickup(SpawnPickup.Type type, float x, float y, Assets assets) {
        this.type = type;
        this.pos = new Vector2(x, y);
        this.texture = assets.whitePixel;
    }

    public void render(SpriteBatch batch) {
        batch.setColor(1f, 1f, 0f, 0.5f);
        batch.draw(texture, pos.x, pos.y, size, size);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void spawn(GameScreen screen) {
        Animation<TextureRegion> anim;
        switch (type) {
            default:
            case dogecoin: anim = screen.game.assets.dogeCoinAnimation; break;
            case bitcoin: anim = screen.game.assets.bitCoinAnimation; break;
        }
        PickupEntity entity = new PickupEntity(screen, pos.x, pos.y, type, anim);
        if (entity == null) return;
        entity.addToScreen(pos.x, pos.y + size);

    }
}
