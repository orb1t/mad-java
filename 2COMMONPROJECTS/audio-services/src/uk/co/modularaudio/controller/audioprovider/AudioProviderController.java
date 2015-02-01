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

package uk.co.modularaudio.controller.audioprovider;

import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorCallback;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingSession;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOConfiguration;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

/**
 * The contract of the audio provider controller allows the creation
 * of an AppRenderingIO object that may then be associated
 * with an AppRenderingSession.</p>
 *
 * @author dan
 */
public interface AudioProviderController
{
	/**
	 * @param hardwareIOConfiguration
	 * @param errorCallback
	 * @return
	 * @throws DatastoreException
	 * @throws RecordNotFoundException
	 */
	AppRenderingSession createAppRenderingSessionForConfiguration( HardwareIOConfiguration hardwareIOConfiguration,
			AppRenderingErrorCallback errorCallback )
		throws DatastoreException, RecordNotFoundException;
}
