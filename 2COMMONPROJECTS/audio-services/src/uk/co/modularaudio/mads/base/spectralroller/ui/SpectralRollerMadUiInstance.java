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

package uk.co.modularaudio.mads.base.spectralroller.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.spectralroller.mu.SpectralRollerIOQueueBridge;
import uk.co.modularaudio.mads.base.spectralroller.mu.SpectralRollerMadDefinition;
import uk.co.modularaudio.mads.base.spectralroller.mu.SpectralRollerMadInstance;
import uk.co.modularaudio.mads.base.spectralroller.ui.SpectralRollerScaleLimitComboUiJComponent.AmpScale;
import uk.co.modularaudio.util.audio.controlinterpolation.CDLowPass24Interpolator;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LogarithmicTimeMillis1To5000SliderModel;

public class SpectralRollerMadUiInstance extends AbstractNoNameChangeNonConfigurableMadUiInstance<SpectralRollerMadDefinition, SpectralRollerMadInstance>
	implements IOQueueEventUiConsumer<SpectralRollerMadInstance>
{
	private static Log log = LogFactory.getLog( SpectralRollerMadUiInstance.class.getName() );

	// Maximum to buffer in entirety is five seconds
	public static final float MAX_CAPTURE_MILLIS = 5000.0f;

	private SpectralRollerDisplayUiJComponent uiDisplay = null;
	private FrontEndRingHandler uiRingHandler = null;

	private final List<ScaleLimitChangeListener> scaleChangeListeners = new ArrayList<ScaleLimitChangeListener>();

	private float desiredAmpScaleLimitDb = 0.0f;

	private float lastReceivedCaptureMillis = LogarithmicTimeMillis1To5000SliderModel.DEFAULT_MILLIS;
	private float lastSetCaptureMillis = lastReceivedCaptureMillis;

	private final CDLowPass24Interpolator captureTimeInterpolator = new CDLowPass24Interpolator();

	public SpectralRollerMadUiInstance( final SpectralRollerMadInstance instance,
			final SpectralRollerMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
		captureTimeInterpolator.resetLowerUpperBounds( LogarithmicTimeMillis1To5000SliderModel.MIN_MILLIS,
				LogarithmicTimeMillis1To5000SliderModel.MAX_MILLIS );
	}

	@Override
	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
	{
		super.receiveStartup(ratesAndLatency, timingParameters, frameTimeFactory);

		// Has a nice smoothing effect
		captureTimeInterpolator.resetSampleRateAndPeriod( 2000, 0, 0 );
		captureTimeInterpolator.hardSetValue( lastReceivedCaptureMillis );

		uiRingHandler.receiveStartup( ratesAndLatency, timingParameters );
		uiDisplay.receiveStartup( ratesAndLatency, timingParameters );
		uiDisplay.setCaptureTimeMillis( lastReceivedCaptureMillis );
	}

	@Override
	public void receiveStop()
	{
		super.receiveStop();

		uiDisplay.receiveStop();
		uiRingHandler.receiveStop();
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final int U_currentGuiTime,
			final int framesSinceLastTick )
	{
		// Process messages before we pass the tick to the controls (and thus the display)
		localQueueBridge.receiveQueuedEventsToUi( tempEventStorage, instance, this );

		super.doDisplayProcessing( tempEventStorage, timingParameters, U_currentGuiTime, framesSinceLastTick );

		final float[] tmpCaptureMillisArray = new float[1];
		captureTimeInterpolator.generateControlValues( tmpCaptureMillisArray, 0, 1 );
		captureTimeInterpolator.checkForDenormal();
		final float lastCalculatedMillis = tmpCaptureMillisArray[0];
		if( lastCalculatedMillis != lastSetCaptureMillis )
		{
			uiDisplay.setCaptureTimeMillis( lastCalculatedMillis );
			lastSetCaptureMillis = lastCalculatedMillis;
		}
	}

	@Override
	public void consumeQueueEntry( final SpectralRollerMadInstance instance,
			final IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			case SpectralRollerIOQueueBridge.COMMAND_OUT_RINGBUFFER_WRITE_INDEX:
			{
				final long value = nextOutgoingEntry.value;
				final int bufferNum = (int)((value ) & 0xFFFFFFFF);
				final int ringBufferIndex = (int)((value >> 32 ) & 0xFFFFFFFF);

				if( bufferNum == 0 )
				{
					uiRingHandler.receiveBufferIndexUpdate( nextOutgoingEntry.U_frameTime, ringBufferIndex );
				}
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unknown output command from MI: " + nextOutgoingEntry.command );
				}
				break;
			}
		}
	}

	public void setCaptureTime( final float captureMillis )
	{
		this.lastReceivedCaptureMillis = captureMillis;
		captureTimeInterpolator.notifyOfNewValue( lastReceivedCaptureMillis );
	}

	public void sendUiActive( final boolean active )
	{
		sendTemporalValueToInstance( SpectralRollerIOQueueBridge.COMMAND_IN_ACTIVE, ( active ? 1 : 0 ) );
	}

	public void addScaleChangeListener( final ScaleLimitChangeListener scl )
	{
		this.scaleChangeListeners.add( scl );
		scl.receiveScaleLimitChange( desiredAmpScaleLimitDb );
	}

	public void setDesiredAmpScaleLimit( final AmpScale ws )
	{
		desiredAmpScaleLimitDb = ws.getDb();
		for( final ScaleLimitChangeListener scl : scaleChangeListeners )
		{
			scl.receiveScaleLimitChange( desiredAmpScaleLimitDb );
		}
	}

	protected void setUiDisplay( final SpectralRollerDisplayUiJComponent uiDisplay )
	{
		this.uiDisplay = uiDisplay;
		this.uiRingHandler = uiDisplay.getUiRingHandler();
	}
}