package uk.co.modularaudio.service.apprendering.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.apprendering.util.AppRenderingJobQueue;
import uk.co.modularaudio.service.apprendering.util.AppRenderingStructure;
import uk.co.modularaudio.service.apprendering.util.jobqueue.HotspotClockSourceJobQueueHelperThread;
import uk.co.modularaudio.service.apprendering.util.jobqueue.STRenderingJobQueue;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.renderingplan.RenderingPlan;
import uk.co.modularaudio.service.renderingplan.RenderingPlanService;
import uk.co.modularaudio.service.timing.TimingService;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class HotspotRenderingAppStructure extends AppRenderingStructure implements HotspotRenderingContainer
{
	private static Log log = LogFactory.getLog( HotspotRenderingAppStructure.class.getName() );

	private HotspotClockSourceJobQueueHelperThread hotspotClockSourceThread;

	public HotspotRenderingAppStructure( final MadComponentService componentService,
			final MadGraphService graphService,
			final RenderingPlanService renderingService,
			final TimingService timingService,
			final boolean shouldProfileRenderingJobs,
			final int maxWaitForTransitionMillis,
			final RenderingPlan renderingPlan )
		throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		super( componentService,
				graphService,
				renderingService,
				0,
				shouldProfileRenderingJobs,
				maxWaitForTransitionMillis );
		hotspotClockSourceThread = new HotspotClockSourceJobQueueHelperThread( renderingPlan,
				renderingService,
				timingService,
				new STRenderingJobQueue( AppRenderingJobQueue.RENDERING_JOB_QUEUE_CAPACITY ),
				shouldProfileRenderingJobs );
	}

	@Override
	public void startHotspotLooping()
	{
		hotspotClockSourceThread.start();
	}

	@Override
	public void stopHotspotLooping()
	{
		// Let it exit gracefully
		try
		{
			hotspotClockSourceThread.halt();
			Thread.sleep( 200 );
		}
		catch(final InterruptedException ie )
		{
		}
		if( hotspotClockSourceThread.isAlive() )
		{
			hotspotClockSourceThread.forceHalt();
		}
		try
		{
			hotspotClockSourceThread.join();
		}
		catch (final InterruptedException ie)
		{
			final String msg = "Interrupted waiting for join to hotspot thread: " + ie.toString();
			log.error( msg, ie );
		}
		hotspotClockSourceThread = null;
	}
}
