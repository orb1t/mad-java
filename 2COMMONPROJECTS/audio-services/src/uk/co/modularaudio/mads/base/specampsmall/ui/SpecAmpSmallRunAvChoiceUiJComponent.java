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

package uk.co.modularaudio.mads.base.specampsmall.ui;

import uk.co.modularaudio.mads.base.specampgen.ui.SpectralAmpGenRunAvChoiceUiJComponent;
import uk.co.modularaudio.mads.base.specampsmall.mu.SpecAmpSmallMadDefinition;
import uk.co.modularaudio.mads.base.specampsmall.mu.SpecAmpSmallMadInstance;

public class SpecAmpSmallRunAvChoiceUiJComponent
	extends SpectralAmpGenRunAvChoiceUiJComponent<SpecAmpSmallMadDefinition, SpecAmpSmallMadInstance, SpecAmpSmallMadUiInstance>
{
	private static final long serialVersionUID = 3528706960655187180L;

	public SpecAmpSmallRunAvChoiceUiJComponent( final SpecAmpSmallMadDefinition definition,
			final SpecAmpSmallMadInstance instance,
			final SpecAmpSmallMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( definition, instance, uiInstance, controlIndex );
	}
}
