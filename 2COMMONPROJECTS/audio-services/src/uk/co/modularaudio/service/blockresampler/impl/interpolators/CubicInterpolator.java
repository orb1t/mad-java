package uk.co.modularaudio.service.blockresampler.impl.interpolators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.co.modularaudio.service.blockresampler.BlockResamplerService;


public class CubicInterpolator implements Interpolator
{
	private static Log log = LogFactory.getLog( CubicInterpolator.class.getName() );

	@Override
	public float interpolate( final float[] sourceBuffer, final int pos, final float frac )
	{
		final float y0 = sourceBuffer[ pos - 1];
		final float y1 = sourceBuffer[ pos ];
		final float y2 = sourceBuffer[ pos + 1 ];
		final float y3 = sourceBuffer[ pos + 2 ];
//		log.debug("CubicInterpolate between y0(" + y0 + ") y1(" + y1 + ") y2(" + y2 + ") y3(" + y3 + ")");

		if( Math.abs(y0) >= BlockResamplerService.EXCESSIVE_FLOAT ||
				Math.abs(y1) >= BlockResamplerService.EXCESSIVE_FLOAT ||
				Math.abs(y2) >= BlockResamplerService.EXCESSIVE_FLOAT ||
				Math.abs(y3) >= BlockResamplerService.EXCESSIVE_FLOAT )
		{
			log.error("Failed on frame " + pos + " with vals " + y0 +
					" " + y1 +
					" " + y2 +
					" " + y3 );
		}

		final float fracSq = frac * frac;

//		float a0 = y3 - y2 - y0 + y1;
//		float a1 = y0 - y1 - a0;
//		float a2 = y2 - y0;
//		float a3 = y1;

		final float a0 = -0.5f*y0 + 1.5f*y1 - 1.5f*y2 + 0.5f*y3;
		final float a1 = y0 - 2.5f*y1 + 2.0f*y2 - 0.5f*y3;
		final float a2 = -0.5f*y0 + 0.5f*y2;
		final float a3 = y1;


		final float result = (a0 * frac * fracSq) + (a1 * fracSq) + (a2 * frac) + a3;

//		if( Math.abs(result) > 0.85f )
//		{
//			log.debug("Dicky value?");
//			final float[] debugArray = new float[6];
//			System.arraycopy( sourceBuffer, pos - 2, debugArray, 0, 6 );
//			log.debug("The six around where we are: " + Arrays.toString( debugArray ) );
//		}

		return result;
	}
}
