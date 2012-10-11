/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.marteEngine.quadtree;

import it.marteEngine.entity.Entity;
import java.util.*;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.*;

/**
 *
 * @author Kristoffer
 */
public class Quad {

    private Rectangle rect;
    private Quad NW;
    private Quad NE;
    private Quad SW;
    private Quad SE;
    private Quad parentQuad;
    private List<Entity> entities;
    private boolean isPartitioned = false;
    private int maxEntities;

    public Quad( Rectangle size, Quad parent, int maxEntites ) {
        entities = new ArrayList<>();
        rect = size;
        parentQuad = parent;
        this.maxEntities = maxEntites;
    }

    public Quad( float x, float y, int width, int height, int maxEntities ) {
        this( new Rectangle( x, y, width, height ), null, maxEntities );
    }

    public Rectangle getBounds() {
        return rect;
    }

    protected void insert( Entity entity ) {
        if( !containsRect( entity.getBounds() ) ) {
            if( parentQuad != null ) {
                parentQuad.insert( entity );
            }
            else {
                System.out.println( "Error 3: HeadNode doesn't contain item" );
            }
            return;
        }

        // If partitioned, try to find child node to add to
        if( !insertInChild( entity ) ) {
            if( !entities.contains( entity ) ) {
                entities.add( entity );
                entity.setQuad( this );
            }

            // Check if this node needs to be partitioned
            if( !isPartitioned && entities.size() >= maxEntities ) {
                partition();
            }
        }
    }

    protected boolean insertInChild( Entity entity ) {

        if( !isPartitioned ) {
            return false;
        }
        if( NW.containsRect( entity.getBounds() ) ) {
            NW.insert( entity );
        }
        else if( NE.containsRect( entity.getBounds() ) ) {
            NE.insert( entity );
        }
        else if( SW.containsRect( entity.getBounds() ) ) {
            SW.insert( entity );
        }
        else if( SE.containsRect( entity.getBounds() ) ) {
            SE.insert( entity );
        }
        else {
            return false; // insert in child failed
        }
        return true;
    }

    public boolean pushEntityDown( Entity entity ) {
        if( insertInChild( entity ) ) {
            removeEntity( entity );
            return true;
        }
        else {
            return false;
        }
    }

    public boolean pushEntityDown( int i ) {
        Entity entity = entities.get( i );
        if( insertInChild( entity ) ) {
            removeEntity( entity );
            return true;
        }
        else {
            return false;
        }
    }

    public void pushEntityUp( Entity entity ) {

        removeEntity( entity );
        parentQuad.insert( entity );
    }

    protected void partition() {
        // Create the nodes
        Vector2f halfSize = new Vector2f( rect.getWidth() / 2, rect.getHeight() / 2 );

        if( halfSize.getX() >= Quadtree.minWidth && halfSize.getY() >= Quadtree.minHeight ) {

            NW = new Quad( new Rectangle( rect.getX(),
                                          rect.getY(),
                                          halfSize.getX(), halfSize.getY() ), this, maxEntities );

            NE = new Quad( new Rectangle( rect.getX() + halfSize.getX(),
                                          rect.getY(),
                                          halfSize.getX(), halfSize.getY() ), this, maxEntities );

            SW = new Quad( new Rectangle( rect.getX(),
                                          rect.getY() + halfSize.getY(),
                                          halfSize.getX(), halfSize.getY() ), this, maxEntities );

            SE = new Quad( new Rectangle( rect.getX() + halfSize.getX(),
                                          rect.getY() + halfSize.getY(),
                                          halfSize.getX(), halfSize.getY() ), this, maxEntities );

            isPartitioned = true;

            // Try to push items down to child nodes
            int i = 0;
            while( i < entities.size() ) {
                if( !pushEntityDown( i ) ) {
                    i++;
                }
            }
        }
    }
//
//    protected boolean unpartition() {
//        if( isPartitioned
//            && NW.unpartition()
//            && NE.unpartition()
//            && SW.unpartition()
//            && SE.unpartition() ) {
//            NW = null;
//            NE = null;
//            SW = null;
//            SE = null;
//            isPartitioned = false;
//        }
//        if( !isPartitioned && entities.isEmpty() ) {
//            return true;
//        }
//
//        return false;
//    }

    protected void getEntity( Vector2f point, List<Entity> itemsFound ) {
        // test the point against this node
        if( containsPoint( point ) ) {
            // test the point in each item
            for( Entity entity : entities ) {
                if( entity.containsPoint( point ) ) {
                    itemsFound.add( entity );
                }
            }

            // query all subtrees
            if( isPartitioned ) {
                NW.getEntity( point, itemsFound );
                NE.getEntity( point, itemsFound );
                SW.getEntity( point, itemsFound );
                SE.getEntity( point, itemsFound );
            }
        }
    }

    protected void getEntity( Rectangle trect, List<Entity> itemsFound ) {
        // test the point against this node
        if( trect.intersects( rect ) ) {
            // test the point in each item
            for( Entity entity : entities ) {
                if( entity.getBounds().intersects( trect ) ) {
                    itemsFound.add( entity );
                }
            }

            // query all subtrees
            if( isPartitioned ) {
                NW.getEntity( trect, itemsFound );
                NE.getEntity( trect, itemsFound );
                SW.getEntity( trect, itemsFound );
                SE.getEntity( trect, itemsFound );
            }
        }
    }

    protected void getAllEntities( List<Entity> itemsFound ) {
        itemsFound.addAll( entities );

        // query all subtrees
        if( isPartitioned ) {
            NW.getAllEntities( itemsFound );
            NE.getAllEntities( itemsFound );
            SW.getAllEntities( itemsFound );
            SE.getAllEntities( itemsFound );
        }
    }

    protected void getAllEntitiesWithType( String type, List<Entity> itemsFound ) {
        for( Entity entity : entities ) {
            if( entity.isType( type ) ) {
                itemsFound.add( entity );
            }

        }

        // query all subtrees
        if( isPartitioned ) {
            NW.getAllEntitiesWithType( type, itemsFound );
            NE.getAllEntitiesWithType( type, itemsFound );
            SW.getAllEntitiesWithType( type, itemsFound );
            SE.getAllEntitiesWithType( type, itemsFound );
        }
    }

    protected Quad findEntityNode( Entity entity ) {
        if( entities.contains( entity ) ) {
            return this;
        }
        else if( isPartitioned ) {
            Quad n = null;

            // Check the nodes that could contain the item
            if( NW.containsRect( entity.getBounds() ) ) {
                n = NW.findEntityNode( entity );
            }
            if( n == null
                && NE.containsRect( entity.getBounds() ) ) {
                n = NE.findEntityNode( entity );
            }
            if( n == null
                && SW.containsRect( entity.getBounds() ) ) {
                n = SW.findEntityNode( entity );
            }
            if( n == null
                && NW.containsRect( entity.getBounds() ) ) {
                n = NW.findEntityNode( entity );
            }

            return n;
        }
        else {
            return null;
        }
    }

    protected void destroy() {
        // Destroy all child nodes
        if( isPartitioned ) {
            NW.destroy();
            NE.destroy();
            SW.destroy();
            SE.destroy();

            NW = null;
            NE = null;
            SW = null;
            SE = null;

            isPartitioned = false;
        }

        entities.clear();
    }

    protected void removeEntity( Entity entity ) {
        // Find and remove the item
        if( entities.contains( entity ) ) {
            entities.remove( entity );
        }
//        unpartition();
    }

    protected void removeEntity( int i ) {
        if( i < entities.size() ) {
            entities.remove( i );
        }
//        unpartition();
    }

    protected void entityMove( Entity entity ) {
        // Find the item
        if( entities.contains( entity ) ) {

            // Try to push the item down to the child
            if( !pushEntityDown( entity ) ) {
                // otherwise, if not root, push up
                if( !containsRect( entity.getBounds() ) && parentQuad != null ) {
                    pushEntityUp( entity );
                }
                else if( !containsRect( entity.getBounds() ) ) {
                    System.out.println( "Error 2: HeadNode doesn't contain item" );
                }
            }
        }
    }

    protected void render( Graphics g ) {
        g.draw( rect );
        if( isPartitioned ) {
            NW.render( g );
            NE.render( g );
            SW.render( g );
            SE.render( g );
        }
    }

    protected boolean containsRect( Rectangle entityRect ) {
        return (rect.getX() <= entityRect.getX()
                && rect.getMaxX() >= entityRect.getMaxX()
                && rect.getY() <= entityRect.getY()
                && rect.getMaxY() >= entityRect.getMaxY());
    }

    protected boolean containsPoint( Vector2f point ) {
        return (rect.getX() <= point.getX()
                && rect.getMaxX() >= point.getX()
                && rect.getY() <= point.getY()
                && rect.getMaxY() >= point.getY());
    }

    @Override
    public String toString() {
        String str = "{";
        for( Entity entity : entities ) {
            str += entity.name + ", ";
        }

        if( isPartitioned ) {
            str += "\n";
            str += addTabs() + " NW : " + NW.toString() + "\n";
            str += addTabs() + " NE : " + NE.toString() + "\n";
            str += addTabs() + " SW : " + SW.toString() + "\n";
            str += addTabs() + " SE : " + SE.toString() + "\n";
            str += addTabs();
        }
        str += "}";

        return str;
    }

    private String addTabs() {
        String str = "";
        for( int i = 0; i < Math.log( 1024 ) - Math.log( rect.getWidth() ); i++ ) {
            str += "\t";
        }
        return str;
    }
}