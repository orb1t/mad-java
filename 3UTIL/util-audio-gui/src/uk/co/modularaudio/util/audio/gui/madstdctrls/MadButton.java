package uk.co.modularaudio.util.audio.gui.madstdctrls;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class MadButton extends AbstractMadButton implements MouseListener
{
	private static final long serialVersionUID = -2594637398951298132L;

	private static Log log = LogFactory.getLog( MadButton.class.getName() );

	public MadButton( final MadButtonColours colours, final String text )
	{
		super( colours, text );
	}

	public String getControlValue()
	{
		return "";
	}

	public void receiveControlValue( final String strValue )
	{
	}

	public abstract void receiveClick();

	@Override
	public MouseListener getMouseListener()
	{
		return this;
	}

	@Override
	public void mouseClicked( final MouseEvent arg0 )
	{
	}

	@Override
	public void mouseEntered( final MouseEvent arg0 )
	{
		switch( pushedState )
		{
			case OUT_NO_MOUSE:
			{
				pushedState = MadButtonState.OUT_MOUSE;
				break;
			}
			case IN_NO_MOUSE:
			{
				pushedState = MadButtonState.IN_MOUSE;
				break;
			}
			default:
			{
				log.error( "Oops - state issue" );
			}
		}
		repaint();
	}

	@Override
	public void mouseExited( final MouseEvent arg0 )
	{
		switch( pushedState )
		{
			case OUT_MOUSE:
			{
				pushedState = MadButtonState.OUT_NO_MOUSE;
				break;
			}
			case IN_MOUSE:
			{
				pushedState = MadButtonState.IN_NO_MOUSE;
				break;
			}
			default:
			{
				log.error( "Oops - state issue" );
			}
		}
		repaint();
	}

	@Override
	public void mousePressed( final MouseEvent arg0 )
	{
		switch( pushedState )
		{
			case IN_MOUSE:
			{
				pushedState = MadButtonState.OUT_MOUSE;
				break;
			}
			case IN_NO_MOUSE:
			{
				pushedState = MadButtonState.OUT_NO_MOUSE;
				break;
			}
			case OUT_MOUSE:
			{
				pushedState = MadButtonState.IN_MOUSE;
				break;
			}
			case OUT_NO_MOUSE:
			{
				pushedState = MadButtonState.IN_NO_MOUSE;
				break;
			}
			default:
			{
				log.error( "Oops - state issue" );
			}
		}
		if( !hasFocus() )
		{
			requestFocusInWindow();
		}
		repaint();
	}

	@Override
	public void mouseReleased( final MouseEvent arg0 )
	{
		switch( pushedState )
		{
			case IN_MOUSE:
			{
				pushedState = MadButtonState.OUT_MOUSE;
				break;
			}
			case IN_NO_MOUSE:
			{
				pushedState = MadButtonState.OUT_NO_MOUSE;
				break;
			}
			case OUT_MOUSE:
			{
				pushedState = MadButtonState.IN_MOUSE;
				break;
			}
			case OUT_NO_MOUSE:
			{
				pushedState = MadButtonState.IN_NO_MOUSE;
				break;
			}
			default:
			{
				log.error( "Oops - state issue" );
			}
		}
		repaint();
		receiveClick();
	}
}
