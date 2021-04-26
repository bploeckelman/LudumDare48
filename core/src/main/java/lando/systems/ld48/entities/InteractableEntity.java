package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.ld48.levels.SpawnInteractable;
import lando.systems.ld48.screens.GameScreen;
import lando.systems.ld48.utils.Callback;

public class InteractableEntity extends GameEntity {

    public final SpawnInteractable.Type type;

    boolean completed = false;
    boolean active = false;
    Callback completionCallback;

    public InteractableEntity(GameScreen screen, float x, float y, SpawnInteractable.Type type, Animation<TextureRegion> anim) {
        super(screen, anim);
        this.type = type;

        float scale = 1f;
        float width = anim.getKeyFrames()[0].getRegionWidth();
        float height = anim.getKeyFrames()[0].getRegionHeight();
        initEntity(x, y, width * scale, height * scale);

        animationPaused = true;

        // TODO: read some data from the spawner and build a completion callback based on that
        //       eg. it's tied to some other interactable like a door, so the completion callback should trigger the door operation
//        completionCallback = null;
        completionCallback = params -> {
            screen.particles.interact(position.x, position.y);
            return null;
        };
    }

    public void interact() {
        if (completed) return;
        if (active) return;
        active = true;

        animationPaused = false;
    }

    @Override
    public void update(float dt) {
        boolean wasIncomplete = !completed;
        completed = animation.isAnimationFinished(stateTime);
        if (wasIncomplete && completed && completionCallback != null) {
            completionCallback.call();
        }
        super.update(dt);
    }


    @Override
    public void addToScreen(float x, float y) {
        screen.interactables.add(this);
        super.addToScreen(x, y);
    }

    @Override
    public void removeFromScreen() {
        screen.interactables.removeValue(this, true);
        super.removeFromScreen();
    }

}
