package it.marteEngine.test.fuzzy;

import it.marteEngine.ME;
import it.marteEngine.ResourceManager;
import it.marteEngine.entity.PlatformerEntity;

import java.io.IOException;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.particles.ConfigurableEmitter;
import org.newdawn.slick.particles.ParticleIO;
import org.newdawn.slick.particles.ParticleSystem;
import org.newdawn.slick.util.Log;

public class FuzzyPlayer extends PlatformerEntity {

	private boolean faceRight = true;
	private Sound jumpSnd;
	private ParticleSystem jumpEffect;
	private ConfigurableEmitter  emitter;
	
	public static int life = 3;
	
	public FuzzyPlayer(float x, float y, String ref)
			throws SlickException {
		super(x, y, 22, 30);
		addAnimation(ResourceManager.getSpriteSheet("left"), "left", true, 0, 0, 1, 2, 3);
		addAnimation(ResourceManager.getSpriteSheet("right"), "right", true, 0, 0, 1, 2, 3);
		
		addType(PLAYER);
		name = PLAYER;
		jumpSnd = ResourceManager.getSound("jump");
		maxSpeed = new Vector2f(3,8);
		
		try {
			jumpEffect = ParticleIO.loadConfiguredSystem("data/fuzzy/jumpEmit.xml");
			emitter = (ConfigurableEmitter) jumpEffect.getEmitter(0);
		} catch (IOException e) {
			Log.error("Error on loading system for jump emitter");
			e.printStackTrace();
		}
	}
	
	@Override
	public void render(GameContainer container, Graphics g)
			throws SlickException {
		currentAnim = faceRight ? "right" : "left";
		
		super.render(container, g);

		if (jumpEffect!=null){
			jumpEffect.render();
		}
	}
	
	@Override
	public void update(GameContainer container, int delta)
			throws SlickException {
		super.update(container, delta);
		
		if (jumpEffect!=null){
			jumpEffect.update(delta);
			emitter.update(jumpEffect, delta);
		}
		
		if (check(CMD_LEFT)) {
			faceRight = false;
		}
		if (check(CMD_RIGHT)) {
			faceRight = true;
		}
		
		if (speed.y < 0 && !jumpSnd.playing() && onGround){
			jumpSnd.play();
		}
		
		if(collide(Spike.SPIKE, x, y)!=null){
			if (life > 0){
				life -=1;
				jump();
			} else {
				removePlayer();
			}
		}
	}
	
	@Override
	public void leftWorldBoundaries() {
		if (y > 0){
			removePlayer();
		}
	}

	private void removePlayer() {
		ME.world.remove(this);
		FuzzyGameWorld.playerDead = true;
	}

}
