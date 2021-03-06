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

package uk.co.modularaudio.mads.base.scopen.ui.display;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.scopen.ui.ScopeNColours;
import uk.co.modularaudio.mads.base.scopen.ui.ScopeNDisplayUiJComponent;

public class ScopeTimeMarks extends JPanel
{
	private static final long serialVersionUID = -6169422018023474030L;

//	private static Log log = LogFactory.getLog( ScopeTimeMarks.class.getName() );

	private int width;
	private int height;
	private int horizPixelsPerMarker;
	private final int numFreqMarkers;

	public ScopeTimeMarks( final int numFreqMarkers )
	{
		this.numFreqMarkers = numFreqMarkers;
		setMinimumSize( new Dimension( ScopeNDisplayUiJComponent.AXIS_MARKS_LENGTH, ScopeNDisplayUiJComponent.AXIS_MARKS_LENGTH ) );
	}

	@Override
	public void paint( final Graphics g )
	{
		g.setColor( ScopeNColours.SCOPE_AXIS_DETAIL );

		for( int i = 0 ; i < numFreqMarkers ; ++i )
		{
			final int x = (horizPixelsPerMarker * i) + ScopeNDisplayUiJComponent.AXIS_MARKS_LENGTH;
			g.drawLine( x, 0, x, height );
		}
	}

	private void setupInternalDistances( final int width, final int height )
	{
		this.width = width - 1 - ScopeNDisplayUiJComponent.AXIS_MARKS_LENGTH - ScopeNDisplayUiJComponent.AMP_DISPLAY_RIGHT_PADDING;
		this.height = height - 1;

		horizPixelsPerMarker = ScopeNDisplayUiJComponent.getAdjustedWidthBetweenMarkers( this.width, numFreqMarkers );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		setupInternalDistances( width, height );
	}
}
