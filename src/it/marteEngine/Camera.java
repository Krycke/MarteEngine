package it.marteEngine;

import it.marteEngine.entity.Entity;
import org.newdawn.slick.geom.*;

/**
 * The camera shows a game in 2D perspective. It represents the area which is
 * seen to the player.
 */
public class Camera {

    /**
     * Camera position
     */
    private float cameraX, cameraY;
    /**
     * Camera dimension
     */
    private int cameraWidth, cameraHeight;
    private Vector2f cameraOffset = new Vector2f();
    /**
     * The rectangle that covers the camera, anything that is not within this
     * rectangle is not visible.
     */
    private Rectangle visibleRect;
    /**
     * The rectangle in which the entity can move without making the camera
     * scroll.
     */
    private Rectangle deadzone;
    /**
     * The offset between the camera position and the dead zone position. It is
     * used to update the dead zone position after the camera moved.
     */
    private float deadzoneXOffset, deadzoneYOffset;
    /**
     * The bounds of the scene. The camera cannot move outside of the scene.
     */
    private Rectangle scene;
    /**
     * The scroll speed
     */
    private Vector2f speed;
    /**
     * The destination to scroll to
     */
    private Vector2f target;
    private Entity entityToFollow;
    private FollowStyle followStyle;

    /**
     * Dead zone presets
     */
    public enum FollowStyle {

        /**
         * No dead zone, just tracks the focus object directly.
         */
        LOCKON,
        /**
         * Narrow but tall rectangle
         */
        PLATFORMER,
        /**
         * A medium-size square around the focus object.
         */
        TOPDOWN,
        /**
         * A small square around the focused object.
         */
        TOPDOWN_TIGHT,
        /**
         * Move screen by screen.
         */
        SCREEN_BY_SCREEN,}

    /**
     * Create a static camera that has the same size as the screen.
     *
     * @param width  The width of the screen
     * @param height The height of the screen
     */
    public Camera( int width, int height ) {
        this( width, height, width, height );
    }

    public Camera( int width, int height, Vector2f offset ) {
        this( width, height );
        cameraOffset = offset;
    }

    /**
     * Create a camera that can move within a scene.
     *
     * @param cameraWidth  The width of the camera
     * @param cameraHeight The height of the camera
     * @param sceneWidth   the width of the area in which the camera can move
     * @param sceneHeight  the height of the area in which the camera can move
     */
    public Camera( int cameraWidth, int cameraHeight, int sceneWidth,
                   int sceneHeight ) {
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
        this.visibleRect = new Rectangle( cameraX, cameraY, cameraWidth, cameraHeight );
        this.deadzone = new Rectangle( cameraX, cameraY, cameraWidth, cameraHeight );
        this.scene = new Rectangle( 0, 0, sceneWidth, sceneHeight );
        this.speed = new Vector2f();
        this.target = new Vector2f();

        if( cameraWidth > sceneWidth || cameraHeight > sceneHeight ) {
            throw new IllegalArgumentException(
                    "The camera cannot be larger then the scene" );
        }
        ME.world.resize( sceneWidth, sceneHeight );
    }

    public void update( int delta ) {
        if( isFollowingEntity() ) {
            followEntity();
        }

        if( cameraX != target.x || cameraY != target.y ) {
            if( followStyle == FollowStyle.SCREEN_BY_SCREEN ) {
                scrollToNextScreen();
            }
            else {
                scroll( delta );
            }
            updateZones();
        }
    }

    private void followEntity() {
        if( !isEntityWithinDeadzone() ) {
            keepInZone( entityToFollow );
//			center(entityToFollow);
        }
        else {
            target.set( cameraX, cameraY );
        }
    }

    private void scrollToNextScreen() {
        if( entityToFollow.getX() > cameraX + cameraWidth ) {
            setPosition( cameraX + cameraWidth, cameraY );
        }
        else if( entityToFollow.getX() < cameraX ) {
            setPosition( cameraX - cameraWidth, cameraY );
        }

        if( entityToFollow.getY() > cameraY + cameraHeight ) {
            setPosition( cameraX, cameraY + cameraHeight );
        }
        else if( entityToFollow.getY() < cameraY ) {
            setPosition( cameraX, cameraY - cameraHeight );
        }
    }

    private void scroll( int delta ) {
        if( speed.x == 0 && speed.y == 0 ) {
            setPosition( target.x, target.y );
        }
        else {
            float scrollX = 0, scrollY = 0;

            float distanceX = Math.abs( target.x - cameraX );
            if( distanceX != 0 && distanceX >= speed.x ) {
                scrollX = target.x > cameraX ? speed.x : -speed.x;
            }
            else {
                cameraX = target.x;
            }

            float distanceY = Math.abs( target.y - cameraY );
            if( distanceY != 0 && distanceY >= speed.y ) {
                scrollY = target.y > cameraY ? speed.y : -speed.y;
            }
            else {
                cameraY = target.y;
            }

            if( ME.useDeltaTiming ) {
                cameraX += scrollX * delta;
                cameraY += scrollY * delta;
            }
            else {
                cameraX += scrollX;
                cameraY += scrollY;
            }
        }
    }

    private void updateZones() {
        visibleRect.setLocation( cameraX, cameraY );
        deadzone.setLocation( cameraX + deadzoneXOffset, cameraY + deadzoneYOffset );
    }

    /**
     * Move the camera with the x and y offset. Using the speed of the camera.
     * See {@link #setSpeed} This is the same as calling following method
     * moveTo(camera.getX() + xOffset, camera.getY() + yOffset)
     *
     * @param xOffset The amount of pixels the camera should move on the x axis
     * @param yOffset The amount of pixels the camera should move on the y axis
     */
    public void scroll( float xOffset, float yOffset ) {
        if( xOffset == 0 && yOffset == 0 ) {
            return;
        }
        moveTo( cameraX + xOffset, cameraY + yOffset );
    }

    /**
     * Position the camera so that it keeps on following the entity.
     *
     * The camera movement is affected by the dead zone. An area in which the
     * entity can move without causing camera movement.
     * {@link #setDeadZone(int, int, int, int)}
     *
     * The entity is immediately centered in the camera.
     *
     * @param entity      The entity to follow
     * @param followStyle One of the existing dead zone presets. If you use a
     * custom dead zone, manually specify the dead zone after calling this
     * method.
     */
    public void follow( Entity entity, FollowStyle followStyle ) {
        // Calculate the position of the camera
        // so that the entity is centered
        center( entity );
        // Move immediately to that position
        setPosition( target.x, target.y );
        entityToFollow = entity;
        applyFollowStyle( entity, followStyle );
    }

    private void applyFollowStyle( Entity entity, FollowStyle followStyle ) {
        this.followStyle = followStyle;
        int w, h, helper;
        switch( followStyle ) {
            case PLATFORMER:
                w = cameraWidth / 5;
                h = cameraHeight / 3;
                setDeadZone( (cameraWidth - w) / 2,
                             (int)((cameraHeight - h) / 2 - h * 0.25), w, h );
                break;
            case TOPDOWN:
                helper = Math.max( cameraWidth, cameraHeight ) / 4;
                setDeadZone( (cameraWidth - helper) / 2, (cameraHeight - helper) / 2,
                             helper, helper );
                break;
            case TOPDOWN_TIGHT:
                helper = Math.max( cameraWidth, cameraHeight ) / 8;
                setDeadZone( (cameraWidth - helper) / 2, (cameraHeight - helper) / 2,
                             helper, helper );
                break;
            case LOCKON:
                w = entity.hitboxWidth;
                h = entity.hitboxHeight;
                setDeadZone( (cameraWidth - w) / 2,
                             (cameraHeight - h) / 2, w, h );
                break;
            case SCREEN_BY_SCREEN:
                setDeadZone( 0, 0, cameraWidth, cameraHeight );
                break;
            default:
                throw new IllegalArgumentException( "Unknown follow style "
                                                    + followStyle );
        }
    }

    public void stopFollowingEntity() {
        entityToFollow = null;
    }

    public void center( Entity entity ) {
        center( (entity.getX() + entity.hitboxOffsetX) + entity.hitboxWidth / 2, (entity.getY() + entity.hitboxOffsetY) + entity.hitboxHeight / 2 );
    }

    public void keepInZone( Entity entity ) {
        float targetX = cameraX;
        float targetY = cameraY;

        if( entity.getX() + entity.hitboxOffsetX < deadzone.getX() ) {
            targetX = cameraX - (deadzone.getX() - (entity.getX() + entity.hitboxOffsetX));
        }
        if( ((entity.getX() + entity.hitboxOffsetX) + entity.hitboxWidth) > (deadzone.getX() + deadzone.getWidth()) ) {
            targetX = cameraX + (((entity.getX() + entity.hitboxOffsetX) + entity.hitboxWidth) - (deadzone.getX() + deadzone.getWidth()));
        }

        if( (entity.getY() + entity.hitboxOffsetY) < deadzone.getY() ) {
            targetY = cameraY - (deadzone.getY() - (entity.getY() + entity.hitboxOffsetY));
        }
        if( (entity.getY() + entity.hitboxOffsetY) + entity.hitboxHeight > deadzone.getY() + deadzone.getHeight() ) {
            targetY = cameraY + (((entity.getY() + entity.hitboxOffsetY) + entity.hitboxHeight) - (deadzone.getY() + deadzone.getHeight()));
        }

        center( targetX + cameraWidth / 2, targetY + cameraHeight / 2 );
    }

    /**
     * Move the camera so that the given point is in the center of the camera.
     *
     * @param xToCenter X coordinate to center
     * @param yToCenter Y coordinate to center
     */
    public void center( float xToCenter, float yToCenter ) {
        float centerX = xToCenter - (cameraWidth / 2);
        float centerY = yToCenter - (cameraHeight / 2);

        moveTo( centerX, centerY );
    }

    /**
     * Move the camera smoothly to the target position. Using the speed of the
     * camera. If no speed is set the camera will moves directly to the targetX,
     * targetY position.
     *
     * @param x The x coordinate the camera should move to
     * @param y The y coordinate the camera should move to
     *
     * @see #setSpeed(float, float)
     */
    public void moveTo( float x, float y ) {
        float targetX = x;
        float targetY = y;
        if( cameraX == targetX && cameraY == targetY ) {
            return;
        }
        if( targetX < scene.getX() || !canMoveHorizontally() ) {
            targetX = scene.getX();
        }
        if( targetY < scene.getY() || !canMoveVertically() ) {
            targetY = scene.getY();
        }

        // Make sure the camera wraps at the edge
        targetX = wrapHorizontal( targetX );
        targetY = wrapVertical( targetY );
        target.set( targetX, targetY );
    }

    private float wrapHorizontal( float x ) {
        float targetX = x;
        if( canMoveHorizontally() ) {
            if( !isWithinScene( targetX, 0 ) ) {
                targetX = scene.getX();
            }
            if( !isWithinScene( targetX + cameraWidth, 0 ) ) {
                targetX = scene.getWidth() - cameraWidth - 1;
            }
        }
        return targetX;
    }

    private float wrapVertical( float y ) {
        float targetY = y;
        if( canMoveVertically() ) {
            if( !isWithinScene( 0, targetY ) ) {
                targetY = scene.getY();
            }
            if( !isWithinScene( 0, targetY + cameraHeight ) ) {
                targetY = scene.getHeight() - cameraHeight - 1;
            }
        }
        return targetY;
    }

    /**
     * Immediately move the camera to the x,y coordinate. Ignoring the speed.
     *
     * @param x The new x position of the camera
     * @param y The new y position of the camera
     */
    public void setPosition( float x, float y ) {
        this.cameraX = x;
        this.cameraY = y;

        // Overwrite target
        target.set( x, y );
        updateZones();
    }

    public boolean isEntityWithinDeadzone() {
        return entityToFollow != null
               && (deadzone.contains( entityToFollow.getBounds() ));
    }

    public boolean contains( Entity e ) {
        Rectangle entityRect = e.getBounds();
        return visibleRect.intersects( entityRect );
    }

    /**
     * Specifies x and y camera movement per update call in pixels. If no speed
     * is given (0,0) the camera will jump towards the destination.
     */
    public void setSpeed( float dx, float dy ) {
        speed.set( dx, dy );
    }

    /**
     * Allows to define a custom dead zone. The dead zone is a rectangle in
     * which the entity can move without causing camera movement. The dead zone
     * is only used when following an entity.
     *
     * There is only 1 dead zone active at any time. The previous values will be
     * overwritten.
     *
     * @param x      The start position of the rectangle, relative to the camera 0,0
     * position.
     * @param y      The start position of the rectangle, relative to the camera 0,0
     * position.
     * @param width  the width of the dead zone
     * @param height the height of the dead zone
     */
    public void setDeadZone( int x, int y, int width, int height ) {
        deadzone.setBounds( this.cameraX + x, this.cameraY + y, width, height );
        deadzoneXOffset = Math.abs( x );
        deadzoneYOffset = Math.abs( y );
    }

    /**
     * Enlarge the scene on all sides by offset pixels.
     *
     * @param offset The offset to add to the scene
     */
    public void setSceneOffset( int offset ) {
        setSceneBounds( (int)scene.getX() - offset,
                        (int)scene.getY() - offset, (int)scene.getWidth() + offset,
                        (int)scene.getHeight() + offset );
    }

    /**
     * Set the scene boundaries. These boundaries define where the camera stops
     * scrolling. Most of the time the scene and game have equal size.
     *
     * To allow the camera to move outside of the game world by 50 pixels you
     * would set minX and minY to -50 and width and height to worldWidth+50 and
     * worldHeight+50.
     *
     * @param minX        The smallest x value of your scene (usually 0).
     * @param minY        The smallest y value of your scene (usually 0).
     * @param sceneWidth  The largest x value of your scene (usually the game
     * width).
     * @param sceneHeight The largest y value of your scene (usually the game
     * height).
     */
    public void setSceneBounds( int minX, int minY, int sceneWidth,
                                int sceneHeight ) {
        scene.setLocation( minX, minY );
        setSceneWidth( sceneWidth );
        setSceneHeight( sceneHeight );
        ME.world.resize( sceneWidth, sceneHeight);
    }

    public void setSceneWidth( int sceneWidth ) {
        scene.setWidth( sceneWidth );
    }

    public void setSceneHeight( int sceneHeight ) {
        scene.setHeight( sceneHeight );
    }

    private boolean isWithinScene( float x, float y ) {
        return x >= scene.getX() && x <= scene.getWidth() && y >= scene.getY()
               && y <= scene.getHeight();
    }

    private boolean canMoveHorizontally() {
        return cameraX >= scene.getX() && cameraWidth < scene.getWidth();
    }

    private boolean canMoveVertically() {
        return cameraY >= scene.getY() && cameraHeight < scene.getHeight();
    }

    public float getX() {
        return cameraX;
    }

    public float getY() {
        return cameraY;
    }

    public boolean isFollowingEntity() {
        return entityToFollow != null;
    }

    public Rectangle getVisibleRect() {
        return visibleRect;
    }

    public Rectangle getScene() {
        return scene;
    }

    public Rectangle getDeadzone() {
        return deadzone;
    }

    public int getWidth() {
        return cameraWidth;
    }

    public int getHeight() {
        return cameraHeight;
    }

    public FollowStyle getCurrentFollowStyle() {
        return followStyle;
    }

    public Vector2f getCameraOffset() {
        return cameraOffset;
    }

    @Override
    public String toString() {
        return String.format(
                "Camera: (%.0f,%.0f) Target(%.0f,%.0f) Following entity:%s", cameraX,
                cameraY, target.x, target.y, isFollowingEntity() );
    }
}