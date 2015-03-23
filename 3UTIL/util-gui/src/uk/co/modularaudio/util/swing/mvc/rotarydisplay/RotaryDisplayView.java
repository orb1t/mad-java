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

package uk.co.modularaudio.util.swing.mvc.rotarydisplay;

import java.awt.Color;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob.KnobType;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDoubleClickMouseListener.RotaryDoubleClickReceiver;

public class RotaryDisplayView extends JPanel
{
	private static final long serialVersionUID = 3201519946309189476L;
//	private static Log log = LogFactory.getLog( RotaryDisplayView.class.getName() );

	public enum DisplayOrientation
	{
		VERTICAL,
		HORIZONTAL
	};

	public enum SatelliteOrientation
	{
		ABOVE,
		RIGHT,
		BELOW,
		LEFT
	};

	private int numColumns = 1;

	private final RotaryDisplayLabel label;
	private final RotaryDisplayKnob knob;
	private final RotaryDisplayTextbox textbox;

	public RotaryDisplayView( final RotaryDisplayModel model,
			final RotaryDisplayController controller,
			final KnobType knobType,
			final SatelliteOrientation labelOrientation,
			final DisplayOrientation displayOrientation,
			final SatelliteOrientation textboxOrientation,
			final String labelText,
			final Color labelColor,
			final Color unitsColor,
			final Color backgroundColor,
			final Color foregroundColor,
			final Color knobColor,
			final Color outlineColor,
			final Color indicatorColor,
			final boolean opaque )
	{
		this( model,
				controller,
				knobType,
				labelOrientation,
				displayOrientation,
				textboxOrientation,
				labelText,
				labelColor,
				unitsColor,
				backgroundColor,
				foregroundColor,
				knobColor,
				outlineColor,
				indicatorColor,
				opaque,
				false );
	}

	public RotaryDisplayView( final RotaryDisplayModel model,
			final RotaryDisplayController controller,
			final KnobType knobType,
			final SatelliteOrientation labelOrientation,
			final DisplayOrientation displayOrientation,
			final SatelliteOrientation textboxOrientation,
			final String labelText,
			final Color labelColor,
			final Color unitsColor,
			final Color backgroundColor,
			final Color foregroundColor,
			final Color knobColor,
			final Color outlineColor,
			final Color indicatorColor,
			final boolean opaque,
			final boolean doubleClickToReset )
	{
		this.setOpaque( opaque );

		// If the label orientation or textbox orientation is left/right
		// we use a two column mode
		numColumns = 1 + ( labelOrientation == SatelliteOrientation.LEFT || textboxOrientation == SatelliteOrientation.LEFT ? 1 : 0 ) +
				(labelOrientation == SatelliteOrientation.RIGHT || textboxOrientation == SatelliteOrientation.RIGHT ? 1 : 0 );
		final MigLayoutStringHelper lh = new MigLayoutStringHelper();

//		lh.addLayoutConstraint( "debug" );
		lh.addLayoutConstraint( "insets 0" );
		lh.addLayoutConstraint( "gap 0" );
		if( numColumns == 1 )
		{
			lh.addLayoutConstraint( "flowy" );
			if( labelOrientation == SatelliteOrientation.ABOVE || textboxOrientation == SatelliteOrientation.ABOVE )
			{
				lh.addRowConstraint( "[fill]" );
				lh.addRowConstraint( "[]" );
			}
			else
			{
				lh.addRowConstraint( "[]" );
			}
			if( labelOrientation == SatelliteOrientation.BELOW || textboxOrientation == SatelliteOrientation.BELOW )
			{
				lh.addRowConstraint( "[fill]" );
			}
		}

		lh.addLayoutConstraint( "fill" );

		final MigLayout layout = lh.createMigLayout();
		setLayout( layout );

		label = new RotaryDisplayLabel( labelText, labelColor, opaque );
		knob = new RotaryDisplayKnob( model,
				controller,
				knobType,
				displayOrientation,
				backgroundColor,
				foregroundColor,
				knobColor,
				outlineColor,
				indicatorColor,
				opaque );
		textbox = new RotaryDisplayTextbox( model, controller, unitsColor, opaque );

		// Any needed components at top
		if( labelOrientation == SatelliteOrientation.ABOVE )
		{
			this.add( label, "center, bottom, grow 0" );
		}
		if( textboxOrientation == SatelliteOrientation.ABOVE )
		{
			this.add( textbox, "center, grow 0" );
		}

		// Left
		if( labelOrientation == SatelliteOrientation.LEFT )
		{
			this.add( label, "alignx right, aligny center" );
		}
		if( textboxOrientation == SatelliteOrientation.LEFT )
		{
			this.add( textbox, "align center" );
		}

		// Main knob
		if( numColumns == 2 )
		{
			if( displayOrientation == DisplayOrientation.HORIZONTAL )
			{
				this.add( knob, "center, grow, shrink 100, push, wrap");
			}
			else
			{
				this.add( knob, "center, grow, shrink 100, push, wrap");
			}
		}
		else
		{
			if( displayOrientation == DisplayOrientation.HORIZONTAL )
			{
//				log.debug("Adding slider with center push 50 shrink 100");
				this.add( knob, "center, grow 50, push 50, shrink 100" );
			}
			else
			{
				this.add( knob, "center, push 50, shrink 100" );
			}
		}

		// Now right
		if( textboxOrientation == SatelliteOrientation.RIGHT )
		{
			if( numColumns > 1 )
			{
//				log.debug("Adding textbox with pushx100 shrink 0 align center and wrap");
				this.add( textbox, "align center, pushx 0, shrink 0, wrap" );
			}
			else
			{
//				log.debug("Adding textbox with grow 0 shrink 0 align center");
				this.add( textbox, "align center, pushx 100, shrink 0" );
			}
		}
		if( labelOrientation == SatelliteOrientation.RIGHT )
		{
			if( numColumns > 1 )
			{
				this.add( label, "align left, grow 0, wrap" );
			}
			else
			{
				this.add( label, "align left, grow 0" );
			}
		}

		// And bottom
		if( textboxOrientation == SatelliteOrientation.BELOW )
		{
			if( numColumns > 1 )
			{
				this.add( textbox, "align center, grow 0, spanx " + numColumns );
			}
			else
			{
				this.add( textbox, "align center, grow 0" );
			}
		}
		if( labelOrientation == SatelliteOrientation.BELOW )
		{
			if( numColumns > 1 )
			{
				this.add( label, "center, top, growx, spanx " + numColumns );
			}
			else
			{
				this.add( label, "center, top, growx" );
			}
		}

		this.validate();

		if( doubleClickToReset )
		{
			addDoubleClickReceiver( new RotaryDoubleClickReceiver()
			{

				@Override
				public void receiveDoubleClick()
				{
					controller.setValue( this, controller.getModel().getInitialValue() );
				}
			} );
		}
	}

	public void addDoubleClickReceiver( final RotaryDoubleClickReceiver receiver )
	{
		final RotaryDoubleClickMouseListener doubleClickMouseListener = new RotaryDoubleClickMouseListener( receiver );
		knob.addMouseListener( doubleClickMouseListener );
	}

	public void changeModel( final RotaryDisplayModel newModel )
	{
		knob.changeModel( newModel );
		textbox.changeModel( newModel );
	}
}
