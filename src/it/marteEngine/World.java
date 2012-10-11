package it.marteEngine;

import it.marteEngine.entity.*;
import it.marteEngine.quadtree.Quadtree;
import java.util.*;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.state.*;
import org.newdawn.slick.util.Log;

//TODO addAll() muss intern add() aufrufen, um korrekt nach flags in die listen einzusortieren
public class World extends BasicGameState {

    public static final int BELOW = -1;
    public static final int GAME = 0;
    public static final int ABOVE = 1;
    /** the game container this world belongs to */
    public GameContainer container = null;
    /** unique id for every world * */
    public int id = 0;
    /** width of the world, useful for horizontal wrapping entities */
    public int width = 0;
    /** height of the world, useful for vertical wrapping entities */
    public int height = 0;
    /** the map of the world */
    private Map map = null;
    /** internal list for entities */
    private Quadtree quadtree;
    private List<Entity> removable = new ArrayList<>();
    private List<Entity> addable = new ArrayList<>();
    /**
     * two lists to contain objects that are rendered before and after camera
     * stuff is rendered
     */
    private List<Entity> belowCamera = new ArrayList<>();
    private List<Entity> aboveCamera = new ArrayList<>();
    /** current camera * */
    public Camera camera;
    public int renderedEntities;
    /** available commands for world * */
    protected InputManager input;

    public World( int id ) {
        this.id = id;
        quadtree = new Quadtree( width, height );
    }

    public World( int id, GameContainer container ) {
        this( id );
        this.container = container;
    }

    @Override
    public void init( GameContainer container, StateBasedGame game )
            throws SlickException {
        this.container = container;
        input = new InputManager( container.getInput() );

        if( width == 0 ) {
            width = container.getWidth();
        }
        if( height == 0 ) {
            height = container.getHeight();
        }
        camera = new Camera( width, height );
    }

    @Override
    public void enter( GameContainer container, StateBasedGame game )
            throws SlickException {
        ME.world = this;
    }

    @Override
    public void render( GameContainer container, StateBasedGame game, Graphics g )
            throws SlickException {

        renderedEntities = 0;
        // first render entities below camera
        for( Entity e : belowCamera ) {
            if( !e.visible ) {
                continue;
            }
            renderEntity( e, g, container );
        }
        g.translate( -(camera.getX() - camera.getCameraOffset().getX()), -(camera.getY() - camera.getCameraOffset().getY()) );

        if( map != null ) {
            renderMap( g, container );
        }

        // render entities
        for( Entity e : quadtree.getEntities( camera.getVisibleRect() ) ) {
            if( !e.visible && !ME.debugEnabled ) {
                continue; // next entity. this one stays invisible
            }
            renderEntity( e, g, container );
        }

        // render particle system
        if( ME.ps != null && ME.renderParticle ) {
            ME.ps.render();
        }

        if( ME.debugEnabled ) {
            g.draw( camera.getDeadzone() );
            g.draw( camera.getVisibleRect() );
            quadtree.render( g );
        }

        g.translate( camera.getX() - camera.getCameraOffset().getX(), camera.getY() - camera.getCameraOffset().getY() );

        // finally render entities above camera
        for( Entity e : aboveCamera ) {
            if( !e.visible ) {
                continue;
            }
            renderEntity( e, g, container );
        }

        ME.render( container, game, g );
    }

    private void renderEntity( Entity e, Graphics g, GameContainer container )
            throws SlickException {
        renderedEntities++;
        e.render( container, g );
    }

    private void renderMap( Graphics g, GameContainer container )
            throws SlickException {
        for( int layerIndex = 0; layerIndex < map.getLayerCount(); layerIndex++ ) {
            if( (ME.debugEnabled || map.isToBeRendered( layerIndex )) ) {
                map.render( (int)camera.getCameraOffset().getX(),
                            (int)camera.getCameraOffset().getY(),
                            (int)camera.getX() / map.getTileWidth(),
                            (int)camera.getY() / map.getTileHeight(),
                            camera.getWidth() / map.getTileWidth(),
                            camera.getHeight() / map.getTileHeight(),
                            layerIndex,
                            false );
            }
        }
    }

    @Override
    public void update( GameContainer container, StateBasedGame game, int delta )
            throws SlickException {
        if( container == null ) {
            throw new SlickException( "no container set" );
        }

        // store the current delta in ME for anyone who's interested in it.
        ME.delta = delta;

        // add new entities
        if( addable.size() > 0 ) {
            for( Entity entity : addable ) {
                quadtree.insert( entity );
                entity.addedToWorld();
            }
            addable.clear();
        }

        // update entities
        for( Entity e : belowCamera ) {
            e.updateAlarms( delta );
            if( e.active ) {
                e.update( container, delta );
            }
        }
        for( Entity e : quadtree.getAllEntities() ) {
            e.updateAlarms( delta );
            if( e.active ) {
                e.update( container, delta );
            }
            // check for wrapping or out of world entities
            e.checkWorldBoundaries();
        }
        for( Entity e : aboveCamera ) {
            e.updateAlarms( delta );
            if( e.active ) {
                e.update( container, delta );
            }
        }

        // update particle system
        if( ME.ps != null ) {
            ME.ps.update( delta );
        }

        // remove signed entities
        for( Entity entity : removable ) {
            quadtree.remove( entity );
            belowCamera.remove( entity );
            aboveCamera.remove( entity );
            entity.removedFromWorld();
        }
        removable.clear();
        camera.update( delta );

        ME.update( container, game, delta );
    }

    @Override
    public int getID() {
        return id;
    }

    /**
     * Add entity to world and sort entity in z order
     *
     * @param e entity to add
     */
    public void add( Entity e, int... flags ) {
        e.setWorld( this );
        if( flags.length == 1 ) {
            switch( flags[0] ) {
                case BELOW:
                    belowCamera.add( e );
                    break;
                case GAME:
                    addable.add( e );
                    break;
                case ABOVE:
                    aboveCamera.add( e );
                    break;
            }
        }
        else {
            addable.add( e );
        }
    }

    public void addAll( Collection<Entity> e, int... flags ) {
        for( Entity entity : e ) {
            this.add( entity, flags );
        }
    }

    /**
     * @return List of entities currently in this world
     */
    public List<Entity> getEntities() {
        return quadtree.getAllEntities();
    }

    /**
     *
     * @param type
     * The entity type to count
     *
     * @return number of entities of the given type in this world
     */
    public int getNrOfEntities( String type ) {
        int number = 0;
        for( Entity entity : quadtree.getAllEntities() ) {
            if( entity.isType( type ) ) {
                number++;
            }
        }
        return number;
    }

    public List<Entity> getEntities( String type ) {
        List<Entity> res = new ArrayList<>();
        for( Entity entity : quadtree.getAllEntities() ) {
            if( entity.isType( type ) ) {
                res.add( entity );
            }
        }
        return res;
    }

    public List<Entity> getEntities( Rectangle rect ) {
        return quadtree.getEntities( rect );
    }

    public List<Entity> getEntities( Vector2f point ) {
        return quadtree.getEntities( point );
    }

    public void setMap( Map map ) {
        this.map = map;
    }

    public Map getMap() {
        return map;
    }

    /**
     * @param entity
     * to remove from game
     *
     * @return false if entity is already set to be remove
     */
    public boolean remove( Entity entity ) {
        if( !removable.contains( entity ) ) {
            return removable.add( entity );
        }
        return false;
    }

    /**
     * @param name
     *
     * @return null if name is null or if no entity is found in game, entity
     * otherwise
     */
    public Entity find( String name ) {
        if( name == null ) {
            return null;
        }
        for( Entity entity : quadtree.getAllEntities() ) {
            if( entity.name != null && entity.name.equalsIgnoreCase( name ) ) {
                return entity;
            }
        }
        // also look in addable list
        for( Entity entity : addable ) {
            if( entity.name != null && entity.name.equalsIgnoreCase( name ) ) {
                return entity;
            }
        }
        // and look in aboveCamera and belowCamera list
        for( Entity entity : aboveCamera ) {
            if( entity.name != null && entity.name.equalsIgnoreCase( name ) ) {
                return entity;
            }
        }
        for( Entity entity : belowCamera ) {
            if( entity.name != null && entity.name.equalsIgnoreCase( name ) ) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Remove all entities
     */
    public void clear() {
        for( Entity entity : quadtree.getAllEntities() ) {
            entity.removedFromWorld();
        }
        belowCamera.clear();
        aboveCamera.clear();
        quadtree.destroy();
        addable.clear();
        removable.clear();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth( int width ) {
        this.width = width;
        camera.setSceneWidth( width );
    }

    public int getHeight() {
        return height;
    }

    public void setHeight( int height ) {
        this.height = height;
        camera.setSceneHeight( height );
    }

    public boolean contains( Entity entity ) {
        return contains( entity.getBounds() );
    }

    public boolean contains( float x, float y, int width, int height ) {
        return x >= 0 && y >= 0 && x + width <= this.width
               && y + height <= this.height;
    }

    public boolean contains( Rectangle rectangle ) {
        return new Rectangle( 0, 0, width, height ).contains( rectangle );
    }

    public List<Entity> findEntityWithType( String type ) {
        if( type == null ) {
            Log.error( "Parameter must be not null" );
            return new ArrayList<>();
        }
        List<Entity> result = new ArrayList<>();
        for( Entity entity : quadtree.getAllEntities() ) {
            if( entity.isType( type ) ) {
                result.add( entity );
            }
        }
        return result;
    }

    public void resize( int width, int height ) {
        quadtree.resize( width, height );
    }

    public void moveEntity( Entity entity ) {
        quadtree.move( entity );
    }

    /**
     * @param x
* ram y
     *
     * @return true if an entity is already in position
     */
    public boolean isEmpty( int x, int y, int depth ) {
        for( Entity entity : quadtree.getEntities( new Vector2f( x, y ) ) ) {
            if( entity.depth == depth ) {
                return false;
            }
        }
        return true;
    }

    public Entity find( int x, int y ) {
        for( Entity entity : quadtree.getEntities( new Vector2f( x, y ) ) ) {
            return entity;
        }
        return null;
    }

    /**
     * @return get number of entities in this world
     */
    public int getCount() {
        return quadtree.getAllEntities().size();
    }

    /**
     * @see #bindToKey(String, int...)
     */
    public void define( String command, int... keys ) {
        bindToKey( command, keys );
    }

    /**
     * @see InputManager#bindToKey(String, int...)
     */
    public void bindToKey( String command, int... keys ) {
        input.bindToKey( command, keys );
    }

    /**
     * @see InputManager#bindToMouse(String, int...)
     */
    public void bindToMouse( String command, int... buttons ) {
        input.bindToMouse( command, buttons );
    }

    /**
     * @see InputManager#isDown(String)
     */
    public boolean check( String command ) {
        return input.isDown( command );
    }

    /**
     * @see InputManager#isPressed(String)
     */
    public boolean pressed( String command ) {
        return input.isPressed( command );
    }
}
