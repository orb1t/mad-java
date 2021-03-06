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

package uk.co.modularaudio.mads.base.limiter.ui;

import java.awt.Component;

import uk.co.modularaudio.mads.base.limiter.mu.LimiterMadDefinition;
import uk.co.modularaudio.mads.base.limiter.mu.LimiterMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.colouredtoggle.ColouredLabelToggle;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.toggle.ToggleReceiver;

public class LimiterHardLimitCheckboxUiJComponent
	implements IMadUiControlInstance<LimiterMadDefinition, LimiterMadInstance, LimiterMadUiInstance>
{
	public static final boolean DEFAULT_USE_HARD_LIMIT = false;

	private final ColouredLabelToggle labelToggle;

	public LimiterHardLimitCheckboxUiJComponent(
			final LimiterMadDefinition definition,
			final LimiterMadInstance instance,
			final LimiterMadUiInstance uiInstance,
			final int controlIndex )
	{
		final ToggleReceiver toggleReceiver = new ToggleReceiver()
		{

			@Override
			public void receiveToggle( final int toggleId, final boolean active )
			{
				uiInstance.setUseHardLimit( active );
			}
		};

		labelToggle = new ColouredLabelToggle( "Hard limit",
				"Hard limit the signal at the specified knee",
				LWTCControlConstants.CONTROL_ROTCHO_CHOICE_BACKGROUND,
				LWTCControlConstants.CONTROL_ROTCHO_FLECHE_ACTIVE,
				LWTCControlConstants.CONTROL_ROTCHO_FLECHE_ACTIVE,
				DEFAULT_USE_HARD_LIMIT,
				toggleReceiver,
				0 );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public String getControlValue()
	{
		return labelToggle.getControlValue();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		labelToggle.receiveControlValue( value );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
	{
	}

	@Override
	public Component getControl()
	{
		return labelToggle;
	}

	@Override
	public void destroy()
	{
	}
}
