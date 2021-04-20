package lando.systems.ld48.screens;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld48.Config;
import lando.systems.ld48.Game;

public abstract class BaseScreen implements InputProcessor, ControllerListener {

    Game game;
    OrthographicCamera worldCamera;
    OrthographicCamera windowCamera;

    public BaseScreen(Game game) {
        this.game = game;
        this.worldCamera = new OrthographicCamera();
        this.worldCamera.setToOrtho(false, Config.worldWidth, Config.worldHeight);
        this.worldCamera.update();
        this.windowCamera = new OrthographicCamera();
        this.windowCamera.setToOrtho(false, Config.windowWidth, Config.windowHeight);
        this.windowCamera.update();
    }

    /**
     * Always called during game update loop, regardless of whether system is paused.
     */
    public void alwaysUpdate(float dt) {};

    /**
     * Normal update, respects Time.pause_for()
     */
    public abstract void update(float dt);

    /**
     * Normal render
     */
    public abstract void render(SpriteBatch batch);

    // ------------------------------------------------------------------------
    // InputProcessor interface
    // ------------------------------------------------------------------------

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    // ------------------------------------------------------------------------
    // ControllerListener interface
    // ------------------------------------------------------------------------

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return false;
    }

}
