package it.randomtower.engine;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

public class StaticActor extends Entity {

    public static final String NAME = "STATIC_ACTOR";

    public StaticActor(float x, float y, int width, int height, String image) {
	super(x, y);

	// set id
	name = NAME;

	// define collision box and type
	setHitBox(0, 0, width, height);
	addType(NAME, ME.SOLID);
	
	// set image
	setupGraphic(image);
    }
    
    public StaticActor(float x, float y, int width, int height, String ref, int row, int frame ) {
	this(x,y,width,height,null);
	
	setupAnimations(ref,row,frame);
    }

    
    private void setupGraphic(String ref) {
	if (ref==null) return;
	try {
	    setGraphic(new Image(ref));
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    private void setupAnimations(String ref, int row, int frame) {
	try {
	    setGraphic(new SpriteSheet(ref, width, height));
	    add(NAME, false, row, frame);
	} catch (SlickException e) {
	    e.printStackTrace();
	}
    }
    

}