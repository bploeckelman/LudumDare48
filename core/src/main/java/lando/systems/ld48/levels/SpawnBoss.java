package lando.systems.ld48.levels;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld48.Assets;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.zuck.ZuckTank;
import lando.systems.ld48.screens.GameScreen;

public class SpawnBoss {

    public enum Type { zuck, musk }

    public Vector2 pos;
    public float size = Level.TILE_SIZE;
    public TextureRegion texture;
    public SpawnBoss.Type type;

    public SpawnBoss(SpawnBoss.Type type, float x, float y, Assets assets) {
        this.type = type;
        this.pos = new Vector2(x, y);
        this.texture = assets.whitePixel;
    }

    public void render(SpriteBatch batch) {
        batch.setColor(1f, 0f, 1f, 0.8f);
        batch.draw(texture, pos.x, pos.y, size, size);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void spawn(GameScreen screen) {
        Boss boss = null;
        switch (type) {
            default:
            case zuck: boss = new ZuckTank(screen, pos.x, pos.y); break;
//            case musk: boss = new MuskKrang(screen, pos.x, pos.y); break;
        }

        if (boss == null) return;
        boss.addToScreen();
    }

}
