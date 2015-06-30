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



public class LogarithmicDbAmpScaleComputer implements AmpScaleComputer
{
//	private static Log log = LogFactory.getLog( LogarithmicAmpScaleComputer.class.getName() );

	private final static float SCALE_FACTOR = 9.08f;

//	public final static float MIN_DB_VALUE = -80.0f;
//	public final static float MIN_DB_VALUE = -96.0f;
//	public final static float MIN_DB_VALUE = -100.0f;
	public final static float MIN_DB_VALUE = -120.0f;
	public final static float MIN_RAW_VALUE = AudioMath.dbToLevelF( MIN_DB_VALUE );

	@Override
	public float scaleIt(final float valForBin)
	{
		float val = (valForBin * 10.0f) + 1.0f;
		val = (float)Math.log( val );
		val = val / SCALE_FACTOR;
		return val;
	}

	@Override
	public int rawToMappedBucket( final int numBuckets, final float maxValue, final float rawValue )
	{
		if( rawValue >= maxValue )
		{
			return (numBuckets-1);
		}
		else
		{
			final float asDb = AudioMath.levelToDbF( rawValue );

			if( asDb < MIN_DB_VALUE )
			{
				return 0;
			}
			else
			{
				final float maxDbValue = AudioMath.levelToDbF( maxValue );

				final float normalisedValue = (asDb - MIN_DB_VALUE) / (maxDbValue-MIN_DB_VALUE );

				return 1 + Math.round( normalisedValue * (numBuckets-2) );
			}
		}
	}

	@Override
	public float mappedBucketToRaw( final int numBuckets, final float maxValue, final int bucket )
	{
		float dbValueInBucket;
		if( bucket == 0 )
		{
			return 0.0f;
		}
		else
		{
			final float normalisedValue = (bucket-1) / (float)(numBuckets-2);

			final float maxDbValue = AudioMath.levelToDbF( maxValue );

			dbValueInBucket = MIN_DB_VALUE + (normalisedValue * (maxDbValue-MIN_DB_VALUE));
			final float finalValue = AudioMath.dbToLevelF( dbValueInBucket );
			return finalValue;
		}
	}

}