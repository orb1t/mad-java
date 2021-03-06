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

package test.uk.co.modularaudio.util.audio.stft.frame.synthesis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.stft.frame.synthesis.StftFrameSynthesisStep;

public class TestSynthStepCalculations
{
	private static Log log = LogFactory.getLog( TestSynthStepCalculations.class.getName() );
	
	public TestSynthStepCalculations()
	{
	}
	
	public void go() throws Exception
	{
		StftFrameSynthesisStep fss = new StftFrameSynthesisStep();
		fss.calculate( 1.0, 1.0, 512 );
		log.debug( fss );
		fss.calculate( 0.5, 1.0, 512 );
		log.debug( fss );
		fss.calculate( 1.0, 0.5, 512 );
		log.debug( fss );
		fss.calculate( 1.0, 2.0, 512 );
		log.debug( fss );
		fss.calculate( 0.5, 2.0, 512 );
		log.debug( fss );
		fss.calculate( 2.0, 1.0, 512 );
		log.debug( fss );
		fss.calculate( 0.5, 0.5, 512 );
		log.debug( fss );
		fss.calculate( 0.75, 0.75, 512 );
		log.debug( fss );
		fss.calculate( 1.25, 0.75, 512 );
		log.debug( fss );
		fss.calculate( 1.5, 0.70, 512 );
		log.debug( fss );
	}
	
	public static void main( String[] args )
		throws Exception
	{
		TestSynthStepCalculations t = new TestSynthStepCalculations();
		t.go();
	}

}
