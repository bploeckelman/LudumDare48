package lando.systems.ld48.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ArrayMap;

public class AnimationSet {

    private ArrayMap<GameEntity.State, Animation<TextureRegion>> stateMap = new ArrayMap<>();

    public void setAnimation(GameEntity.State s, Animation<TextureRegion> anim) {
        stateMap.put(s, anim);
    }

    public Animation<TextureRegion> getAnimation(GameEntity.State s) {
        return stateMap.get(s);
    }

}
