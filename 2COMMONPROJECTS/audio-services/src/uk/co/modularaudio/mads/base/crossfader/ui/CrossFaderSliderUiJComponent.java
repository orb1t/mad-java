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

package uk.co.modularaudio.mads.base.crossfader.ui;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadDefinition;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCSlider;

public class CrossFaderSliderUiJComponent extends LWTCSlider
	implements IMadUiControlInstance<CrossFaderMadDefinition, CrossFaderMadInstance, CrossFaderMadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	private final CrossFaderMadUiInstance uiInstance;

	public CrossFaderSliderUiJComponent(
			final CrossFaderMadDefinition definition,
			final CrossFaderMadInstance instance,
			final CrossFaderMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( SwingConstants.HORIZONTAL,
				new DefaultBoundedRangeModel(
						0, 0,
						-1000, 1000 ),
						LWTCControlConstants.STD_SLIDER_NOMARK_COLOURS,
						false );

		this.uiInstance = uiInstance;

		model.addChangeListener( new ChangeListener()
		{

			@Override
			public void stateChanged( final ChangeEvent e )
			{
				passChangeToInstanceData( model.getValue() );
			}
		} );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final int value )
	{
		final float newValue = (value) / 1000.0f;
		uiInstance.setCrossFaderPosition( newValue );
		uiInstance.recalculateAmps();
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void destroy()
	{
		// Nothing needed
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public String getControlValue()
	{
		return Integer.toString( model.getValue() );
	}

	@Override
	public void receiveControlValue( final String value )
	{
		final int pos = Integer.parseInt( value );
		model.setValue( pos );
	}
}
