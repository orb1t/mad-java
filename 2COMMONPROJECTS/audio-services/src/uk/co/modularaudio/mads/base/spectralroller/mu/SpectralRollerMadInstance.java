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

package uk.co.modularaudio.mads.base.spectralroller.mu;

import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.buffer.BackendToFrontendDataRingBuffer;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class SpectralRollerMadInstance extends MadInstance<SpectralRollerMadDefinition,SpectralRollerMadInstance>
{
//	private static Log log = LogFactory.getLog( SpectralRollerMadInstance.class.getName() );

	protected boolean active;

	private int maxRingBufferingInSamples;

	private BackendToFrontendDataRingBuffer dataRingBuffer;

	private int numSamplesPerFrontEndPeriod;

	public SpectralRollerMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final SpectralRollerMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		dataRingBuffer = new BackendToFrontendDataRingBuffer( 1 );
	}

	@Override
	public void start( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		try
		{
			final int sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

			// We will need enough buffer space such to queue samples between GUI frames
			// that takes into account some amount of output latency

			final long nanosFeBuffering = timingParameters.getNanosPerFrontEndPeriod() * 2;
			// Use up to three back end periods
			final long nanosBeBuffering = timingParameters.getNanosPerBackEndPeriod() * 3;
			final long nanosForBuffering = nanosFeBuffering + nanosBeBuffering;

			// We have to handle enough per visual frame along with the necessary audio IO latency
			maxRingBufferingInSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 16) +
					AudioTimingUtils.getNumSamplesForNanosAtSampleRate( sampleRate, nanosForBuffering );

			dataRingBuffer = new BackendToFrontendDataRingBuffer( maxRingBufferingInSamples );
			dataRingBuffer.backEndClearNumSamplesQueued();

			numSamplesPerFrontEndPeriod = timingParameters.getSampleFramesPerFrontEndPeriod();

		}
		catch (final Exception e)
		{
			throw new MadProcessingException( e );
		}
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final int U_periodStartTimestamp ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames  )
	{
		final boolean inConnected = channelConnectedFlags.get( SpectralRollerMadDefinition.CONSUMER_AUDIO_SIGNAL0 );
		final MadChannelBuffer inChannelBuffer = channelBuffers[ SpectralRollerMadDefinition.CONSUMER_AUDIO_SIGNAL0 ];
		final float[] in0Floats = (inConnected ? inChannelBuffer.floatBuffer : null );

		if( active && inConnected )
		{
			int curSampleIndex = 0;
			while( curSampleIndex < numFrames )
			{
				final int U_timestampForIndexUpdate = U_periodStartTimestamp + curSampleIndex;

				if( dataRingBuffer.backEndGetNumSamplesQueued() >= numSamplesPerFrontEndPeriod )
				{
					queueWriteIndexUpdate( tempQueueEntryStorage,
							0,
							dataRingBuffer.getWritePosition(),
							U_timestampForIndexUpdate );
					dataRingBuffer.backEndClearNumSamplesQueued();
				}

				final int numLeft = numSamplesPerFrontEndPeriod - dataRingBuffer.backEndGetNumSamplesQueued();

				final int numAvailable = numFrames - curSampleIndex;
				final int numThisRound = ( numLeft > numAvailable ? numAvailable : numLeft );

				final int spaceAvailable = dataRingBuffer.getNumWriteable();

				final int numToWrite = ( spaceAvailable > numThisRound ? numThisRound : spaceAvailable );
				if( numToWrite > 0 )
				{
					dataRingBuffer.backEndWrite( in0Floats, frameOffset + curSampleIndex, numToWrite );
				}
				else
				{
//							log.error("Ring full?");
//							log.debug("Available: " + spaceAvailable + " and writing " + numToWrite + " samples to ring[" + curSampleIndex + "]");
					break;
				}
				curSampleIndex += numThisRound;
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	private void queueWriteIndexUpdate(final ThreadSpecificTemporaryEventStorage tses,
			final int dataChannelNum,
			final int writePosition,
			final int U_frameTime)
	{
		final long joinedParts = ((long)writePosition << 32 ) | (dataChannelNum);

		localBridge.queueTemporalEventToUi( tses,
				U_frameTime,
				SpectralRollerIOQueueBridge.COMMAND_OUT_RINGBUFFER_WRITE_INDEX,
				joinedParts,
				null );
	}

	public BackendToFrontendDataRingBuffer getDataRingBuffer()
	{
		return dataRingBuffer;
	}
}