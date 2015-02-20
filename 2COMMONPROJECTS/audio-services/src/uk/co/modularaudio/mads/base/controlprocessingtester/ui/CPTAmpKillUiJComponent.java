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

package uk.co.modularaudio.mads.base.controlprocessingtester.ui;

import java.awt.Font;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.controlprocessingtester.mu.CPTMadDefinition;
import uk.co.modularaudio.mads.base.controlprocessingtester.mu.CPTMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacToggleButton;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class CPTAmpKillUiJComponent extends PacToggleButton
	implements IMadUiControlInstance<CPTMadDefinition, CPTMadInstance, CPTMadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	private final CPTMadUiInstance uiInstance;

	public CPTAmpKillUiJComponent(
			final CPTMadDefinition definition,
			final CPTMadInstance instance,
			final CPTMadUiInstance uiInstance,
			final int controlIndex )
	{
		// Default value
		super( false );

		this.uiInstance = uiInstance;
		this.setOpaque( false );
//		Font f = this.getFont().deriveFont( 9f );
		final Font f = this.getFont();
		setFont( f );
		this.setText( "Kill" );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final boolean selected )
	{
		uiInstance.setGuiKill( selected );
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
	public void receiveUpdateEvent( final boolean previousValue, final boolean newValue )
	{
		if( previousValue != newValue )
		{
			passChangeToInstanceData( newValue );
		}
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}