package it.marteEngine.test.tiled;

import it.marteEngine.ResourceManager;
import it.marteEngine.entity.Entity;
import it.marteEngine.tween.LinearMotion;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

public class Player extends Entity {

	private LinearMotion motion;

	public int currentEase = 0;

	public Player(float x, float y) {
		super(x, y);
		setGraphic(ResourceManager.getImage("image"));
		bindToMouse("MOVE", Input.MOUSE_LEFT_BUTTON);
		bindToMouse("CHANGE_MODE", Input.MOUSE_RIGHT_BUTTON);
		define("START", Input.KEY_Z);
		define("PAUSE", Input.KEY_X);
		define("RESET", Input.KEY_C);

		motion = null;
	}

	@Override
	public void update(GameContainer container, int delta)
			throws SlickException {
		super.update(container, delta);
		if (motion != null)
			motion.update(delta);

		Input input = container.getInput();
		// change tween's ease
		if (pressed("CHANGE_MODE")) {
			if (currentEase + 1 <= 26) {
				currentEase++;
			} else {
				currentEase = 0;
			}
		}
		// check controls
		if (check("MOVE")) {
			motion = new LinearMotion(x, y, input.getMouseX(),
					input.getMouseY(), 100, currentEase);
		}
		if (pressed("START")) {
			motion.start();
		}
		if (pressed("PAUSE")) {
			motion.pause();
		}
		if (pressed("RESET")) {
			motion.reset();
		}

		// update player position according to tween
		if (motion != null)
			setPosition(motion.getPosition());
	}
}
