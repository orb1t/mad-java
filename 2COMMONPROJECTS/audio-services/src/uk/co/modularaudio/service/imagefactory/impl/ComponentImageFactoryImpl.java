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

package uk.co.modularaudio.service.imagefactory.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;

public class ComponentImageFactoryImpl implements ComponentWithLifecycle, ComponentImageFactory
{
	private static Log log = LogFactory.getLog( ComponentImageFactoryImpl.class.getName() );

	private static final String CONFIG_KEY_RESOURCE_PREFIX = ComponentImageFactoryImpl.class.getSimpleName() + ".ResourcePrefix";

	private final Map<String, BufferedImage> biCache = new HashMap<String, BufferedImage>();

	private ConfigurationService configurationService;

	private String resourcePrefix;

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( configurationService == null )
		{
			throw new ComponentConfigurationException("Service missing dependencies. Check configuration");
		}
		final Map<String, String> errors = new HashMap<String, String>();
		resourcePrefix = ConfigurationServiceHelper.checkForSingleStringKey( configurationService, CONFIG_KEY_RESOURCE_PREFIX, errors );
		ConfigurationServiceHelper.errorCheck( errors );
	}

	@Override
	public void destroy()
	{
	}

	private BufferedImage getBufferedImageFromPath( final String filename) throws DatastoreException
	{
		final String pathToLoad = filename;
		BufferedImage retVal = biCache.get( pathToLoad );

		if( retVal == null )
		{
			try {
				final String resourcePath = resourcePrefix + pathToLoad;
				final InputStream iis = this.getClass().getResourceAsStream( resourcePath );
				if( iis == null )
				{
					throw new DatastoreException("Failed to find image resource at path: " + resourcePath );
				}
				retVal = ImageIO.read( iis );
				if( log.isDebugEnabled() )
				{
					log.debug( "Adding " + pathToLoad + " to buffered image cache" );
				}
				if( retVal.getColorModel().hasAlpha() )
				{
					if( log.isWarnEnabled() )
					{
						log.warn("Image " + pathToLoad + " has alpha, and probably shouldn't");
					}
				}
				biCache.put( pathToLoad, retVal );
			} catch (final IOException ie) {
				final String msg = "Exception caught loading image " + pathToLoad + ": " + ie.toString();
				log.error( msg, ie );
				throw new DatastoreException( msg, ie );
			}
		}

		return retVal;
	}

	@Override
	public synchronized BufferedImage getBufferedImage( final String filename) throws DatastoreException
	{
		return getBufferedImageFromPath( filename );
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}
}
