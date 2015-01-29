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

package uk.co.modularaudio.util.audio.gui.mad.helper;

import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.table.Span;

public abstract class AbstractNoNameChangeNonConfigurableMadUiInstance<D extends MadDefinition<D,I>, I extends MadInstance<D, I>>
	extends AbstractNonConfigurableMadUiInstance<D, I>
{
	public AbstractNoNameChangeNonConfigurableMadUiInstance( final Span span,
			final I instance,
			final MadUiDefinition<D, I> componentUiDefinition )
	{
		super( span, instance, componentUiDefinition );
	}

	@Override
	public final void receiveComponentNameChange( final String newName )
	{
		// No name change
	}

	@Override
	public abstract void consumeQueueEntry( I instance, IOQueueEvent nextOutgoingEntry );

}
