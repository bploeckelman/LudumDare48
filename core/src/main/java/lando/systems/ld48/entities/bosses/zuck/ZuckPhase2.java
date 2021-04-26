package lando.systems.ld48.entities.bosses.zuck;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Quint;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld48.entities.Player;
import lando.systems.ld48.entities.bosses.BossPhase;
import lando.systems.ld48.utils.accessors.Vector2Accessor;

public class ZuckPhase2 implements BossPhase {

    private final ZuckTank zuck;
    private boolean complete;
    private boolean blastin;
    private Vector2 target;
    private Vector2 targetA;
    private Vector2 targetB;
    private Vector2 targetC;
    private Vector2 eye1Start;
    private Vector2 eye1End;
    private Vector2 eye2Start;
    private Vector2 eye2End;
    private float iFramesTimer;

    public ZuckPhase2(ZuckTank zuck) {
        this.zuck = zuck;
        this.complete = false;
        this.target = new Vector2();
        this.targetA = new Vector2(zuck.position.x, zuck.position.y - zuck.imageBounds.height / 2f);
        this.targetB = new Vector2(zuck.position.x - 500f, zuck.position.y);
        this.targetC = new Vector2(zuck.position.x, zuck.position.y + zuck.imageBounds.height + zuck.imageBounds.height / 2f);
        this.eye1Start = new Vector2();
        this.eye1End   = new Vector2();
        this.eye2Start = new Vector2();
        this.eye2End   = new Vector2();
        this.iFramesTimer = 0f;

        zuck.animation = zuck.animations.shoot;
        zuck.stateTime = 0f;

        this.blastin = false;
        this.target.set(targetA);
        Timeline.createSequence()
                .pushPause(1f)
                .push(Tween.call((type, source) -> blastin = true))
                .push(Tween.to(target, Vector2Accessor.XY, 2f).target(targetB.x, targetB.y).ease(Quint.INOUT))
                .push(Tween.to(target, Vector2Accessor.XY, 2f).target(targetC.x, targetC.y).ease(Quint.INOUT))
                .push(Tween.call((type, source) -> blastin = false))
                .pushPause(2f)
                .push(Tween.call((type, source) -> complete = true))
                .start(zuck.screen.game.tween);
    }

    @Override
    public void update(float dt) {
        iFramesTimer -= dt;
        if (iFramesTimer < 0f) {
            iFramesTimer = 0f;
        }
        if (iFramesTimer == 0f && blastin) {
            Player player = zuck.screen.player;
            eye1Start.set(zuck.position.x - 28, zuck.position.y + 43);
            eye2Start.set(zuck.position.x - 63, zuck.position.y + 33);
            eye1End.set(target.x, target.y);
            eye2End.set(target.x, target.y);
            boolean hitPlayer1 = Intersector.intersectSegmentRectangle(eye1Start, eye1End, player.collisionBounds);
            boolean hitPlayer2 = Intersector.intersectSegmentRectangle(eye2Start, eye2End, player.collisionBounds);
            if (hitPlayer1 || hitPlayer2) {
                player.hitPoints -= 1;
                zuck.screen.particles.interact(player.position.x, player.position.y);
                iFramesTimer = 2f;
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!blastin) return;
        // NOTE: this is trash garbage and might cause problems
        float x1a = zuck.position.x - 28;
        float y1a = zuck.position.y + 43;
        float x1b = zuck.position.x - 63;
        float y1b = zuck.position.y + 33;
        float x2 = target.x;
        float y2 = target.y;
        float width = 3f;
        batch.end();
        zuck.assets.shapes.setProjectionMatrix(zuck.screen.getWorldCamera().combined);
        zuck.assets.shapes.begin(ShapeRenderer.ShapeType.Filled);
        zuck.assets.shapes.setColor(Color.RED);
        zuck.assets.shapes.rectLine(x1a, y1a, x2, y2, width);
        zuck.assets.shapes.rectLine(x1b, y1b, x2, y2, width);
        zuck.assets.shapes.setColor(Color.WHITE);
        zuck.assets.shapes.end();
        batch.begin();
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public BossPhase nextPhase() {
        return new ZuckPhase3(zuck);
    }

}
