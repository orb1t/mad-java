package test.uk.co.modularaudio.util.math;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import junit.framework.TestCase;
import uk.co.modularaudio.util.math.FastMath;
import uk.co.modularaudio.util.math.MathDefines;
import uk.co.modularaudio.util.math.MathFormatter;

public class TestFastSine extends TestCase
{
	private final static Log LOG = LogFactory.getLog( TestFastSine.class );

	public TestFastSine()
	{
	}

	@Test
	public void testCheckFastSine()
	{
		LOG.info( "Got here" );

		final float[] sourceVals = new float[] {
				0.0f,
				MathDefines.HALF_PI_F,
				MathDefines.TWO_PI_F,
				-MathDefines.HALF_PI_F,
				(float)Math.PI
		};

		for( float sf : sourceVals )
		{
			while( sf < -MathDefines.ONE_PI_F )
			{
				sf += MathDefines.TWO_PI_F;
			}

			while( sf > MathDefines.ONE_PI_F )
			{
				sf -= MathDefines.TWO_PI_F;
			}

			final float ssf = (float)Math.sin( sf );
			final float assf = FastMath.fastSinApprox( sf );

			LOG.info( "Source: " + MathFormatter.slowFloatPrint( sf, 10, true ) +
					" Real: " + MathFormatter.slowFloatPrint( ssf, 10, true ) +
					" Approx: " + MathFormatter.slowFloatPrint( assf, 10, true ));
		}
	}
}
