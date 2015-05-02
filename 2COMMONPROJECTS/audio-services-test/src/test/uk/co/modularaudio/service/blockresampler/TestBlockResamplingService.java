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

package test.uk.co.modularaudio.service.blockresampler;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.service.samplecaching.BufferFillCompletionListener;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.impl.SampleCachingServiceImpl;
import uk.co.modularaudio.util.audio.floatblockpool.BlockBufferingConfiguration;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.springhibernate.SpringHibernateContextHelper;

public class TestBlockResamplingService extends TestCase
{
	public static Log log = LogFactory.getLog( TestBlockResamplingService.class.getName() );

//	private final static String testFile1 = "/music/Samples/House/VocalStabs/comeon.flac";
//	private final static String testFile2 = "/music/Samples/House/VocalStabs/haha.flac";
//	private final static String testFile2 = "/music/Mp3Repository/TestForPac/hc.wav";
	// First one should be a wav so the forward skipping works
	private final static String testFile1 = "/home/dan/Music/20131121/4823846_Bengala_Joel_Mull_Remix.wav";
	private final static String testFile2 = "/home/dan/SetSources/200808/146914_We_Share_Our_Mothers_Health_Original_Mix.mp3";
	private SpringComponentHelper sch;
	private GenericApplicationContext gac;

	private AdvancedComponentsFrontController frontController;
	private SampleCachingServiceImpl scsi;
	private BlockBufferingConfiguration bbc;

	@Override
	protected void setUp() throws Exception
	{
		final List<SpringContextHelper> clientHelpers = new ArrayList<SpringContextHelper>();
		clientHelpers.add( new SpringHibernateContextHelper() ) ;
		clientHelpers.add( new PostInitPreShutdownContextHelper() );
		sch = new SpringComponentHelper( clientHelpers );
		gac = sch.makeAppContext();

		frontController = gac.getBean( AdvancedComponentsFrontController.class );
		scsi = gac.getBean( SampleCachingServiceImpl.class );
		bbc = scsi.getBlockBufferingConfiguration();
	}

	@Override
	protected void tearDown() throws Exception
	{
		gac.close();
	}

	public void testReadingAFile() throws Exception
	{
		log.debug( "Will attempt to read a file from start to end." );

		final int blockLengthInFloats = bbc.blockLengthInFloats;
		final int numChannels = 2;

		final int numFloatsToRead = (bbc.blockLengthInFloats * 2) + 20;
		final int numFramesToRead = numFloatsToRead / numChannels;

		final float[] outputFrameFloats = new float[ numFloatsToRead ];

//		int outputFrameIndex = 0;
//		int numOutputFrames = 32;
//		float playbackSpeed = 1.0f;

		final SampleCacheClient scc1 = frontController.registerCacheClientForFile( testFile1 );
		SampleCacheClient scc2 = frontController.registerCacheClientForFile( testFile2 );

		// Read a block with loads of zeros
		scc1.setCurrentFramePosition(-(bbc.blockLengthInFloats * 2) - 20);
		scsi.readSamplesForCacheClient(scc1, outputFrameFloats, 0, numFramesToRead );

		scc1.setCurrentFramePosition( scc1.getTotalNumFrames() - 20 );

		scsi.registerForBufferFillCompletion(scc1, new BufferFillCompletionListener()
		{

			@Override
			public void notifyBufferFilled(final SampleCacheClient sampleCacheClient)
			{
				// Don't do anything we're using this to trigger a scan of the cache clients
			}
		});

		scsi.readSamplesForCacheClient( scc1,  outputFrameFloats,  0,  numFramesToRead );

		// Read the first block with some leading zeros
		scc1.setCurrentFramePosition( -10 );
		scsi.readSamplesForCacheClient( scc1,  outputFrameFloats,  0,  numFramesToRead );

		// And back (should free the block up)
		final long readFramePosition = (bbc.blockLengthInFloats * 2 + 40) / numChannels;
		scc2.setCurrentFramePosition( readFramePosition );

		scsi.readSamplesForCacheClient( scc2,  outputFrameFloats, 0, numFramesToRead );

		// Just over a boundary
		scc2.setCurrentFramePosition( (blockLengthInFloats * 2) / numChannels );

		scsi.readSamplesForCacheClient( scc2,  outputFrameFloats, 0, numFramesToRead );

		frontController.unregisterCacheClientForFile( scc1 );
		frontController.unregisterCacheClientForFile( scc2 );

		scc2 = frontController.registerCacheClientForFile( testFile2 );

		frontController.unregisterCacheClientForFile( scc2 );
		log.debug( "All done" );
	}
}
