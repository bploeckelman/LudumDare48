package lando.systems.ld48.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import lando.systems.ld48.Assets;
import lando.systems.ld48.Game;
import lando.systems.ld48.ui.typinglabel.TypingLabel;

public class LaunchScreen extends BaseScreen {


    private final String text = "{JUMP=.2}{GRADIENT=white;dark_gray}Touch to start{ENDGRADIENT}{ENDJUMP}";
    private final Assets assets;
    private final TypingLabel label;

    public LaunchScreen(Game game) {
        super(game);
        assets = game.assets;
        label = new TypingLabel(assets.pixelFont16, text, 0f, windowCamera.viewportHeight / 2f + 50f);
        label.setWidth(windowCamera.viewportWidth);
        label.setFontScale(2f);
    }

    private boolean hasSetScreen = false;

    @Override
    public void update(float dt) {
        label.update(dt);
        if (Gdx.input.justTouched() && !hasSetScreen) {
            game.setScreen(new TitleScreen(game));
            hasSetScreen = true;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            // fill background
            batch.setColor(Color.BLACK);
            batch.draw(assets.whitePixel, 0, 0, windowCamera.viewportWidth, windowCamera.viewportHeight);
            batch.setColor(Color.WHITE);

            // draw text
            label.render(batch);

            // draw cheeky footnote
            BitmapFont font = assets.pixelFont16;
            GlyphLayout layout = assets.layout;
            float prevScaleX = font.getData().scaleX;
            float prevScaleY = font.getData().scaleY;
            font.getData().setScale(0.3f);
            layout.setText(font, "*chrome needs this extra screen so the audio works, thanks obama", Color.DARK_GRAY, windowCamera.viewportWidth, Align.center, true);
            font.draw(batch, layout, 0f, 15f);
            font.getData().setScale(prevScaleX, prevScaleY);
        }
        batch.end();
    }

}
