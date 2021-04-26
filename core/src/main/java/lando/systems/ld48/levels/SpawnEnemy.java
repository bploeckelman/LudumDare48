package lando.systems.ld48.levels;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld48.Assets;
import lando.systems.ld48.entities.EnemyEntity;
import lando.systems.ld48.entities.enemies.Gray;
import lando.systems.ld48.entities.enemies.Reptilian;
import lando.systems.ld48.entities.enemies.ReptilianBaby;
import lando.systems.ld48.entities.enemies.Soldier;
import lando.systems.ld48.screens.GameScreen;

public class SpawnEnemy {

    public enum Type { soldier, alien, zuck, reptilian, reptilianBaby }

    public Vector2 pos;
    public float size = Level.TILE_SIZE;
    public TextureRegion texture;
    public Type type;

    public SpawnEnemy(Type type, float x, float y, Assets assets) {
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
        EnemyEntity enemy = null;
        switch (type) {
            case soldier: {
                enemy = new Soldier(screen, pos.x, pos.y);
            } break;
            case alien: {
                enemy = new Gray(screen, pos.x, pos.y);
            } break;
            case reptilian: {
                enemy = new Reptilian(screen, pos.x, pos.y);
                break;
            }
            case reptilianBaby: {
                enemy = new ReptilianBaby(screen, pos.x, pos.y);
                break;
            }
        }

        if (enemy == null) return;
        enemy.addToScreen(pos.x, pos.y + size);
    }

}
