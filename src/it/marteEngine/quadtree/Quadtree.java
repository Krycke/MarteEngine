package it.marteEngine.quadtree;

import it.marteEngine.entity.Entity;
import java.util.ArrayList;
import java.util.List;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

/**
 *
 * @author Kristoffer
 */
public class Quadtree {

    private Quad headNode;
    private int maxEntities = 4;
    protected static int minWidth;
    protected static int minHeight;

    public Quadtree( int width, int height ) {
        headNode = new Quad( 0, 0, width, height, maxEntities );
        Quadtree.minWidth = 16;
        Quadtree.minHeight = 16;
    }

    public Quadtree( int width, int height, int minWidth, int minHeight ) {
        headNode = new Quad( 0, 0, width, height, maxEntities );
        Quadtree.minWidth = minWidth;
        Quadtree.minHeight = minHeight;
    }

    public Rectangle worldRect() {
        return headNode.getBounds();
    }

    public void insert( Entity entity ) {
        // check if the world needs resizing
        if( !headNode.containsRect( entity.getBounds() ) ) {
            System.out.println( "Error 1: HeadNode doesn't contain item" );
        }
        else {
            headNode.insert( entity );
        }
    }

    public List<Entity> getEntities( Vector2f point ) {
        List<Entity> entityList = new ArrayList<>();
        headNode.getEntity( point, entityList );
        return entityList;
    }

    public List<Entity> getEntities( Rectangle rect ) {
        List<Entity> entityList = new ArrayList<>();
        headNode.getEntity( rect, entityList );
        return entityList;
    }

    public List<Entity> getAllEntities() {
        List<Entity> entityList = new ArrayList<>();
        headNode.getAllEntities( entityList );
        return entityList;
    }

    public void move( Entity entity ) {
        Quad quad = entity.getQuad();
        if( quad == null ) {
            quad = findNode( entity );
        }
        if( quad != null ) {
            quad.entityMove( entity );
        }
    }

    @Override
    public String toString() {
        return "QuadTree: \n" + headNode.toString();
    }

    public Quad findNode( Entity entity ) {
        return headNode.findEntityNode( entity );
    }

    public void resize( int width, int height ) {
        List<Entity> entitites = getAllEntities();
        destroy();
        headNode = new Quad( 0, 0, width, height, maxEntities );
        for( Entity entity : entitites ) {
            headNode.insert( entity );
        }
    }

    public void remove( Entity entity ) {

        Quad node = entity.getQuad();
        if( node == null ) {
            node = findNode( entity );
        }
        if( node != null ) {
            node.removeEntity( entity );
        }
    }

    public void destroy() {
        headNode.destroy();
    }

    public void render( Graphics g ) {
        Color prevColor = g.getColor();
        g.setColor( Color.blue );
        headNode.render( g );
        g.setColor( prevColor );
    }
    /**
     * Finds and returns a list of neighbouring Entities. The list will be sorted from closest
     * to fartherst away, in the rectangle width, height, with QuadItem in the center
     *
     * @param e
     * param width
     * param height
     *
     * @return Returns a sorted list of Entities
     */
//    public List<QuadItem> findNeighbours(QuadItem e, int width, int height){
//    }
}
