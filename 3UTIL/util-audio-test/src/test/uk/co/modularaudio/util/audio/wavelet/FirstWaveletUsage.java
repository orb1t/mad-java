/**
 *
 * Copyright (C) 2015 -> 2018 - Daniel Hams, Modular Audio Limited
 *                              daniel.hams@gmail.com
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

package test.uk.co.modularaudio.util.audio.wavelet;

import jwave.exceptions.JWaveException;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.Wavelet;
import jwave.transforms.wavelets.daubechies.Daubechies4;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.oscillatortable.Oscillator;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactory;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorInterpolationType;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveTableType;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.timing.NanosTimestampFormatter;

public class FirstWaveletUsage
{
	private final static Log log = LogFactory.getLog( FirstWaveletUsage.class );

	private final static String CACHE_ROOT = "wavetablecache";

	private final static float OSC_FREQ = 60;
	private final static int NUM_SAMPLES = 4096;
	private final static int SAMPLE_RATE = 48000;
//	private final static int SAMPLE_RATE = 10 * 1000;
//	private final static int NUM_PASSES = 1;
	private final static int NUM_PASSES = 100;

//	private final static float OSC_AMP = 0.5f;

	public FirstWaveletUsage()
	{
	}

	public double[] go(final FastWaveletTransform t, final double[] sampleArray ) throws Exception, JWaveException
	{
//		log.debug( "And we're off" );

//		final double[][] result = t.decompose( sampleArray );

		final double[] result = t.forward( sampleArray );

//		for( int l = 0 ; l < result.length ; ++l )
//		{
//			log.debug( "Level " + l + " result is " + resultArrayToString( result[l] ) );
//		log.debug( "Result is " + resultArrayToString( result ) );
//		}
		return result;
	}

	private static String resultArrayToString( final double[] ds )
	{
		final StringBuilder sb = new StringBuilder(256);
		sb.append( '[' );
		sb.append( MathFormatter.slowDoublePrint( ds[0], 4, true )  );
		for( int i = 1 ; i < ds.length ; ++i )
		{
			sb.append( ", " );
			sb.append( MathFormatter.slowDoublePrint( ds[i], 4, true )  );
		}
		sb.append(']');

		return sb.toString();
	}

	public static void main( final String[] args ) throws Exception, JWaveException
	{
		final OscillatorFactory of = OscillatorFactory.getInstance( CACHE_ROOT );

		final Oscillator o = of.createOscillator( OscillatorWaveTableType.BAND_LIMITED,
				OscillatorInterpolationType.CUBIC,
				OscillatorWaveShape.SINE );

		final float[] tmpOutput = new float[NUM_SAMPLES];
		final double[] sampleArray = new double[NUM_SAMPLES];

//		final Wavelet wavelet = new Daubechies2();
		final Wavelet wavelet = new Daubechies4();
		final FastWaveletTransform fwt = new FastWaveletTransform( wavelet );

//		o.oscillate( OSC_FREQ, (float)Math.PI/4, 1.0f, tmpOutput, 0, NUM_SAMPLES, SAMPLE_RATE );
		o.oscillate( OSC_FREQ, 0.0f, 1.0f, tmpOutput, 0, NUM_SAMPLES, SAMPLE_RATE );

		for( int i = 0 ; i < NUM_SAMPLES ; ++i )
		{
			sampleArray[i] = tmpOutput[i] * 0.5f;
		}

		final double[] firstTwenty = new double[20];
		final FirstWaveletUsage t = new FirstWaveletUsage();
		for( int i = 0 ; i < NUM_PASSES ; ++i )
		{
			final long tb = System.nanoTime();
			final double[] result = t.go( fwt, sampleArray );
			final long ta = System.nanoTime();
			final long diff = ta-tb;
			final String diffStr = NanosTimestampFormatter.formatTimestampForLogging( diff, false );
			if( i % 10 == 0 )
			{
				log.info( "Pass took " + diffStr + " nanos");
				System.arraycopy( result, 0, firstTwenty, 0, 20 );
				log.debug( "First twenty in result is " + resultArrayToString( firstTwenty ) );
			}
		}
	}

}