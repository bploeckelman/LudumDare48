package lando.systems.ld48.levels;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld48.Assets;
import lando.systems.ld48.entities.InteractableEntity;
import lando.systems.ld48.screens.GameScreen;

public class SpawnInteractable {

    public boolean interactionDisabled = false;

    public enum Type { lever, door, pizzaGate, alienGate, reptilianGate }

    public int id;
    public int targetId;

    public Vector2 pos;
    public float size = Level.TILE_SIZE;
    public TextureRegion texture;
    public SpawnInteractable.Type type;

    public SpawnInteractable(SpawnInteractable.Type type, int id, int targetId, float x, float y, Assets assets) {
        this.type = type;
        this.id = id;
        this.targetId = targetId;
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
            case lever: anim = screen.game.assets.leverAnimation; break;
            case door:
                switch (screen.level.currentLevel) {
                    case alien:
                        anim = screen.game.assets.alienGateIdle; break;
                    case reptilian:
                        anim = screen.game.assets.reptilianGateIdle; break;
                    case military:
                        anim = screen.game.assets.doorAnimation; break;
                    case zuck_arena:
                    case musk_arena:
                        anim = screen.game.assets.pizzaGateIdle; break;
                    default:
                        anim = screen.game.assets.doorAnimation;  break;
                }


        }
        InteractableEntity entity = new InteractableEntity(screen, pos.x, pos.y, this, anim);
        entity.disabled = interactionDisabled;
        entity.addToScreen(pos.x, pos.y + size);
    }

}
