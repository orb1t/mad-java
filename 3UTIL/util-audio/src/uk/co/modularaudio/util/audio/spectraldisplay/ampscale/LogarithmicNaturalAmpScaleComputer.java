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

package uk.co.modularaudio.util.audio.spectraldisplay.ampscale;

import uk.co.modularaudio.util.audio.math.AudioMath;



public class LogarithmicNaturalAmpScaleComputer implements AmpScaleComputer
{
//	private static Log log = LogFactory.getLog( LogarithmicAmpScaleComputer.class.getName() );

	private final static float SCALE_FACTOR = 9.08f;

	public final static float MIN_DB_VALUE = -120.0f;
	public final static float MIN_RAW_VALUE = AudioMath.dbToLevelF( MIN_DB_VALUE );
	public final static float MIN_NATURAL_VALUE = levelToNatural( MIN_RAW_VALUE );

//	private static final float PRE_LOG_SCALE_FACTOR = 10.0f * 512.0f;
//	private static final float PRE_LOG_SCALE_FACTOR = 10.0f * 512.0f;
//	private static final float PRE_LOG_SCALE_FACTOR = 10.0f * 200.0f;
//	private static final float PRE_LOG_SCALE_FACTOR = 10.0f * 150.0f;
	private static final float PRE_LOG_SCALE_FACTOR = 10.0f * 100.0f;

	@Override
	public float scaleIt(final float valForBin)
	{
		float val = (valForBin * 10.0f) + 1.0f;
		val = (float)Math.log( val );
		val = val / SCALE_FACTOR;
		return val;
	}

	private final static float levelToNatural( final float rawValue )
	{
		final float preLog = (rawValue * PRE_LOG_SCALE_FACTOR) + 1.0f;
		final float theLog = (float)Math.log(preLog);
		return theLog / SCALE_FACTOR;
	}

	private final static float naturalToLevel( final float natural )
	{
		final float preExp = natural * SCALE_FACTOR;
		final float postExp = (float)Math.exp( preExp );
		return (postExp - 1.0f) / PRE_LOG_SCALE_FACTOR;
	}

	private int lastBucketIndex = 200;
	private float minDb = -96.0f;
	private float minValue = AudioMath.dbToLevelF( minDb );
	private float minNaturalValue = levelToNatural( minValue );
	private float maxDb = 0.0f;
	private float maxValue = AudioMath.dbToLevelF( maxDb );
	private float maxNaturalValue = levelToNatural( maxValue );

	private float rangeNatural = maxNaturalValue - minNaturalValue;

	@Override
	public void setParameters( final int numBuckets,
			final float minValueDb,
			final float maxValueDb )
	{
		this.lastBucketIndex = numBuckets - 1;
		this.minDb = minValueDb;
		minValue = AudioMath.dbToLevelF( minDb );
		minNaturalValue = levelToNatural( minValue );
		this.maxDb = maxValueDb;
		maxValue = AudioMath.dbToLevelF( maxDb );
		maxNaturalValue = levelToNatural( maxValue );
		rangeNatural = maxNaturalValue - minNaturalValue;
	}

	@Override
	public final int normalisedRawToMappedBucket( final float rawValue )
	{
		if( rawValue >= maxValue )
		{
			return lastBucketIndex;
		}
		else if( rawValue <= minValue )
		{
			return 0;
		}
		else
		{
			final float asNatural = levelToNatural( rawValue );
			final float normalisedValue = (asNatural - minNaturalValue) / rangeNatural;

			return (int)( (normalisedValue * lastBucketIndex) + 0.5f );
		}
	}

	@Override
	public final float mappedBucketToNormalisedRaw( final int bucket )
	{
		if( bucket == 0 )
		{
			return minValue;
		}
		else if( bucket == lastBucketIndex )
		{
			return maxValue;
		}
		else
		{
			final float normalisedValue = bucket / (float)lastBucketIndex;

			final float naturalValueInBucket = minNaturalValue + (normalisedValue * rangeNatural);
			final float finalValue = naturalToLevel( naturalValueInBucket );
			return finalValue;
		}
	}
}
