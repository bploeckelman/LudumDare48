package lando.systems.ld48.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import lando.systems.ld48.Config;
import lando.systems.ld48.Game;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
		@Override
		public GwtApplicationConfiguration getConfig () {
			// Resizable application, uses available space in browser
//			return new GwtApplicationConfiguration(true);
			// Fixed size application:
			return new GwtApplicationConfiguration(Config.windowWidth, Config.windowHeight);
		}

		@Override
		public ApplicationListener createApplicationListener () { 
			return new Game();
		}
}
