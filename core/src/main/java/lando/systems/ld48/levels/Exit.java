package lando.systems.ld48.levels;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld48.Assets;
import lando.systems.ld48.utils.Time;

public class Exit {

    public Vector2 pos;
    public LevelTransition.Type levelTransitionType;
    public LevelDescriptor targetLevel;
    public String transitionString;
    public TextureRegion texture;
    public Rectangle bounds;
    public float size = Level.TILE_SIZE;
    public Animation<TextureRegion> indicatorAnim;
    public float stateTime = 0f;

    public Exit(float x, float y,
                LevelTransition.Type levelTransitionType,
                LevelDescriptor targetLevel,
                String transitionString,
                Assets assets) {
        this.pos = new Vector2(x, y);
        this.texture = assets.whitePixel;
        this.levelTransitionType = levelTransitionType;
        this.targetLevel = targetLevel;
        this.bounds = new Rectangle(pos.x, pos.y, size, size);
        this.transitionString = transitionString;
        this.indicatorAnim = assets.downArrowsAnimation;
    }

    public Exit (LevelTransition.Type levelTransitionType, LevelDescriptor targetLevel, String transitionString) {
        this.levelTransitionType = levelTransitionType;
        this.targetLevel = targetLevel;
        this.transitionString = transitionString;
    }

    public void render(SpriteBatch batch) {
        stateTime += Time.delta;
        TextureRegion keyframe = indicatorAnim.getKeyFrame(stateTime);
        batch.draw(keyframe, bounds.x, bounds.y);
    }

    public void renderDebug(SpriteBatch batch) {
        batch.setColor(0f, 0f, 1f, 0.5f);
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        batch.setColor(1f, 1f, 1f, 1f);
    }

}
