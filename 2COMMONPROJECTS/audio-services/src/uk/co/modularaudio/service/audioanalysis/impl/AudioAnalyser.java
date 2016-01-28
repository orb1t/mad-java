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

package uk.co.modularaudio.service.audioanalysis.impl;

import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.hashedstorage.HashedRef;
import uk.co.modularaudio.util.audio.format.DataRate;

public interface AudioAnalyser
{
	void dataStart( DataRate dataRate, int numChannels, long totalFrames, int maxFramesPerCall ) throws AnalysisException;
	void receiveFrames( float[] data, int numFrames ) throws AnalysisException;
	void dataEnd( AnalysisContext context, AnalysedData analysedData, HashedRef hashedRef ) throws AnalysisException;
	void completeAnalysis( AnalysisContext context, AnalysedData analysedData, HashedRef hashedRef ) throws AnalysisException;

}
