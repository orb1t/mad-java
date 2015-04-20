package uk.co.modularaudio.util.audio.gui.madstdctrls;

import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import javax.swing.JPanel;

public abstract class AbstractMadButton extends JPanel implements FocusListener
{
	private static final long serialVersionUID = -7622401208667882019L;

//	private static Log log = LogFactory.getLog( AbstractMadButton.class.getName() );

	protected enum MadButtonState
	{
		OUT_NO_MOUSE,
		OUT_MOUSE,
		IN_NO_MOUSE,
		IN_MOUSE
	};

	private static final int OUTLINE_ARC_WIDTH = 6;
	private static final int OUTLINE_ARC_HEIGHT = OUTLINE_ARC_WIDTH;

	private static final int INSIDE_ARC_WIDTH = 1;
	private static final int INSIDE_ARC_HEIGHT = OUTLINE_ARC_WIDTH;

	protected final MadButtonColours colours;

	protected MadButtonState pushedState = MadButtonState.OUT_NO_MOUSE;

	protected String text = "";

	protected int fontHeight = 0;
	protected FontMetrics fm;

	public AbstractMadButton( final MadButtonColours colours )
	{
		this( colours, null );
	}

	public AbstractMadButton( final MadButtonColours colours, final String textContent )
	{
		this.colours = colours;
		this.text = textContent;
		setOpaque( false );

		this.setFont( MadControlConstants.RACK_FONT );

		fm = this.getFontMetrics( MadControlConstants.RACK_FONT );
		fontHeight = fm.getHeight();

		setFocusable( true );

		this.addMouseListener( getMouseListener() );
		this.addFocusListener( this );
	}

	protected abstract MouseListener getMouseListener();

	private final void paintButton( final Graphics2D g2d,
			final MadButtonStateColours stateColours,
			final GradientPaint gp,
			final int width,
			final int height )
	{
		// outline
		g2d.setColor( stateColours.getControlOutline() );
		g2d.fillRoundRect( 0, 0, width, height, OUTLINE_ARC_WIDTH, OUTLINE_ARC_HEIGHT );

		// Background with gradient
		g2d.setPaint( gp );
		g2d.fillRoundRect( 1, 1, width-2, height-2, INSIDE_ARC_WIDTH, INSIDE_ARC_HEIGHT );

		// Highlight
		g2d.setColor( stateColours.getHighlight() );
		g2d.drawLine( 2, 1, width-3, 1 );

		if( text != null )
		{
			g2d.setColor( stateColours.getForegroundText() );
			final Rectangle stringBounds = fm.getStringBounds( text, g2d ).getBounds();
			final FontRenderContext frc = g2d.getFontRenderContext();
			final GlyphVector glyphVector = getFont().createGlyphVector( frc, text );
			final Rectangle visualBounds = glyphVector.getVisualBounds().getBounds();

			final int stringWidth = stringBounds.width;
			final int textLeft = (width - stringWidth) / 2;

			final int textBottom = (height - visualBounds.height) / 2 - visualBounds.y;

			g2d.drawString( text, textLeft, textBottom );
		}
	}


	@Override
	public void paintComponent( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();
		final Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

//		log.debug("Paint called we are in state " + pushedState.toString() );

		final MadButtonStateColours stateColours = colours.getColoursForState( pushedState );

		final GradientPaint bgGrad = new GradientPaint( 0, 0,
				stateColours.getContentGradStart(),
				0, height,
				stateColours.getContentGradEnd() );

		paintButton( g2d,
				stateColours,
				bgGrad,
				width, height );

		// Focus outline
		if( hasFocus() )
		{
			g2d.setColor( stateColours.getFocus() );
			g2d.drawRect( 5, 5, width-10, height-10 );
		}
	}

	@Override
	public void focusGained( final FocusEvent arg0 )
	{
//		log.debug("Gained focus repaint");
		repaint();
	}

	@Override
	public void focusLost( final FocusEvent arg0 )
	{
//		log.debug("Lost focus repaint");
		repaint();
	}
}
