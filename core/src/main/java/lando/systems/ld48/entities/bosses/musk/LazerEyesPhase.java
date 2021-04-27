package lando.systems.ld48.entities.bosses.musk;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld48.Audio;
import lando.systems.ld48.entities.Player;
import lando.systems.ld48.entities.bosses.Boss;
import lando.systems.ld48.entities.bosses.BossPhase;
import lando.systems.ld48.levels.Level;
import lando.systems.ld48.utils.accessors.Vector2Accessor;

public class LazerEyesPhase extends BossPhase {

    private final MuskKrang muskKrang;
    private boolean blastin;
    private final Vector2 target;
    private final Vector2 eye1Start;
    private final Vector2 eye1End;
    private final Vector2 eye2Start;
    private final Vector2 eye2End;
    private float iFramesTimer;

    public LazerEyesPhase(Boss boss) {
        super(boss, () -> new IdlePhase(boss)); //new RamPhase(boss));
        this.muskKrang = (MuskKrang) boss;
        this.complete = false;
        this.target = new Vector2();
        this.eye1Start = new Vector2();
        this.eye1End   = new Vector2();
        this.eye2Start = new Vector2();
        this.eye2End   = new Vector2();
        this.iFramesTimer = 0f;

        muskKrang.animation = muskKrang.animations.shootA;
        muskKrang.stateTime = 0f;

        this.blastin = false;

        TiledMapTileLayer tileLayer = muskKrang.screen.level.getLayer(Level.LayerType.collision).tileLayer;
        float mapHeight = tileLayer.getHeight() * tileLayer.getTileHeight();
        Vector2 targetA = new Vector2(muskKrang.position.x, 0);
        Vector2 targetB = new Vector2(0, 0);
        Vector2 targetC = new Vector2(0, mapHeight);
        Vector2 targetD = new Vector2(muskKrang.position.x, mapHeight);

        this.target.set(targetA);
        Timeline.createSequence()
                .pushPause(2f)
//                .push(Tween.call((type, source) -> blastin = true))
                .push(Tween.call((type, source) -> {
                    blastin = true;
                    muskKrang.screen.game.audio.playSound(Audio.Sounds.laser);
                }))
                .push(Tween.to(target, Vector2Accessor.XY, 1.0f).target(targetB.x, targetB.y))//.ease(Quint.INOUT))
//                .push(Tween.call((type, source) -> blastin = false))
//                .pushPause(0.6f)
//                .push(Tween.call((type, source) -> blastin = true))
                .push(Tween.to(target, Vector2Accessor.XY, 1.5f).target(targetC.x, targetC.y))//.ease(Quint.INOUT))
//                .push(Tween.call((type, source) -> blastin = false))
//                .pushPause(0.6f)
//                .push(Tween.call((type, source) -> blastin = true))
                .push(Tween.to(target, Vector2Accessor.XY, 1.0f).target(targetD.x, targetD.y))//.ease(Quint.INOUT))
//                .push(Tween.call((type, source) -> blastin = false))
//                .pushPause(0.6f)
                .push(Tween.call((type, source) -> complete = true))
                .start(muskKrang.screen.game.tween);

        Gdx.app.log("lazer phase", "started");
    }

    @Override
    public void update(float dt) {
        if (iFramesTimer == 0f && blastin) {
            Player player = muskKrang.screen.player;
            eye1Start.set(muskKrang.position.x - 28, muskKrang.position.y + 43);
            eye2Start.set(muskKrang.position.x - 63, muskKrang.position.y + 33);
            eye1End.set(target.x, target.y);
            eye2End.set(target.x, target.y);
            boolean hitPlayer1 = Intersector.intersectSegmentRectangle(eye1Start, eye1End, player.collisionBounds);
            boolean hitPlayer2 = Intersector.intersectSegmentRectangle(eye2Start, eye2End, player.collisionBounds);
            if (hitPlayer1 || hitPlayer2) {
                player.hitPoints -= 2;
                muskKrang.screen.particles.interact(player.position.x, player.position.y);
                iFramesTimer = 2f;
            }
        }

        iFramesTimer -= dt;
        if (iFramesTimer < 0f) {
            iFramesTimer = 0f;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!blastin) return;

        // NOTE: killing the batch like this to draw some rects is trash garbage and might cause problems
        batch.end();
        {
            ShapeRenderer shapes = muskKrang.assets.shapes;
            shapes.setProjectionMatrix(muskKrang.screen.getWorldCamera().combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            {
                float width = 2f;
                shapes.setColor(Color.RED);
                shapes.rectLine(eye1Start, target, width * 3);
                shapes.rectLine(eye2Start, target, width * 3);
                shapes.setColor(Color.ORANGE);
                shapes.rectLine(eye1Start, target, width * 2);
                shapes.rectLine(eye2Start, target, width * 2);
                shapes.setColor(Color.YELLOW);
                shapes.rectLine(eye1Start, target, width);
                shapes.rectLine(eye2Start, target, width);
                shapes.setColor(Color.WHITE);
                shapes.rectLine(eye1Start, target, 1);
                shapes.rectLine(eye2Start, target, 1);
                shapes.setColor(Color.WHITE);
            }
            shapes.end();
        }
        batch.begin();
    }

}
