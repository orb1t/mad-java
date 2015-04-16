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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;

import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.AnalysisFillCompletionListener;
import uk.co.modularaudio.service.audioanalysis.AudioAnalysisService;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.AnalysisListener;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.GainDetectionListener;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.StaticThumbnailGeneratorListener;
import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileDirection;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileFormat;
import uk.co.modularaudio.service.audiofileio.StaticMetadata;
import uk.co.modularaudio.service.audiofileioregistry.AudioFileIORegistryService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.hashedstorage.HashedRef;
import uk.co.modularaudio.service.hashedstorage.HashedStorageService;
import uk.co.modularaudio.service.hashedstorage.HashedWarehouse;
import uk.co.modularaudio.service.hibsession.HibernateSessionService;
import uk.co.modularaudio.service.library.LibraryEntry;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.HibernateQueryBuilder;
import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;
import uk.co.modularaudio.util.hibernate.ReflectionUtils;
import uk.co.modularaudio.util.hibernate.ThreadLocalSessionResource;
import uk.co.modularaudio.util.hibernate.component.ComponentWithHibernatePersistence;
import uk.co.modularaudio.util.hibernate.component.HibernatePersistedBeanDefinition;

public class AudioAnalysisServiceImpl implements ComponentWithLifecycle, AudioAnalysisService,
	ComponentWithHibernatePersistence
{
	private static Log log = LogFactory.getLog( AudioAnalysisServiceImpl.class.getName() );

	private static final String CONFIG_KEY_ANALYSIS_BUFFER_SIZE = "BufferSize";

	private static final String CONFIG_KEY_STATIC_THUMB_ROOT = "StaticThumbnailRootDir";
	private static final String CONFIG_KEY_STATIC_MINMAX_COLOR = "StaticThumbnailMinMaxColor";
	private static final String CONFIG_KEY_STATIC_RMS_COLOR = "StaticThumbnailRmsColor";
	private static final String CONFIG_KEY_STATIC_THUMB_WIDTH = "StaticThumbnailWidth";
	private static final String CONFIG_KEY_STATIC_THUMB_HEIGHT = "StaticThumbnailHeight";

	private String databaseTablePrefix;

	private ConfigurationService configurationService;
	private AudioFileIORegistryService audioFileIORegistryService;
	private HashedStorageService hashedStorageService;
	private HibernateSessionService hibernateSessionService;

	private final ReentrantLock analysisLock = new ReentrantLock();

	private String staticThumbnailRootDir;
	private int staticThumbnailWidth;
	private int staticThumbnailHeight;
	private Color staticMinMaxColor;
	private Color staticRmsColor;

	private HashedWarehouse staticThumbnailWarehouse;

	private int analysisBufferSize;
	private int analysisBufferFrames;

	private boolean isHalting;

	private final List<HibernatePersistedBeanDefinition> hibernateBeanDefs = new ArrayList<HibernatePersistedBeanDefinition>();

	public AudioAnalysisServiceImpl()
	{
		hibernateBeanDefs.add( new HibernatePersistedBeanDefinition( ReflectionUtils.getClassPackageAsPath( this ) +
				"/hbm/AnalysedData.hbm.xml",
				databaseTablePrefix ) );
	}

	@Override
	public List<HibernatePersistedBeanDefinition> listHibernatePersistedBeanDefinitions()
	{
		return hibernateBeanDefs;
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( configurationService == null ||
				audioFileIORegistryService == null ||
				hashedStorageService == null ||
				hibernateSessionService == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Check configuration" );
		}

		// Fetch our configuration data
		// (1) Static thumb nail storage directory
		// (2) Scrolling thumb nail storage directory
		try
		{
			// Static thumbnail config
			staticThumbnailRootDir = configurationService.getSingleStringValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_STATIC_THUMB_ROOT );
			staticThumbnailWidth = configurationService.getSingleIntValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_STATIC_THUMB_WIDTH );
			staticThumbnailHeight = configurationService.getSingleIntValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_STATIC_THUMB_HEIGHT );
			staticMinMaxColor = configurationService.getSingleColorValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_STATIC_MINMAX_COLOR );
			staticRmsColor = configurationService.getSingleColorValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_STATIC_RMS_COLOR );

			// Our internal buffer size
			analysisBufferSize = configurationService.getSingleIntValue(
					getClass().getSimpleName() + "." + CONFIG_KEY_ANALYSIS_BUFFER_SIZE );
		}
		catch (final RecordNotFoundException e)
		{
			final String msg = "Unable to find config key: " + e.toString();
			log.error( msg, e);
			throw new ComponentConfigurationException( msg, e );
		}

		// Make sure our directories are correctly initialised
		try
		{
			staticThumbnailWarehouse = hashedStorageService.initStorage( staticThumbnailRootDir );
		}
		catch (final IOException e)
		{
			final String msg = "Unable to initialise hashed storage: " + e.toString();
			log.error( msg, e);
			throw new ComponentConfigurationException( msg, e );
		}
	}

	@Override
	public void destroy()
	{
		analysisLock.lock();
		try
		{
			isHalting = true;
		}
		finally
		{
			analysisLock.unlock();
		}
	}

	@Override
	public AnalysedData analyseFile(final String pathToFile, final AnalysisFillCompletionListener progressListener)
			throws DatastoreException, IOException, RecordNotFoundException, UnsupportedAudioFileException
	{
		final ArrayList<AnalysisListener> analysisListeners = new ArrayList<AnalysisListener>();

		final GainDetectionListener gdl = new GainDetectionListener();
		analysisListeners.add( gdl );

		final StaticThumbnailGeneratorListener stgl = new StaticThumbnailGeneratorListener( staticThumbnailWidth,
				staticThumbnailHeight,
				staticMinMaxColor,
				staticRmsColor,
				hashedStorageService,
				staticThumbnailWarehouse );
		analysisListeners.add( stgl );

		final AnalysedData analysedData = new AnalysedData();

		// Create the hashed ref our listeners might be interested in
		final HashedRef fileHashedRef = hashedStorageService.getHashedRefForFilename( pathToFile );

		final AudioFileFormat fileFormat = audioFileIORegistryService.sniffFileFormatOfFile( pathToFile );

		final AudioFileIOService decoderService = audioFileIORegistryService.getAudioFileIOServiceForFormatAndDirection( fileFormat,
				AudioFileDirection.DECODE );

		final float[] analysisBuffer = new float[ analysisBufferSize ];

		AudioFileHandleAtom fha = null;

		try
		{
			fha = decoderService.openForRead( pathToFile );

			final StaticMetadata sm = fha.getStaticMetadata();
			final DataRate dataRate = sm.dataRate;
			final int numChannels = sm.numChannels;
			final long totalFloats = sm.numFloats;
			final long totalFrames = sm.numFrames;
			if( log.isDebugEnabled() )
			{
				log.debug("Beginning audio analysis on file " + pathToFile +
						" with " + totalFloats + " total floats and " +
						totalFrames + " frames");
			}

			progressListener.receiveAnalysisBegin();

			analysisBufferFrames = analysisBufferSize / numChannels;

			for( final AnalysisListener al : analysisListeners )
			{
				al.start( dataRate, numChannels, totalFrames );
			}
			int percentageComplete = 0;

			long framesLeft = totalFrames;
			long curFramePos = 0;
			while( framesLeft > 0 )
			{
				final int framesThisRound = (int)(framesLeft > analysisBufferFrames ? analysisBufferFrames : framesLeft );

				final int numFramesRead = decoderService.readFrames( fha,
						analysisBuffer,
						0,
						framesThisRound,
						curFramePos );

//				log.debug("Read " + numFramesRead + " at pos " + curFramePos );

				if( numFramesRead != framesThisRound )
				{
					throw new DatastoreException("Failed reading frames - asked(" +
							framesThisRound + ") received(" + numFramesRead +")");
				}

				for( final AnalysisListener al : analysisListeners )
				{
					al.receiveFrames( analysisBuffer, numFramesRead );
				}

				final int newPercentageComplete = (int)((curFramePos / (float)totalFrames) * 100.0f);
				if( newPercentageComplete != percentageComplete )
				{
					percentageComplete = newPercentageComplete;
					progressListener.receivePercentageComplete( percentageComplete );
				}
				curFramePos += numFramesRead;
				framesLeft -= numFramesRead;
			}

			for( final AnalysisListener al : analysisListeners )
			{
				al.end();
				al.updateAnalysedData( analysedData, fileHashedRef );
			}
		}
		finally
		{
			if( fha != null )
			{
				decoderService.closeHandle( fha );
			}
		}

		return analysedData;
	}

	public void setAudioFileIORegistryService( final AudioFileIORegistryService audioFileIORegistryService )
	{
		this.audioFileIORegistryService = audioFileIORegistryService;
	}

	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public void setHashedStorageService(final HashedStorageService hashedStorageService)
	{
		this.hashedStorageService = hashedStorageService;
	}

	public void setDatabaseTablePrefix( final String databaseTablePrefix )
	{
		this.databaseTablePrefix = databaseTablePrefix;
	}

	public void setHibernateSessionService( final HibernateSessionService hibernateSessionService )
	{
		this.hibernateSessionService = hibernateSessionService;
	}

	private AnalysedData internalAnalyseFile( final Session session, final LibraryEntry libraryEntry,
			final AnalysisFillCompletionListener analysisListener )
		throws DatastoreException
	{
		AnalysedData retVal = null;
		try
		{
			retVal = analyseFile( libraryEntry.getLocation(),
					analysisListener );
			retVal.setLibraryEntryId( libraryEntry.getLibraryEntryId() );
			session.save( retVal );
		}
		catch( final IOException | RecordNotFoundException | UnsupportedAudioFileException e )
		{
			final String msg = "Exception caught during audio file analysis: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}

		return retVal;
	}

	@Override
	public AnalysedData analyseLibraryEntryFile( final LibraryEntry libraryEntry,
			final AnalysisFillCompletionListener analysisListener )
			throws DatastoreException, NoSuchHibernateSessionException
	{
		analysisLock.lock();
		try
		{
			if( isHalting )
			{
				throw new DatastoreException("No more analysis jobs allowed - shutting down");
			}
			final Session session = ThreadLocalSessionResource.getSessionResource();
			final HibernateQueryBuilder qb = new HibernateQueryBuilder();
			qb.initQuery( session, "from AnalysedData where libraryEntryId = :libraryEntryId" );
			qb.setInt( "libraryEntryId", libraryEntry.getLibraryEntryId() );

			final Query q = qb.buildQuery();
			AnalysedData retVal = (AnalysedData)q.uniqueResult();

			boolean needsAnalysis = false;
			if( retVal != null )
			{
				// Check the thumbnail exists
				final File tn = new File( retVal.getPathToStaticThumbnail() );
				if( !tn.exists() || !tn.canRead() )
				{
					// Delete the existing DB entry
					session.delete( retVal );
					session.flush();
					needsAnalysis = true;
				}
			}
			else
			{
				needsAnalysis = true;
			}

			if( needsAnalysis )
			{
				retVal = internalAnalyseFile( session, libraryEntry,
						analysisListener );
			}
			return retVal;
		}
		finally
		{
			analysisLock.unlock();
		}
	}

}
