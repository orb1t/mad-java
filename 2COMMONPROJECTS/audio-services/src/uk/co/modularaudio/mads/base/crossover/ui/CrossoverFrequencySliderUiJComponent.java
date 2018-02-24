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

package uk.co.modularaudio.mads.base.crossover.ui;

import java.awt.Component;

import uk.co.modularaudio.mads.base.crossover.mu.CrossoverMadDefinition;
import uk.co.modularaudio.mads.base.crossover.mu.CrossoverMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.OscillatorFrequencySliderModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.SatelliteOrientation;

public class CrossoverFrequencySliderUiJComponent
	implements IMadUiControlInstance<CrossoverMadDefinition, CrossoverMadInstance, CrossoverMadUiInstance>
{
	private final OscillatorFrequencySliderModel model;
	private final SliderDisplayController controller;
	private final LWTCSliderDisplayView view;

	public CrossoverFrequencySliderUiJComponent(
			final CrossoverMadDefinition definition,
			final CrossoverMadInstance instance,
			final CrossoverMadUiInstance uiInstance,
			final int controlIndex )
	{

		model = new OscillatorFrequencySliderModel();
		controller = new SliderDisplayController( model );
		view = new LWTCSliderDisplayView(
				model,
				controller,
				SatelliteOrientation.LEFT,
				DisplayOrientation.HORIZONTAL,
				SatelliteOrientation.RIGHT,
				LWTCControlConstants.SLIDER_VIEW_COLORS,
				"Freq:",
				false );

		view.setLabelMinSize( CrossoverMadUiDefinition.SLIDER_LABEL_MIN_WIDTH, 30 );

		model.addChangeListener( new ValueChangeListener()
		{

			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				uiInstance.sendFrequencyChange( model.getValue() );
			}
		} );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public String getControlValue()
	{
		return Float.toString( model.getValue() );
	}

	@Override
	public void receiveControlValue( final String value )
	{
		final float val = Float.parseFloat( value );
		controller.setValue( this, val );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage ,
			final MadTimingParameters timingParameters ,
			final int U_currentGuiTime , final int framesSinceLastTick  )
	{
	}

	@Override
	public Component getControl()
	{
		return view;
	}

	@Override
	public void destroy()
	{
	}

}
