/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.marteEngine.entity;

import it.marteEngine.ME;
import java.util.List;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;

/**
 *
 * @author Kristoffer
 */
public abstract class MovableEntity extends Entity {

    public MovableEntity( float x, float y ) {
        super( x, y );
    }

    public MovableEntity( float x, float y, Image image ) {
        super( x, y, image );
    }

    public MovableEntity( float x, float y, int width, int height ) {
        super( x, y, width, height );
    }


    @Override
    public Entity collide( String type, float x, float y ) {
        Rectangle hitbox = new Rectangle( x + hitboxOffsetX, y + hitboxOffsetY, hitboxWidth, hitboxHeight );
        Entity collide = super.collide( type, x, y );
        if( collide != null ) {
            return collide;
        }

        if( type.equals( SOLID ) ) {
            return ME.world.getMap().getCollision( hitbox );
        }

        return null;
    }
    @Override
    public List<Entity> collideInto( String type, float x, float y ) {
        Rectangle hitbox = new Rectangle( x + hitboxOffsetX, y + hitboxOffsetY, hitboxWidth, hitboxHeight );
        List entities = super.collideInto( type, x, y );

        Entity e = ME.world.getMap().getCollision( hitbox );
        if( e != null ) {
            entities.add( e );
        }

        return entities;
    }

}
