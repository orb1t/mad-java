/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.util.swing.lwtc;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public abstract class LWTCToggleButton extends AbstractLWTCButton
{
	private static final long serialVersionUID = -2594637398951298132L;

//	private static Log log = LogFactory.getLog( LWTCToggleButton.class.getName() );

	private class ToggleButtonMouseListener implements MouseListener, MouseMotionListener
	{
		private boolean pushedStateBeforeDrag;

		public ToggleButtonMouseListener( final boolean defaultPushedState )
		{
			this.pushedStateBeforeDrag = defaultPushedState;
		}

		@Override
		public void mouseDragged( final MouseEvent me )
		{
//			log.debug("mouseDragged event");
			if( !isImmediate )
			{
				if( isPushed )
				{
					if( !contains( me.getPoint() ) )
					{
						isPushed = false;
						repaint();
					}
				}
				else
				{
					if( contains( me.getPoint() ) )
					{
						isPushed = true;
						repaint();
					}
				}
			}
		}

		@Override
		public void mouseMoved( final MouseEvent me )
		{
		}

		@Override
		public void mouseClicked( final MouseEvent me )
		{
//			log.debug("mouseClicked event");
		}

		@Override
		public void mousePressed( final MouseEvent me )
		{
//			log.debug("mousePressed event");
			if( me.getButton() == MouseEvent.BUTTON1 )
			{
				if( !hasFocus() )
				{
					requestFocusInWindow();
				}

				pushedStateBeforeDrag = isPushed;
				isPushed = !pushedStateBeforeDrag;
				if( isImmediate )
				{
					setSelected( isPushed );
				}

				repaint();
				me.consume();
			}
		}

		@Override
		public void mouseReleased( final MouseEvent me )
		{
//			log.debug("mouseReleased event");
			if( me.getButton() == MouseEvent.BUTTON1 )
			{
				if( !isImmediate )
				{
					if( contains( me.getPoint() ) )
					{
						isPushed = !pushedStateBeforeDrag;
						setSelected( isPushed );
					}
					else
					{
						isPushed = pushedStateBeforeDrag;
					}
				}

				repaint();
				me.consume();
			}
		}

		@Override
		public void mouseEntered( final MouseEvent me )
		{
//			log.debug("mouseEntered event");
			final int onmask = MouseEvent.BUTTON1_DOWN_MASK;
			if( (me.getModifiersEx() & onmask) != onmask )
			{
				if( !mouseEntered )
				{
					mouseEntered = true;
				}
				repaint();
				me.consume();
			}
		}

		@Override
		public void mouseExited( final MouseEvent me )
		{
//			log.debug("mouseExited event");
			if( mouseEntered )
			{
				mouseEntered = false;
			}
			repaint();
			me.consume();
		}
	};

	private class ButtonKeyListener implements KeyListener
	{
		@Override
		public void keyPressed( final KeyEvent ke )
		{
			final int keyCode = ke.getKeyCode();
			final int modMask = ke.getModifiers();

			if( modMask != 0 )
			{
				return;
			}
			switch( keyCode )
			{
				case KeyEvent.VK_SPACE:
				case KeyEvent.VK_ENTER:
				{
					final boolean previousValue = isPushed;
					isPushed = !previousValue;
					if( isImmediate )
					{
						setSelected( isPushed );
					}

					repaint();
					break;
				}
				default:
				{
					break;
				}
			}
			ke.consume();
		}

		@Override
		public void keyReleased( final KeyEvent ke )
		{
			final int keyCode = ke.getKeyCode();
			final int modMask = ke.getModifiers();

			if( modMask != 0 )
			{
				return;
			}
			switch( keyCode )
			{
				case KeyEvent.VK_SPACE:
				case KeyEvent.VK_ENTER:
				{
					if( !isImmediate )
					{
						setSelected( isPushed );
					}

					repaint();
					break;
				}
				default:
				{
					break;
				}
			}
			ke.consume();
		}

		@Override
		public void keyTyped( final KeyEvent ke ) {} // NOPMD by dan on 27/04/15 12:23
	};

	protected boolean isImmediate;
	protected boolean isSelected;

	public LWTCToggleButton( final LWTCButtonColours colours,
			final String text,
			final boolean isImmediate,
			final boolean defaultValue )
	{
		super( colours, text );
		this.isImmediate = isImmediate;
		isSelected = defaultValue;
		isPushed = isSelected;

		final ToggleButtonMouseListener tbml = new ToggleButtonMouseListener( isSelected );
		addMouseListener( tbml );
		addMouseMotionListener( tbml );

		final ButtonKeyListener kl = new ButtonKeyListener();
		addKeyListener( kl );
	}

	public void setSelected( final boolean newSelected )
	{
		final boolean previousValue = isSelected;
		setSelectedNoPropogate( newSelected );
		receiveUpdateEvent( previousValue, newSelected );
	}

	public void setSelectedNoPropogate( final boolean newSelected )
	{
		isSelected = newSelected;
		isPushed = newSelected;
	}

	public boolean isSelected()
	{
		return isSelected;
	}

	public String getControlValue()
	{
		return Boolean.toString( isSelected );
	}

	public void receiveControlValue( final String strValue )
	{
		setSelected( Boolean.parseBoolean( strValue ) );
	}

	public abstract void receiveUpdateEvent( boolean previousValue, boolean newValue );

}
