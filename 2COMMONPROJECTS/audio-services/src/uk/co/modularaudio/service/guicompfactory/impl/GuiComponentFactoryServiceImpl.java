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

package uk.co.modularaudio.service.guicompfactory.impl;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.gui.valueobjects.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.guicompfactory.GuiComponentFactoryService;
import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentImageCache;
import uk.co.modularaudio.service.guicompfactory.impl.cache.bia.ComponentBackImageACache;
import uk.co.modularaudio.service.guicompfactory.impl.cache.bia.ComponentFrontImageACache;
import uk.co.modularaudio.service.guicompfactory.impl.cache.raw.ComponentBackImageRCache;
import uk.co.modularaudio.service.guicompfactory.impl.cache.raw.ComponentFrontImageRCache;
import uk.co.modularaudio.service.guicompfactory.impl.components.GuiAudioComponentBack;
import uk.co.modularaudio.service.guicompfactory.impl.components.GuiAudioComponentFront;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;

public class GuiComponentFactoryServiceImpl implements ComponentWithLifecycle, GuiComponentFactoryService
{
	//	private static Log log = LogFactory.getLog( GuiComponentFactoryServiceImpl.class.getName() );

	private ConfigurationService configurationService;
	private BufferedImageAllocationService bufferedImageAllocationService;

	private GuiComponentImageCache frontComponentImageCache;
	private GuiComponentImageCache backComponentImageCache;

	private final static boolean USE_ALLOCATOR_TO_BACK_IMAGES = true;

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( USE_ALLOCATOR_TO_BACK_IMAGES )
		{
			frontComponentImageCache = new ComponentFrontImageACache( bufferedImageAllocationService );
			backComponentImageCache = new ComponentBackImageACache( bufferedImageAllocationService );
		}
		else
		{
			frontComponentImageCache = new ComponentFrontImageRCache();
			backComponentImageCache = new ComponentBackImageRCache();
		}
	}

	@Override
	public void destroy()
	{
		frontComponentImageCache.destroy();
		backComponentImageCache.destroy();
	}

	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	@Override
	public AbstractGuiAudioComponent createBackGuiComponent(final RackComponent inComponent)
	{
		return new GuiAudioComponentBack( backComponentImageCache, inComponent );
	}

	@Override
	public AbstractGuiAudioComponent createFrontGuiComponent(final RackComponent inComponent)
	{
		return new GuiAudioComponentFront( frontComponentImageCache, inComponent );
	}

	public void setBufferedImageAllocationService(
			final BufferedImageAllocationService bufferedImageAllocationService )
	{
		this.bufferedImageAllocationService = bufferedImageAllocationService;
	}
}
