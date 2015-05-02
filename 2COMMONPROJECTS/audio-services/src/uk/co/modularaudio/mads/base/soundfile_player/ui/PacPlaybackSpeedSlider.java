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

package uk.co.modularaudio.mads.base.soundfile_player.ui;

import javax.swing.JPanel;

import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.SatelliteOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderViewColors;

public abstract class PacPlaybackSpeedSlider extends JPanel implements ValueChangeListener
{
	private static final long serialVersionUID = 2783504281404548759L;

	protected LWTCSliderDisplayView view;
	protected SliderDisplayModel model;
	protected SliderDisplayController controller;

	public PacPlaybackSpeedSlider( final float minValue,
			final float maxValue,
			final float initialValue,
			final float defaultValue,
			final String unitsStr,
			final SatelliteOrientation labelOrientation,
			final DisplayOrientation displayOrientation,
			final SatelliteOrientation textboxOrientation,
			final LWTCSliderViewColors colors,
			final String labelText,
			final boolean opaque )
	{
		this.setOpaque( opaque );
		this.model = new SliderDisplayModel( minValue,
				maxValue,
				initialValue,
				defaultValue,
				2000,
				500,
				new SimpleSliderIntToFloatConverter(),
				2,
				2,
				unitsStr );

		this.controller = new SliderDisplayController( model );
		this.view = new LWTCSliderDisplayView( model,
				controller,
				labelOrientation,
				displayOrientation,
				textboxOrientation,
				colors,
				labelText,
				opaque );

		final MigLayoutStringHelper lh = new MigLayoutStringHelper();
		lh.addLayoutConstraint( "fill" );
		lh.addLayoutConstraint( "insets 0" );
		lh.addLayoutConstraint( "gap 0" );
//		lh.addLayoutConstraint( "debug" );
		this.setLayout( lh.createMigLayout() );
		this.add( view, "grow" );

		// Finally subscribe to the mode so that derived classes can handle the value change
		model.addChangeListener( this );
	}

	@Override
	public abstract void receiveValueChange( Object source, float newValue );

}
