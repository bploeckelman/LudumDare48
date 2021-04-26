package lando.systems.ld48.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import lando.systems.ld48.Assets;

public class Modal {
    public static float SIDE_MARGIN = 50;
    public static float DELAY_TO_TOUCH = 1f;
    private String text;
    private boolean completed;

    Rectangle bounds;
    private float targetInterval;
    private float currentInterval;
    Interpolation interpolation = Interpolation.swingOut;

    InputProcessor prevProcessor;
    float targetWidth;
    float targetHeight;
    float centerX;
    float centerY;
    float currentScale;
    float currentMargin;
    OrthographicCamera hudCamera;
    GlyphLayout layout;
    float accum;
    Assets assets;
    BitmapFont font;

    public Modal (Assets assets, String text, OrthographicCamera hudCamera) {
        this.font = assets.pixelFont16;
        this.assets = assets;
        layout = assets.layout;
        this.text = text;
        prevProcessor = Gdx.input.getInputProcessor();
//        Gdx.input.setInputProcessor(null); // Because we store state they can get stuck
        targetInterval = 1f;
        currentInterval = 0;
        this.hudCamera = hudCamera;
        centerX = hudCamera.viewportWidth/2f;
        centerY = hudCamera.viewportHeight/2f;
        targetWidth = hudCamera.viewportWidth*.7f;
        bounds = new Rectangle(centerX, centerY, 0, 0);
        font.getData().setScale(1f);
        layout.setText(font, text, Color.BLACK, targetWidth - SIDE_MARGIN*2f, Align.center, true);
        targetHeight = layout.height + SIDE_MARGIN*2f;
        currentScale = 0f;
    }

    public void update(float dt) {
        accum += dt;
        if (targetInterval == 0 && currentInterval == 0){
            completed = true;
            Gdx.input.setInputProcessor(prevProcessor);
        }

        if (targetInterval == 1 && currentInterval == 1 && accum > DELAY_TO_TOUCH ) {
            if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
                targetInterval = 0;
            }
        }

        if (currentInterval < targetInterval){
            currentInterval += dt * 2f;
        } else if (currentInterval > targetInterval) {
            currentInterval -= dt * 3f;
        }
        currentInterval = MathUtils.clamp(currentInterval, 0f,1f);
        currentScale = interpolation.apply(currentInterval);


        currentMargin = currentScale * SIDE_MARGIN;
        bounds.width = targetWidth * currentScale;
        bounds.height = targetHeight * currentScale;
        bounds.x = centerX - bounds.width/2f;
        bounds.y = centerY - bounds.height/2f;
    }

    Color textColor = new Color();
    public void render(SpriteBatch batch) {
        float alpha = currentInterval;
        textColor.set(.2f, .2f, .2f, alpha);
        batch.setColor(0,0,0,.6f * alpha);
        batch.draw(assets.whitePixel, 0, 0, hudCamera.viewportWidth, hudCamera.viewportHeight);
        batch.setColor(1f, 1f, 1f, alpha);
        assets.roundedBoxNinePatch.draw(batch, bounds.x, bounds.y, bounds.width, bounds.height);
        if (bounds.width > SIDE_MARGIN) {
            font.getData().setScale(currentScale);
            layout.setText(font, text, textColor, bounds.width - currentMargin, Align.center, true);
            font.draw(batch, layout, bounds.x + currentMargin, bounds.y + bounds.height - currentMargin);
        }

        font.getData().setScale(1f);
    }

    public boolean isComplete(){
        return completed;
    }
}
