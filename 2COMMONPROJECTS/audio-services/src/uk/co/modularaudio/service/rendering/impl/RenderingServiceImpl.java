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

package uk.co.modularaudio.service.rendering.impl;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.rendering.AbstractParallelRenderingJob;
import uk.co.modularaudio.service.rendering.RenderingPlan;
import uk.co.modularaudio.service.rendering.RenderingService;
import uk.co.modularaudio.service.rendering.impl.flatgraph.DirectedDependencyGraph;
import uk.co.modularaudio.service.rendering.impl.flatgraph.DirectedDependencyGraphHelper;
import uk.co.modularaudio.service.rendering.impl.flatgraph.FlattenedRenderJob;
import uk.co.modularaudio.service.rendering.impl.rpdump.Dumper;
import uk.co.modularaudio.service.timing.TimingService;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingSource;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;


public class RenderingServiceImpl implements ComponentWithLifecycle, RenderingService
{
	private static Log log = LogFactory.getLog( RenderingServiceImpl.class.getName());

	private MadGraphService graphService;
	private TimingService timingService;

	@Override
	public RenderingPlanWithFanAndSync createRenderingPlan( final MadGraphInstance<?,?> graph,
		final HardwareIOChannelSettings hardwareChannelSettings,
		final MadFrameTimeFactory frameTimeFactory )
			throws DatastoreException
	{
		RenderingPlanWithFanAndSync retVal = null;

		try
		{
			// Remove subgraphs by recursively moving their contents 'up' until we have no more subgraphs
			final MadGraphInstance<?,?> zeroDepthGraph = graphService.flattenGraph( graph );

			// First step - create a directed graph of "renderjob" that specifies jobs it waits for and
			// jobs that wait for it
			final DirectedDependencyGraph dependencyGraph = DirectedDependencyGraphHelper.buildDirectedDependencyGraph( graphService, zeroDepthGraph );
			// Now recurse over the graph adding a "cardinality" to each node where the leaves
			// start at one and parents have max(child_card) + 1
			DirectedDependencyGraphHelper.annotateDependencyGraph( dependencyGraph );

			// Finally now we have the cardinality we can order them and create rendering jobs
			// based on it along with dependency information
			final MadTimingSource timingSource = timingService.getTimingSource();

			retVal = annotatedDependencyGraphToRenderingPlan( dependencyGraph,
					zeroDepthGraph,
					hardwareChannelSettings,
					frameTimeFactory,
					timingSource );

			graphService.destroyGraph( zeroDepthGraph, true, false );
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught creating render plan from graph: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}

		return retVal;
	}

	protected void fillInBuffersAndConnectedFlags( final HardwareIOChannelSettings planHardwareChannelSettings,
			final MadRenderingJob renderingJob,
			final MadGraphInstance<?,?> graph,
			final Map<MadChannelInstance, MadChannelBuffer> channelInstanceToBufferMap,
			final Set<MadChannelBuffer> allChannelBuffersSet )
		throws DatastoreException, RecordNotFoundException, MadProcessingException
	{
		final MadInstance<?,?> madInstance = renderingJob.getMadInstance();
		final MadChannelInstance[] allAuChannelInstances = madInstance.getChannelInstances();
		final MadChannelConnectedFlags channelActiveBitset = renderingJob.getChannelConnectedFlags();
		// Should set the size of the bitset here
		//channelActiveBitset.setSize( allAuChannelInstances.size() );
		final MadChannelBuffer[] channelBufferArray = renderingJob.getChannelBuffers();

		// Now loop around consumer and producer links to this component, filling in as necessary
		final Set<MadLink> producerLinks = graphService.findAllLinksFromInstance( graph, madInstance );
		for( final MadLink link : producerLinks )
		{
			final MadChannelInstance producerChannelInstance = link.getProducerChannelInstance();
			if( channelInstanceToBufferMap.containsKey( producerChannelInstance ) )
			{
				// Already filled in
				continue;
			}
			final MadInstance<?,?> producerInstance = producerChannelInstance.instance;
			assert( producerInstance == madInstance );

			final int channelIndex = madInstance.getChannelInstanceIndex( producerChannelInstance );

			channelActiveBitset.set( channelIndex );

			final MadChannelType channelType = producerChannelInstance.definition.type;
			final MadChannelBuffer buf = new MadChannelBuffer( channelType,
					planHardwareChannelSettings.getChannelBufferLengthForChannelType( channelType ) );
			channelBufferArray[ channelIndex ] = buf;
			allChannelBuffersSet.add( buf );
			channelInstanceToBufferMap.put( producerChannelInstance, buf );
//			log.debug("Created buffer for " + producerInstance.toString() + " channel instance: " + producerChannelInstance.toString());
		}

		final Set<MadLink> consumerLinks = graphService.findAllLinksToInstance( graph, madInstance );

		for( final MadLink link : consumerLinks )
		{
			final MadChannelInstance consumerChannelInstance = link.getConsumerChannelInstance();
			final MadInstance<?,?> consumerInstance = link.getConsumerChannelInstance().instance;
			assert( consumerInstance == madInstance );

			final int channelIndex = madInstance.getChannelInstanceIndex( consumerChannelInstance );

			channelActiveBitset.set( channelIndex );

			// Now we need to go find the channel buffer that should already be defined for the other end of this link
			final MadChannelInstance producerChannelInstance = link.getProducerChannelInstance();
			final MadChannelBuffer producerBuffer = channelInstanceToBufferMap.get( producerChannelInstance );
			if( producerBuffer == null )
			{
				throw new DatastoreException( "Unable to find already initialised produce buffer for link up to consumer" );
			}
			else
			{
//				MadInstance<?,?> producerInstance = producerChannelInstance.instance;
//				log.debug("Using predefined buffer of " + producerInstance + " - " +
//						producerChannelInstance.toString() );
				channelBufferArray[ channelIndex ] = producerBuffer;
			}
		}

		// Finally loop over all the channels filling in empty buffers for channels that are not filled.
		for( int i = 0 ; i < allAuChannelInstances.length ; i++ )
		{
			final MadChannelInstance auci = allAuChannelInstances[i];
			if( channelBufferArray[i] == null )
			{
				final MadChannelType channelType = auci.definition.type;
				final MadChannelBuffer buf = new MadChannelBuffer( channelType,
						planHardwareChannelSettings.getChannelBufferLengthForChannelType( channelType ) );
				channelBufferArray[ i ] = buf;
				allChannelBuffersSet.add( buf );
				channelInstanceToBufferMap.put( auci, buf );
//				log.debug("Creating unconnected buffer of " + auci.instance + " - " +
//						auci.toString() );
			}
		}
	}

	protected RenderingPlanWithFanAndSync annotatedDependencyGraphToRenderingPlan( final DirectedDependencyGraph annotatedGraph,
			final MadGraphInstance<?,?> graph,
			final HardwareIOChannelSettings planHardwareSettings,
			final MadFrameTimeFactory planFrameTimeFactory,
			final MadTimingSource timingSource )
		throws DatastoreException, RecordNotFoundException, MadProcessingException
	{
		// Sort all of the jobs into cardinality order - we start from producers
		// and work down the graph
		final List<FlattenedRenderJob> allFlattenedJobs = annotatedGraph.getJobs();
//		Collections.sort( allFlattenedJobs, Collections.reverseOrder());
		Collections.sort( allFlattenedJobs );

		final Map<FlattenedRenderJob, MadParallelRenderingJob> flatToParallelJobMap = new HashMap<FlattenedRenderJob, MadParallelRenderingJob>();
		// Places we keep track of all, initial and final jobs
		// As we want to insert a FAN job at the front, and a SYNC job at the end.
		final Set<AbstractParallelRenderingJob> allJobSet = new HashSet<AbstractParallelRenderingJob>();
		final Set<MadParallelRenderingJob> initialAuJobSet = new HashSet<MadParallelRenderingJob>();
		final Set<MadParallelRenderingJob> finalAuJobSet = new HashSet<MadParallelRenderingJob>();
		// We must update the final producersWeWaitFor of the final sync job after we know how many

		final FinalSyncParallelRenderingJob finalSyncPrj = new FinalSyncParallelRenderingJob();
		allJobSet.add( finalSyncPrj );
		final Set<MadInstance<?,?>> allMadInstancesSet = new HashSet<MadInstance<?,?>>();
		final Set<MadChannelBuffer> allChannelBuffersSet = new HashSet<MadChannelBuffer>();
		int totalNumJobs = 0;

		final Map<MadChannelInstance, MadChannelBuffer> channelInstanceToBufferMap =
				new HashMap<MadChannelInstance, MadChannelBuffer>();

		// First pass - create entries in the flat to parallel job map for all jobs
		// and fill in the necessary buffers
		for( final FlattenedRenderJob flatJob : allFlattenedJobs )
		{
			final MadInstance<?,?> madInstance = flatJob.getMadInstance();
			allMadInstancesSet.add( madInstance );
			final MadRenderingJob renderingJob = new MadRenderingJob( madInstance.getInstanceName(), madInstance );

			fillInBuffersAndConnectedFlags( planHardwareSettings,  renderingJob,  graph,  channelInstanceToBufferMap,  allChannelBuffersSet );

			final MadParallelRenderingJob parallelJob = new MadParallelRenderingJob(flatJob.getCardinality(),  timingSource, renderingJob );
			flatToParallelJobMap.put( flatJob, parallelJob );

			allJobSet.add( parallelJob );

			totalNumJobs++;
		}

		// Second pass, filling in dependencies
		for( final FlattenedRenderJob flatJob : allFlattenedJobs )
		{
			// Who do this job depend on, and who depends on this job
			final Set<FlattenedRenderJob> flatProducerJobsWeWaitFor = flatJob.getProducerJobsWeWaitFor();
			final int numProducersWeWaitFor = flatProducerJobsWeWaitFor.size();
			final Set<FlattenedRenderJob> flatConsJobsWaitingForUs = flatJob.getConsumerJobsWaitingForUs();
			final int numConsumersWaitForUs = flatConsJobsWaitingForUs.size();

			final MadParallelRenderingJob parallelJob = flatToParallelJobMap.get( flatJob );

			// Now look up the jobs waiting for us
			final Set<AbstractParallelRenderingJob> consJobsThatWaitForUsSet = new HashSet<AbstractParallelRenderingJob>();
			for( final FlattenedRenderJob flatConJobWaitingForUs : flatConsJobsWaitingForUs )
			{
				final MadParallelRenderingJob relatedParallelRenderJob = flatToParallelJobMap.get( flatConJobWaitingForUs );
				if( relatedParallelRenderJob == null )
				{
					throw new RecordNotFoundException( "Unable to find related parallel job for one waiting for us. Sort order incorrect maybe?");
				}
				else
				{
					consJobsThatWaitForUsSet.add( relatedParallelRenderJob );
				}
			}

			if( numConsumersWaitForUs == 0 )
			{
				consJobsThatWaitForUsSet.add( finalSyncPrj );
			}

			final AbstractParallelRenderingJob[] consJobsThatWaitForUs = consJobsThatWaitForUsSet.toArray( new AbstractParallelRenderingJob[ consJobsThatWaitForUsSet.size() ] );

			final int cardinality = flatJob.getCardinality();
			if( numProducersWeWaitFor == 0 )
			{
				// Is an "initial" mad parallel job (doesn't need to wait for input, can just go)
				assert( cardinality == 1 );
				// We will have to wait for the inital fan to complete.
				parallelJob.setDependencies( consJobsThatWaitForUs, 1 );
				initialAuJobSet.add(  parallelJob );
			}
			else
			{
				parallelJob.setDependencies( consJobsThatWaitForUs, numProducersWeWaitFor );
			}

			if( numConsumersWaitForUs == 0 )
			{
				// Is a "final" mad parallel job (has no further dependencies)
				finalAuJobSet.add( parallelJob );
			}
		}

		final MadParallelRenderingJob[] initialAuJobs = initialAuJobSet.toArray( new MadParallelRenderingJob[ initialAuJobSet.size() ] );

		// Now create the initial fan and final sync jobs and re-write where necessary.
		final InitialFanParallelRenderingJob initialFanPrj = new InitialFanParallelRenderingJob( initialAuJobs, 1 );
		allJobSet.add( initialFanPrj );
		finalSyncPrj.setNumProducersWeWaitFor( finalAuJobSet.size() );

		final AbstractParallelRenderingJob[] realInitialJobs = new AbstractParallelRenderingJob[1];
		realInitialJobs[0] = initialFanPrj;

		final AbstractParallelRenderingJob[] allJobs = allJobSet.toArray( new AbstractParallelRenderingJob[ allJobSet.size() ] );

		final MadChannelBuffer[] allChannelBuffers = allChannelBuffersSet.toArray( new MadChannelBuffer[ allChannelBuffersSet.size() ] );

		return new RenderingPlanWithFanAndSync( planHardwareSettings,
				timingSource.getTimingParameters(),
				planFrameTimeFactory,
				initialFanPrj,
				finalSyncPrj,
				allJobs,
				realInitialJobs,
				totalNumJobs + 2,
				allMadInstancesSet,
				allChannelBuffers );
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	public void setGraphService( final MadGraphService graphService )
	{
		this.graphService = graphService;
	}

	@Override
	public void dumpRenderingPlan( final RenderingPlan renderingPlan ) throws DatastoreException
	{
		log.debug("Rendering plan dump:");
		log.debug("=====================");
		@SuppressWarnings("unused")
		final Dumper dumper = new Dumper( renderingPlan );
		log.debug("=====================");
	}

	public void setTimingService( final TimingService timingService )
	{
		this.timingService = timingService;
	}

	@Override
	public void destroyRenderingPlan( final RenderingPlan renderingPlan )
	{
		// Do nothing, we'll let GC take care of it.
	}

}
