package it.marteEngine;

import it.marteEngine.entity.Entity;
import it.marteEngine.entity.SolidEntity;
import java.util.HashSet;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.tiled.TiledMap;

public class Map extends TiledMap {

    /*
     * Special Layers
     */
    public final static String COLLISION = "Collision";

    /*
     * Layer Properties
     */
    public final static String RENDER = "render";

    private HashSet<Integer> dontRender;
    private int collisionIndex = -1;

    private static SolidEntity collisionEntity;

    public Map( String ref ) throws SlickException {
        super( ref );
        dontRender = new HashSet<>();
        setNotToBeRendered();
        collisionEntity = new SolidEntity( 0, 0, getTileWidth(), getTileHeight() );

    }

    private void setNotToBeRendered() {
        collisionIndex = getLayerIndex( COLLISION );
        for( int layerIndex = 0; layerIndex < getLayerCount(); layerIndex++ ) {
            if( layerIndex == collisionIndex
                || Boolean.parseBoolean( getLayerProperty( layerIndex, RENDER, "true" ) ) ) {
                dontRender.add( layerIndex );
            }
        }
    }

    public boolean isToBeRendered( int layerIndex ) {
        if( dontRender.contains( layerIndex ) ) {
            return false;
        }

        return true;
    }

    public synchronized Entity getCollision( Rectangle rect ) {
        return getCollision( rect.getX(), rect.getY(), (int)rect.getWidth(), (int)rect.getHeight() );
    }

    public synchronized Entity getCollision( float x, float y, int width, int height ) {
        if( collisionIndex == -1 ){
            return null;
        }

        int lowX = (int)Math.max( 0, Math.floor( x / tileWidth ) );
        int highX = (int)Math.min( this.width - 1, Math.floor( ( x + width ) / tileWidth ) );
        int lowY = (int)Math.max( 0, Math.floor( y / tileHeight ) );
        int highY = (int)Math.min( this.height - 1, Math.floor( ( y + height ) / tileHeight ) );

        for( int i = lowX; i <= highX; i++ ) {
            for( int j = lowY; j <= highY; j++ ) {
                if( getTileId( i, j, collisionIndex ) > 0 ) {
                    collisionEntity.setPosition( new Vector2f( i * tileWidth, j * tileHeight ) );
                    return collisionEntity;
                }
            }
        }

        return null;
    }
}
