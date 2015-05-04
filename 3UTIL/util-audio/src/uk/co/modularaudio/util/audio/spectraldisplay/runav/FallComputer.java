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

package uk.co.modularaudio.util.audio.spectraldisplay.runav;


public class FallComputer implements RunningAverageComputer
{
	@Override
	public void computeNewRunningAverages( final int currentNumBins, final float[] newValues, final float[] runningValues)
	{
		for( int i = 0 ; i < currentNumBins ; i++ )
		{
			final float valToAdd = newValues[i];
			final float curValue = runningValues[i];
			if( valToAdd < curValue )
			{
				runningValues[i] = (curValue * 0.95f) + (valToAdd * 0.05f);
			}
			else
			{
				runningValues[i] = valToAdd;
			}
		}
	}
}
