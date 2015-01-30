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

package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.util.exception.DatastoreException;

public class DumpProfileAction extends AbstractAction
{
	private static Log log = LogFactory.getLog( DumpProfileAction.class.getName() );
	/**
	 *
	 */
	private static final long serialVersionUID = -3756758345674844578L;
	private final ComponentDesignerFrontController fc;

	public DumpProfileAction( final ComponentDesignerFrontController fc )
	{
		this.fc = fc;
		this.putValue(NAME, "Dump Profile To Console");
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		try
		{
			fc.dumpProfileResults();
		}
		catch (final DatastoreException e1)
		{
			if( log.isErrorEnabled() )
			{
				log.error( "Error executing dump profile results: " + e1.toString(), e1 );
			}
		}
	}
}
