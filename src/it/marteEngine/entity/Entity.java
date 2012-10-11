package it.marteEngine.entity;

import it.marteEngine.*;
import it.marteEngine.quadtree.Quad;
import java.util.*;
import java.util.Map;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

//TODO modify hitbox coordinates to a real shape without changing method interface.
//TODO a shape can be rotated and scaled when the entity is rotated and scaled.
public abstract class Entity implements Comparable<Entity>, Cloneable {

    /**
     * default collidable type SOLID
     */
    public static final String SOLID = "Solid";
    /**
     * predefined type for player
     */
    public static final String PLAYER = "Player";
    /**
     * predefined type for monsters
     */
    public static final String MONSTER = "Monster";
    /**
     * the world this entity lives in
     */
    public World world = null;
    /**
     * unique identifier
     */
    public String name;
    private Rectangle rect;
    /**
     * If this entity is centered the x,y position is in the center. otherwise
     * the x,y position is the top left corner.
     */
    public boolean centered = false;
    public float previousx, previousy;
    /**
     * start x and y position stored for reseting for example. very helpful
     */
    public float startx, starty;
    public boolean wrapHorizontal = false;
    public boolean wrapVertical = false;

    private Quad quad;
    /**
     * speed vector (x,y): specifies x and y movement per update call in pixels
     */
    public Vector2f speed = new Vector2f( 0, 0 );
    /**
     * angle in degrees from 0 to 360, used for drawing the entity rotated. NOT
     * used for direction!
     */
    protected int angle = 0;
    /**
     * scale used for both horizontal and vertical scaling.
     */
    public float scale = 1.0f;
    /**
     * color of the entity, mainly used for alpha transparency, but could also
     * be used for tinting
     */
    private Color color = new Color( Color.white );
    private AlarmContainer alarms;
    protected SpriteSheet sheet;
    private Map<String, Animation> animations = new HashMap<>();
    private String currentAnim;
    public int duration = 200;
    public int depth = -1;
    /**
     * static image for non-animated entity
     */
    public Image currentImage;
    public InputManager input;
    /**
     * The types this entity can collide with
     */
    private HashSet<String> collisionTypes = new HashSet<>();
    /**
     * true if this entity can receive updates
     */
    public boolean active = true;
    public boolean collidable = true;
    public boolean visible = true;
    public float hitboxOffsetX;
    public float hitboxOffsetY;
    public int hitboxWidth;
    public int hitboxHeight;
    public StateManager stateManager;
    private boolean leftTheWorld;


    @Override
    public Entity clone() throws CloneNotSupportedException {
        /* @TODO clone all instance variables */
        Entity clonedEntity = (Entity)super.clone();
        clonedEntity.setBounds( new Rectangle( rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight() ) );
        return clonedEntity;
    }

    /**
     * Create a new entity positioned at the (x,y) coordinates.
     */
    public Entity( float x, float y ) {
        rect = new Rectangle( x, y, 0, 0 );
        this.startx = x;
        this.starty = y;
        stateManager = new StateManager();
        alarms = new AlarmContainer( this );
        input = new InputManager();
    }

    /**
     * Create a new entity positioned at the (x,y) coordinates. Displayed as an
     * image.
     */
    public Entity( float x, float y, int width, int height ) {
        this( x, y );
        rect.setWidth( width );
        rect.setHeight( height );
    }

    /**
     * Create a new entity positioned at the (x,y) coordinates. Displayed as an
     * image.
     */
    public Entity( float x, float y, Image image ) {
        this( x, y );
        setGraphic( image );
    }

    /**
     * Returns the position and size of the entity as a Rectangle
     *
     * @return
     */
    public Rectangle getBounds() {
        return rect;
    }

    public void setBounds( Rectangle bounds ) {
        this.rect = bounds;
    }



    /**
     * Returns entitys current x position
     */
    public float getX() {
        return rect.getX();
    }

    /**
     * Returns entitys current y position
     */
    public float getY() {
        return rect.getY();
    }

    /**
     * Returns entitys current y position
     */
    public int getWidth() {
        return (int)rect.getWidth();
    }

    /**
     * Returns entitys current y position
     */
    public int getHeight() {
        return (int)rect.getHeight();
    }

    /**
     * Sets entity position
     *
     * @param x
* param y
     */
    public void setPosition( float x, float y ) {
        rect.setLocation( x, y );
    }

    /**
     * Sets entity position
     *
     * @param position
     */
    public void setPosition( Vector2f position ) {
        if( position != null ) {
            rect.setLocation( position );
            ME.world.moveEntity( this );
        }
    }

    /**
     * Moves the entity with dx, dy
     *
     * @param dxdy Distance to move
     */
    public void move( Vector2f dxdy ) {
        move( dxdy.getX(), dxdy.getY() );
    }

    /**
     * Moves the entity with dx, dy
     *
     * @param dx Distance to move horizontally
     * @param dy Distance to move vertically
     */
    public void move( float dx, float dy ) {
        rect.setX( rect.getX() + dx );
        rect.setY( rect.getY() + dy );
        ME.world.moveEntity(this);
    }

    /**
     * Sets entities x position
     *
     * @param x
     */
    public void setX( float x ) {
        rect.setX( x );

    }

    /**
     * Sets entities y position
     *
     * @param y
     */
    public void setY( float y ) {
        rect.setY( y );
    }

    /**
     * Sets entity width
     *
     * @param width
     */
    public void setWidth( int width ) {
        rect.setWidth( width - 1 );
    }

    /**
     * Sets entity width
     *
     * @param height
     */
    public void setHeight( int height ) {
        rect.setHeight( height - 1 );
    }

    public Rectangle getHitbox() {
        return new Rectangle( getX() + hitboxOffsetX, getY() + hitboxOffsetY, hitboxWidth, hitboxHeight );
    }

    /**
     * Set if the image or animation must be centered
     */
    public void setCentered( boolean center ) {
        int whalf = 0, hhalf = 0;
        if( currentImage != null ) {
            whalf = currentImage.getWidth() / 2;
            hhalf = currentImage.getHeight() / 2;
        }
        if( currentAnim != null ) {
            whalf = animations.get( currentAnim ).getWidth() / 2;
            hhalf = animations.get( currentAnim ).getHeight() / 2;
        }
        if( center ) {
            // modify hitbox position accordingly - move it a bit up and left
            this.hitboxOffsetX -= whalf;
            this.hitboxOffsetY -= hhalf;
            this.centered = true;
        }
        else {
            if( centered ) {
                // reset hitbox position to top left origin
                this.hitboxOffsetX += whalf;
                this.hitboxOffsetY += hhalf;
            }
            this.centered = false;
        }
    }

    public void update( GameContainer container, int delta )
            throws SlickException {
        previousx = getX();
        previousy = getY();
        if( stateManager != null && stateManager.currentState() != null ) {
            stateManager.update( container, delta );
            return;
        }
        updateAnimation( delta );
        if( speed != null && (speed.x != 0 && speed.y != 0) ) {
            move( speed.x, speed.y );
        }
        checkWorldBoundaries();
        previousx = getX();
        previousy = getY();
    }

    protected void updateAnimation( int delta ) {
        if( animations != null ) {
            if( currentAnim != null ) {
                Animation anim = animations.get( currentAnim );
                if( anim != null ) {
                    anim.update( delta );
                }
            }
        }
    }

    public void render( GameContainer container, Graphics g )
            throws SlickException {
        if( stateManager != null && stateManager.currentState() != null ) {
            stateManager.render( g );
            return;
        }
        float xpos = getX(), ypos = getY();
        if( currentAnim != null ) {
            Animation anim = animations.get( currentAnim );
            int w = anim.getWidth();
            int h = anim.getHeight();
            int whalf = w / 2;
            int hhalf = h / 2;
            if( centered ) {
                xpos = getX() - (whalf * scale);
                ypos = getY() - (hhalf * scale);
            }
            if( angle != 0 ) {
                g.rotate( getX(), getY(), angle );
            }
            anim.draw( xpos, ypos, w * scale, h * scale, color );
            if( angle != 0 ) {
                g.resetTransform();
            }
        }
        else if( currentImage != null ) {
            currentImage.setAlpha( color.a );
            int w = currentImage.getWidth() / 2;
            int h = currentImage.getHeight() / 2;
            if( centered ) {
                xpos -= w;
                ypos -= h;
                currentImage.setCenterOfRotation( w, h );
            }
            else {
                currentImage.setCenterOfRotation( 0, 0 );
            }

            if( angle != 0 ) {
                currentImage.setRotation( angle );
            }
            if( scale != 1.0f ) {
                if( centered ) {
                    g.translate( xpos - (w * scale - w), ypos - (h * scale - h) );
                }
                else {
                    g.translate( xpos, ypos );
                }
                g.scale( scale, scale );
                g.drawImage( currentImage, 0, 0 );
            }
            else {
                g.drawImage( currentImage, xpos, ypos );
            }
            if( scale != 1.0f ) {
                g.resetTransform();
            }
        }
        if( ME.debugEnabled && collidable ) {
            g.setColor( ME.borderColor );
            g.drawRect( getX() + hitboxOffsetX, getY() + hitboxOffsetY, hitboxWidth, hitboxHeight );
            g.setColor( Color.white );
            g.drawRect( getX(), getY(), 0, 0 );
            //draw entity center
            if( getWidth() != 0 && getHeight() != 0 ) {
                float centerX = getX() + getWidth() / 2;
                float centerY = getY() + getWidth() / 2;
                g.setColor( Color.green );
                g.drawRect( centerX - 1, centerY - 1, 1, 1 );
                g.setColor( Color.white );
            }
        }
    }

    /**
     * Set an image as graphic
     */
    public void setGraphic( Image image ) {
        this.currentImage = image;
        setWidth( image.getWidth() );
        setHeight( image.getHeight() );
    }

    /**
     * Set a sprite sheet as graphic
     */
    public void setGraphic( SpriteSheet sheet ) {
        this.sheet = sheet;
        setWidth( sheet.getSprite( 0, 0 ).getWidth() );
        setHeight( sheet.getSprite( 0, 0 ).getHeight() );
    }

    public void addAnimation( String animName, boolean loop, int row,
                              int... frames ) {
        Animation anim = new Animation( false );
        anim.setLooping( loop );
        for( int frame : frames ) {
            anim.addFrame( sheet.getSprite( frame, row ), duration );
        }
        addAnimation( animName, anim );
    }

    public Animation addAnimation( SpriteSheet sheet, String animName,
                                   boolean loop, int row, int... frames ) {
        Animation anim = new Animation( false );
        anim.setLooping( loop );
        for( int frame : frames ) {
            anim.addFrame( sheet.getSprite( frame, row ), duration );
        }
        addAnimation( animName, anim );
        return anim;
    }

    /**
     * Add animation to entity. The frames can be flipped horizontally and/or
     * vertically.
     */
    public void addFlippedAnimation( String animName, boolean loop,
                                     boolean fliphorizontal, boolean flipvertical, int row,
                                     int... frames ) {
        Animation anim = new Animation( false );
        anim.setLooping( loop );
        for( int frame : frames ) {
            anim.addFrame(
                    sheet.getSprite( frame, row ).getFlippedCopy( fliphorizontal,
                                                                  flipvertical ), duration );
            if( frame == 15 ) {
                row++;
            }
        }
        addAnimation( animName, anim );
    }

    /**
     * Add an animation.The first animation added is set as the current
     * animation.
     */
    public void addAnimation( String animName, Animation animation ) {
        boolean firstAnim = animations.isEmpty();
        animations.put( animName, animation );

        if( firstAnim ) {
            setAnim( animName );
        }
    }

    /**
     * Start playing the animation stored as animName.
     *
     * @param animName The name of the animation to play
     *
     * @throws IllegalArgumentException If there is no animation stored as
     * animName
     * @see #addAnimation(String, org.newdawn.slick.Animation)
     */
    public void setAnim( String animName ) {
        if( !animations.containsKey( animName ) ) {
            throw new IllegalArgumentException( "No animation for " + animName );
        }
        if( currentAnim != null && !currentAnim.equals( animName ) ) {
//            resetAnimation();
        }
        currentAnim = animName;
        Animation currentAnimation = animations.get( currentAnim );
        setWidth( currentAnimation.getWidth() );
        setHeight( currentAnimation.getHeight() );
    }

    public void resetAnimation() {
        Animation thisAnimation = animations.get( currentAnim );
        thisAnimation.restart();
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

    /**
     * Compare to another entity on zLevel
     */
    @Override
    public int compareTo( Entity o ) {
        if( depth == o.depth ) {
            if( getY() + getHeight() == o.getY() + o.getHeight() ) {
                if( getX() == o.getX() ) {
                    return 0;
                }
                if( getX() > o.getX() ) {
                    return 1;
                }
            }
            if( getY() + getHeight() > o.getY() + o.getHeight() ) {
                return 1;
            }
        }
        if( depth > o.depth ) {
            return 1;
        }
        return -1;
    }

    /**
     * Set the hitbox used for collision detection. If an entity has an hitbox,
     * it is collidable against other entities.
     *
     * @param xOffset The offset of the hitbox on the x axis. Relative to the
     * top left point of the entity.
     * @param yOffset The offset of the hitbox on the y axis. Relative to the
     * top left point of the entity.
     * @param width   The width of the rectangle in pixels
     * @param height  The height of the rectangle in pixels
     */
    public void setHitBox( float xOffset, float yOffset, int width, int height ) {
        this.hitboxOffsetX = xOffset;
        this.hitboxOffsetY = yOffset;
        this.hitboxWidth = width;
        this.hitboxHeight = height;
        setWidth( width );
        setHeight( height );
        this.collidable = true;
    }

    /**
     * Add a type that this entity can collide with. To allow collision with
     * other entities add at least 1 type. For example in a space invaders game.
     * To allow a ship to collide with a bullet and a monster:
     * ship.addType("bullet", "monster")
     *
     * @param types The types that this entity can collide with.
     */
    public boolean addType( String... types ) {
        return collisionTypes.addAll( Arrays.asList( types ) );
    }

    /**
     * Reset the types that this entity can collide with
     */
    public void clearTypes() {
        collisionTypes.clear();
    }

    /**
     * Reset the types that this entity can collide with
     */
    public void removeType( String type ) {
        if( collisionTypes.contains( type ) ) {
            collisionTypes.remove( type );
        }
    }

    /**
     * Check for a collision with another entity of the given entity type. Two
     * entities collide when the hitbox of this entity intersects with the
     * hitbox of another entity.
     * <p/>
     * The hitbox starts at the provided x,y coordinates. The size and offset of
     * the hitbox is set in the {@link #setHitBox(float, float, int, int)}
     * method.
     * <p/>
     * If a collision occurred then both the entities are notified of the
     * collision by the {@link #collisionResponse(Entity)} method.
     *
     * @param type The type of another entity to check for collision.
     * @param x    The x coordinate where the the collision check needs to be done.
     * @param y    The y coordinate where the the collision check needs to be done.
     *
     * @return The first entity that is colliding with this entity at the x,y
     * coordinates, or NULL if there is no collision.
     */
    public Entity collide( String type, float x, float y ) {
        if( type == null || type.isEmpty() ) {
            return null;
        }
        String[] types = { type };
        return collide( types, x, y );
    }

    /**
     * Checks for collision against multiple types.
     *
     * @see #collide(String, float, float)
     */
    public Entity collide( String[] types, float x, float y ) {
        Rectangle checkRect = new Rectangle( x + hitboxOffsetX, y + hitboxOffsetY, hitboxWidth, hitboxHeight );

        for( Entity entity : world.getEntities( checkRect ) ) {
            if( entity.collidable && entity.isType( types ) ) {
                if( !entity.equals( this ) && checkRect.intersects( entity.getHitbox() ) ) {
                    this.collisionResponse( entity );
                    entity.collisionResponse( this );
                    return entity;
                }
            }
        }
        return null;
    }

    /**
     * Checks if this Entity collides with a specific Entity.
     *
     * @param other The Entity to check for collision
     * @param x     The x coordinate where the the collision check needs to be done.
     * @param y     The y coordinate where the the collision check needs to be done.
     *
     * @return The entity that is colliding with the other entity at the x,y
     * coordinates, or NULL if there is no collision.
     */
    public Entity collideWith( Entity other, float x, float y ) {
        if( other.collidable ) {
            if( !other.equals( this )
                && x + hitboxOffsetX + hitboxWidth > other.getX()
                                                     + other.hitboxOffsetX
                && y + hitboxOffsetY + hitboxHeight > other.getY()
                                                      + other.hitboxOffsetY
                && x + hitboxOffsetX < other.getX() + other.hitboxOffsetX
                                       + other.hitboxWidth
                && y + hitboxOffsetY < other.getY() + other.hitboxOffsetY
                                       + other.hitboxHeight ) {
                this.collisionResponse( other );
                other.collisionResponse( this );
                return other;
            }
            return null;
        }
        return null;
    }

    public List<Entity> collideInto( String type, float x, float y ) {
        if( type == null || type.isEmpty() ) {
            return null;
        }
        ArrayList<Entity> collidingEntities = null;
        for( Entity entity : world.getEntities( new Vector2f( x, y ) ) ) {
            if( entity.collidable && entity.isType( type ) ) {
                if( !entity.equals( this )
                    && x + hitboxOffsetX + hitboxWidth > entity.getX()
                                                         + entity.hitboxOffsetX
                    && y + hitboxOffsetY + hitboxHeight > entity.getY()
                                                          + entity.hitboxOffsetY
                    && x + hitboxOffsetX < entity.getX() + entity.hitboxOffsetX
                                           + entity.hitboxWidth
                    && y + hitboxOffsetY < entity.getY() + entity.hitboxOffsetY
                                           + entity.hitboxHeight ) {
                    this.collisionResponse( entity );
                    entity.collisionResponse( this );
                    if( collidingEntities == null ) {
                        collidingEntities = new ArrayList<>();
                    }
                    collidingEntities.add( entity );
                }
            }
        }
        return collidingEntities;
    }

    /**
     * Checks if this Entity contains the specified point. The
     * {@link #collisionResponse(Entity)} is called to notify this entity of the
     * collision.
     *
     * @param x The x coordinate of the point to check
     * @param y The y coordinate of the point to check
     *
     * @return If this entity contains the specified point
     */
    public boolean collidePoint( float x, float y ) {
        if( x >= getX() - hitboxOffsetX && y >= getY() - hitboxOffsetY
            && x < getX() - hitboxOffsetX + getWidth()
            && y < getY() - hitboxOffsetY + getHeight() ) {
            this.collisionResponse( null );
            return true;
        }
        return false;
    }

    /**
     * overload if you want to act on addition to the world
     */
    public void addedToWorld() {
    }

    /**
     * overload if you want to act on removal from the world
     */
    public void removedFromWorld() {
    }

    /**
     * Response to a collision with another entity
     *
     * @param other The other entity that collided with us.
     */
    public void collisionResponse( Entity other ) {
    }

    /**
     * overload if you want to act on leaving world boundaries
     */
    public void leftWorldBoundaries() {
    }

    public Image getCurrentImage() {
        return currentImage;
    }

    public void setWorld( World world ) {
        this.world = world;
        input.setInput( world.container.getInput() );
    }

    /**
     * Check if this entity has left the world.
     *
     * If the entity has moved outside of the world then the entity is notified
     * by the
     * {@link #leftWorldBoundaries()} method. If the entity must be wrapped,
     * make it reappear on the opposite side of the world.
     */
    public void checkWorldBoundaries() {
        if( world.contains( this ) ) {
            leftTheWorld = false;
        }
        else {
            if( !leftTheWorld ) {
                leftWorldBoundaries();
                leftTheWorld = true;
            }
            wrapEntity();
        }
    }

    private void wrapEntity() {
        if( getX() + getWidth() < 0 ) {
            if( wrapHorizontal ) {
                setX( world.width - 1 );
            }
        }
        if( getX() > world.width ) {
            if( wrapHorizontal ) {
                setX( -getWidth() + 1 );
            }
        }
        if( getY() + getHeight() < 0 ) {
            if( wrapVertical ) {
                setY( world.height - 1 );
            }
        }
        if( getY() > world.height ) {
            if( wrapVertical ) {
                setY( -getHeight() + 1 );
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "name: " ).append( name );
        sb.append( ", types: " ).append( collisionTypesToString() );
        sb.append( ", depth: " ).append( depth );
        sb.append( ", x: " ).append( getX() );
        sb.append( ", y: " ).append( getY() );
        sb.append( ", width: " ).append( getWidth() );
        sb.append( ", height: " ).append( getHeight() );
        return sb.toString();
    }

    public String[] getCollisionTypes() {
        return collisionTypes.toArray( new String[collisionTypes.size()] );
    }

    public boolean isType( String type ) {
        return collisionTypes.contains( type );
    }

    public boolean isType( String[] types ) {
        for( String type : types ) {
            if( collisionTypes.contains( type ) )
                return true;
        }
        return false;
    }

    /**
     * remove ourselves from world
     */
    public void destroy() {
        this.world.remove( this );
        this.visible = false;
    }

    /**
     * *************** some methods to deal with angles and vectors
     * ***********************************
     */
    public int getAngleToPosition( Vector2f otherPos ) {
        Vector2f diff = otherPos.sub( new Vector2f( getX(), getY() ) );
        return (((int)diff.getTheta()) + 90) % 360;
    }

    public int getAngleDiff( int angle1, int angle2 ) {
        return ((((angle2 - angle1) % 360) + 540) % 360) - 180;
    }

    public Vector2f getPointWithAngleAndDistance( int angle, float distance ) {
        Vector2f point;
        float tx, ty;
        double theta = StrictMath.toRadians( angle + 90 );
        tx = (float)(getX() + distance * StrictMath.cos( theta ));
        ty = (float)(getY() + distance * StrictMath.sin( theta ));
        point = new Vector2f( tx, ty );
        return point;
    }

    public float getDistance( Entity other ) {
        return getDistance( new Vector2f( other.getX(), other.getY() ) );
    }

    public float getDistance( Vector2f otherPos ) {
        Vector2f myPos = new Vector2f( getX(), getY() );
        return myPos.distance( otherPos );
    }

    public static Vector2f calculateVector( float angle, float magnitude ) {
        Vector2f v = new Vector2f();
        v.x = (float)Math.sin( Math.toRadians( angle ) );
        v.x *= magnitude;
        v.y = (float)-Math.cos( Math.toRadians( angle ) );
        v.y *= magnitude;
        return v;
    }

    public static float calculateAngle( float x, float y, float x1, float y1 ) {
        double angle = Math.atan2( y - y1, x - x1 );
        return (float)(Math.toDegrees( angle ) - 90);
    }

    /**
     * *************** some methods to deal with alarms
     * ***********************************
     */
    /**
     * Add an alarm with the given parameters and add it to this Entity
     */
    public void addAlarm( String alarmName, int triggerTime, boolean oneShot ) {
        addAlarm( alarmName, triggerTime, oneShot, true );
    }

    /**
     * Add an alarm with given parameters and add it to this Entity
     */
    public void addAlarm( String alarmName, int triggerTime, boolean oneShot,
                          boolean startNow ) {
        Alarm alarm = new Alarm( alarmName, triggerTime, oneShot );
        alarms.addAlarm( alarm, startNow );
    }

    public boolean restartAlarm( String alarmName ) {
        return alarms.restartAlarm( alarmName );
    }

    public boolean pauseAlarm( String alarmName ) {
        return alarms.pauseAlarm( alarmName );
    }

    public boolean resumeAlarm( String alarmName ) {
        return alarms.resumeAlarm( alarmName );
    }

    public boolean destroyAlarm( String alarmName ) {
        return alarms.destroyAlarm( alarmName );
    }

    public boolean hasAlarm( String alarmName ) {
        return alarms.hasAlarm( alarmName );
    }

    /**
     * Overwrite this method if your entity shall react on alarms that reached
     * their triggerTime.
     *
     * @param alarmName the name of the alarm that triggered right now
     */
    public void alarmTriggered( String alarmName ) {
        // this method needs to be overwritten to deal with alarms
    }

    /**
     * this method is called automatically by the World and must not be called
     * by your game code. Don't touch this method ;-) Consider it private!
     */
    public void updateAlarms( int delta ) {
        alarms.update( delta );
    }

    public int getAngle() {
        return angle;
    }

    // TODO: add proper rotation for the hitbox/shape here!!!
    public void setAngle( int angle ) {
        this.angle = angle;
    }

    public Color getColor() {
        return color;
    }

    public void setColor( Color color ) {
        this.color = color;
    }

    public float getAlpha() {
        return color.a;
    }

    public void setAlpha( float alpha ) {
        if( alpha >= 0.0f && alpha <= 1.0f ) {
            color.a = alpha;
        }
    }

    public boolean isCurrentAnim( String animName ) {
        return currentAnim.equals( animName );
    }

    public String toCsv() {
        return "" + (int)getX() + "," + (int)getY() + "," + name + ","
               + collisionTypesToString();
    }

    private String collisionTypesToString() {
        StringBuilder sb = new StringBuilder();
        for( String type : collisionTypes ) {
            if( sb.length() > 0 ) {
                sb.append( ", " );
            }
            sb.append( type );
        }
        return sb.toString();
    }

    /**
     * @param shape the shape to check for intersection
     *
     * @return The entities that intersect with their hitboxes into the given
     * shape
     */
    public List<Entity> intersect( Shape shape ) {
        if( shape == null ) {
            return null;
        }
        List<Entity> result = new ArrayList<>();
        for( Entity entity : world.getEntities() ) {
            if( entity.collidable && !entity.equals( this ) ) {
                Rectangle rec = entity.getBounds();
                if( shape.intersects( rec ) ) {
                    result.add( entity );
                }
            }
        }
        return result;
    }

    public boolean containsPoint( Vector2f point ) {
        return (rect.getX() <= point.getX()
                && rect.getMaxX() >= point.getX()
                && rect.getY() <= point.getY()
                && rect.getMaxY() >= point.getY());
    }

    public Vector2f getPos() {
        return new Vector2f( getX(), getY() );
    }

    public Quad getQuad() {
        return quad;
    }

    public void setQuad(Quad quad) {
        this.quad = quad;
    }
}