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

package uk.co.modularaudio.mads.base.midside.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class MidSideIOQueueBridge extends MadLocklessQueueBridge<MidSideMadInstance>
{
	private static Log log = LogFactory.getLog( MidSideIOQueueBridge.class.getName() );

	public static final int COMMAND_IN_MS_TYPE = 0;

	@Override
	public void receiveQueuedEventsToInstance( final MidSideMadInstance instance,
			final ThreadSpecificTemporaryEventStorage tses,
			final int U_periodTimestamp,
			final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case MidSideIOQueueBridge.COMMAND_IN_MS_TYPE:
			{
				final boolean isLrToMs = (queueEntry.value == 1);
				instance.setMidSideType( isLrToMs );
				break;
			}
			default:
			{
				final String msg = "Unknown command: " + queueEntry.command;
				log.error( msg );
				break;
			}
		}
	}

}
