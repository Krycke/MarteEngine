package it.marteEngine.entity;

import it.marteEngine.ResourceManager;
import org.newdawn.slick.*;

public class SolidEntity extends Entity {

	public SolidEntity(float x, float y, int width, int height) throws SlickException {
		super(x, y);
		addType(SOLID);
		name = SOLID;
		setHitBox(0, 0, width, height);
	}

	public SolidEntity(float x, float y, int width, int height, int depth)
			throws SlickException {
		this(x, y, width, height);
        this.depth = depth;
	}

	public SolidEntity(float x, float y, int width, int height, int depth,
			String refImage) throws SlickException {
		this(x, y, width, height, depth);

		setupGraphic( refImage );
	}

	public SolidEntity(float x, float y, int width, int height, int depth, Image image)
			throws SlickException {
		this(x, y, width, height, depth);
		currentImage = image;
	}

	public SolidEntity(float x, float y, int width, int height, int depth, String ref,
                        int row, int frame)
			throws SlickException {
		this(x, y, width, height, depth);
		setupAnimations( ref, row, frame );
	}

    private void setupGraphic( String ref ) {
        if( ref == null || ref.length() == 0 ) {
            return;
        }
        try {
            Image image;
            if( ResourceManager.hasImage( ref ) ) {
                image = ResourceManager.getImage( ref );
            }
            else {
                image = new Image( ref );
            }
            currentImage = image;
        }
        catch( Exception e ) {
        }
    }

    private void setupAnimations( String ref, int row, int frame ) {
        try {
            setGraphic( new SpriteSheet( ref, getWidth(), getHeight() ) );
            addAnimation( SOLID, false, row, frame );
        }
        catch( SlickException e ) {
        }
    }

}
