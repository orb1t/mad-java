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

package uk.co.modularaudio.mads.internal.fade.mu;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.modularaudio.mads.internal.InternalComponentsCreationContext;
import uk.co.modularaudio.util.audio.lookuptable.fade.FadeConstants;
import uk.co.modularaudio.util.audio.lookuptable.fade.FadeOutWaveTable;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class FadeOutMadInstance extends MadInstance<FadeOutMadDefinition, FadeOutMadInstance>
{
	private final AtomicInteger curTablePosition = new AtomicInteger(0);
	private FadeOutWaveTable waveTable;

	public FadeOutMadInstance( final InternalComponentsCreationContext creationContext,
			final String instanceName,
			final FadeOutMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void start( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
		throws MadProcessingException
	{
		waveTable = new FadeOutWaveTable( hardwareChannelSettings.getAudioChannelSetting().getDataRate(), FadeConstants.FADE_MILLIS );
	}

	@Override
	public void stop() throws MadProcessingException
	{
		// Nothing to do
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final long periodStartFrameTime ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers ,
			int frameOffset , final int numFrames  )
	{
		int runningTablePosition = curTablePosition.get();
		// Only do some processing if we are connected
		if( channelConnectedFlags.get( FadeOutMadDefinition.CONSUMER ) &&
				channelConnectedFlags.get( FadeOutMadDefinition.PRODUCER ) )
		{
			final MadChannelBuffer in = channelBuffers[ FadeOutMadDefinition.CONSUMER ];
			final float[] inBuffer = in.floatBuffer;

			final MadChannelBuffer out = channelBuffers[ FadeOutMadDefinition.PRODUCER ];
			final float[] outBuffer = out.floatBuffer;

			// Use the fade wave table and our current position to pull out the fade value to use.
			for( int i = 0 ; i < numFrames ; i++ )
			{
				final float curVal = inBuffer[i];
				final float currentFadeMultiplier = waveTable.getValueAt( runningTablePosition );
				outBuffer[i] = curVal * currentFadeMultiplier;
				runningTablePosition++;
			}
		}
		else
		{
			runningTablePosition += numFrames;
		}
		curTablePosition.set( runningTablePosition );

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public boolean completed()
	{
		if( waveTable != null )
		{
			return curTablePosition.get() >= waveTable.capacity;
		}
		else
		{
			return false;
		}
	}
}
