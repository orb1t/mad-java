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

package uk.co.modularaudio.mads.base.scopesmall.ui;

import uk.co.modularaudio.mads.base.scopegen.ui.ScopeGenRecaptureButtonUiJComponent;
import uk.co.modularaudio.mads.base.scopesmall.mu.ScopeSmallMadDefinition;
import uk.co.modularaudio.mads.base.scopesmall.mu.ScopeSmallMadInstance;

public class ScopeSmallRecaptureButtonUiJComponent
	extends ScopeGenRecaptureButtonUiJComponent<ScopeSmallMadDefinition, ScopeSmallMadInstance, ScopeSmallMadUiInstance>
{
	public ScopeSmallRecaptureButtonUiJComponent( final ScopeSmallMadDefinition definition,
			final ScopeSmallMadInstance instance,
			final ScopeSmallMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( definition, instance, uiInstance, controlIndex );
	}
}
