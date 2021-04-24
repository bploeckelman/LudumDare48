package lando.systems.ld48.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld48.Game;
import lando.systems.ld48.levels.Level;
import lando.systems.ld48.levels.LevelDescriptor;
import lando.systems.ld48.physics.PhysicsComponent;
import lando.systems.ld48.physics.PhysicsSystem;

public class GameScreen extends BaseScreen {

    public Level level;
    public PhysicsSystem physicsSystem;
    public Array<PhysicsComponent> physicsEntities;

    public GameScreen(Game game) {
        super(game);
        loadLevel(LevelDescriptor.test);
    }

    private void loadLevel(LevelDescriptor levelDescriptor) {
        this.level = new Level(levelDescriptor, this);
        this.physicsSystem = new PhysicsSystem(this);
        this.physicsEntities = new Array<>();
    }

    @Override
    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) DebugFlags.renderFpsDebug   = !DebugFlags.renderFpsDebug;
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) DebugFlags.renderLevelDebug = !DebugFlags.renderLevelDebug;
    }

    @Override
    public void render(SpriteBatch batch) {
        // draw world stuff
        batch.setProjectionMatrix(worldCamera.combined);
        {
            batch.begin();
            {
                // draw distant background
            }
            batch.end();

            level.render(Level.LayerType.background, worldCamera);
            level.render(Level.LayerType.collision, worldCamera);

            batch.begin();
            {
//                player.render(batch);
                level.renderObjects(batch);
            }
            batch.end();

            level.render(Level.LayerType.foreground, worldCamera);

            batch.begin();
            {
                // draw foreground entity decorations and such
            }
            batch.end();

            if (DebugFlags.renderLevelDebug) {
                batch.begin();
                {
                    level.renderDebug(batch);
                }
                batch.end();
            }
        }

        // draw window space stuff
        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            if (DebugFlags.renderFpsDebug) {
                game.assets.pixelFont16.draw(batch, " fps: " + Gdx.graphics.getFramesPerSecond(), 10f, windowCamera.viewportHeight - 10f);
            }
            // draw overlay ui stuff
        }
        batch.end();
    }

    // ------------------------------------------------------------------------
    // Implementation stuff
    // ------------------------------------------------------------------------

    static class DebugFlags {
        public static boolean renderFpsDebug = true;
        public static boolean renderLevelDebug = false;
    }

}
