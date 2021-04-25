package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.ld48.levels.SpawnPickup;
import lando.systems.ld48.screens.GameScreen;

public class PickupEntity extends GameEntity {

    public final SpawnPickup.Type type;

    public PickupEntity(GameScreen screen, float x, float y, SpawnPickup.Type type, Animation<TextureRegion> anim) {
        super(screen, anim);
        this.type = type;

        float scale = 0.5f;
        float width = anim.getKeyFrames()[0].getRegionWidth();
        float height = anim.getKeyFrames()[0].getRegionHeight();
        initEntity(x, y, width * scale, height * scale);
    }

    @Override
    public void addToScreen(float x, float y) {
        screen.pickups.add(this);
        super.addToScreen(x, y);
    }

    @Override
    public void removeFromScreen() {
        screen.pickups.removeValue(this, true);
        super.removeFromScreen();
    }

}
