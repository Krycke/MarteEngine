package it.marteEngine.entity;

import java.util.ArrayList;
import org.newdawn.slick.*;

public class TextEntity extends Entity {

    public enum Justify{
        LEFT,
        RIGHT,
        CENTER;
    }

    private Font font = null;
    private String text = null;
    private ArrayList<String> rows;
    private Justify justify = Justify.LEFT;
    private String fontString = java.awt.Font.SANS_SERIF;
    private int style = java.awt.Font.BOLD | java.awt.Font.ITALIC;
    private int size = 16;

    public TextEntity( float x, float y, Font font, String text ) {
        super( x, y );
        this.font = font;
        this.text = text;
    }

    public TextEntity( float x, float y, Font font, ArrayList<String> rows ) {
        super( x, y );
        this.font = font;
        this.rows = rows;
    }

    @Override
    public void render( GameContainer container, Graphics g )
        throws SlickException {
        super.render( container, g );

        if( font == null ) {
            java.awt.Font awtFont = new java.awt.Font(fontString,style, size);
            font = new TrueTypeFont(awtFont, false);
        }
        if( rows == null ) {
            rows = splitText();
        }
        g.setFont( font );
        float y = getY() + hitboxOffsetY;
        for( String row : rows ) {
            g.drawString( row, getNextX( row ), y );
            y += font.getLineHeight() - 3;
        }
    }

    public float getNextX( String row ){
        switch( justify ) {
            case RIGHT:
                return getX() + hitboxOffsetX + hitboxWidth - font.getWidth( row );
            case CENTER:
                return getX() + ( hitboxOffsetX + hitboxWidth / 2 ) - font.getWidth( row ) / 2;
            default:
                return getX();
        }
    }

    public void setJustify( Justify justify ) {
        this.justify = justify;
    }

    public void fixTextBox() {
        int fontSize = size;
        do {
            java.awt.Font awtFont = new java.awt.Font( fontString, style, fontSize );
            this.font = new TrueTypeFont( awtFont, false );
            rows = splitText();
            fontSize--;
        }
        while( rows.size() * ( this.font.getLineHeight() - 3 ) > hitboxHeight );
    }

    private ArrayList<String> splitText() {
        ArrayList<String> rows = new ArrayList<>();
        if( text != null ) {
            String[] words = text.split( "\\s+" );
            int i = 0;
            while( i < words.length ) {
                String row = words[i];

                while( i + 1 < words.length && font.getWidth( row + " " + words[i + 1] ) < hitboxWidth ) {
                    i++;
                    row += " " + words[i];
                }
                rows.add( row );
                i++;
            }
        }
        return rows;
    }

    public void setRows( ArrayList<String> rows ) {
        this.rows = rows;
    }

    public void setStyle( int style ) {
        this.style = style;
    }

    public void setSize( int size ) {
        this.size = size;
    }

    public Font getFont() {
        return font;
    }

    public void setFont( Font font ) {
        this.font = font;
    }

    public String getText() {
        return text;
    }

    public void setText( String text ) {
        this.text = text;
    }

    private void calculateHitBox() {
        if( font != null && text != null ) {
            int w = font.getWidth( text );
            int h = font.getHeight( text );
            this.setHitBox( 0, 0, w, h );
        }
    }
}
