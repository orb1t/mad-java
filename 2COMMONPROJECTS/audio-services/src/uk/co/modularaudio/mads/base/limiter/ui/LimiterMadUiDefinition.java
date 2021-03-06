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

package uk.co.modularaudio.mads.base.limiter.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.limiter.mu.LimiterMadDefinition;
import uk.co.modularaudio.mads.base.limiter.mu.LimiterMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class LimiterMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<LimiterMadDefinition, LimiterMadInstance, LimiterMadUiInstance>
{
	private static final Span SPAN = new Span(2,2);

	private static final int[] CHAN_INDEXES = new int[] {
		LimiterMadDefinition.CONSUMER_IN_LEFT,
		LimiterMadDefinition.CONSUMER_IN_RIGHT,
		LimiterMadDefinition.PRODUCER_OUT_LEFT,
		LimiterMadDefinition.PRODUCER_OUT_RIGHT
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 150, 40 ),
		new Point( 170, 40 ),
		new Point( 350, 40 ),
		new Point( 370, 40 )
	};

	private static final String[] CONTORL_NAMES = new String[] {
		"UseHardLimit",
		"Knee",
		"Falloff"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.CHECKBOX,
		ControlType.SLIDER,
		ControlType.SLIDER
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		LimiterHardLimitCheckboxUiJComponent.class,
//		LimiterKneeSliderUiJComponent.class,
		LimiterKneeSliderUiJComponent.class,
		LimiterFalloffSliderUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 200,   6, 156,  18 ),		// Use hard limit
		new Rectangle(  16,  30, 255,  30 ),		// Knee slider
		new Rectangle( 285,  30, 255,  30 )			// Falloff slider
	};

	private static final Class<LimiterMadUiInstance> INSTANCE_CLASS = LimiterMadUiInstance.class;

	public LimiterMadUiDefinition( final LimiterMadDefinition definition )
		throws DatastoreException
	{
		super( MadUIStandardBackgrounds.STD_2X2_DARKGRAY,
				definition,
				SPAN,
				INSTANCE_CLASS,
				CHAN_INDEXES,
				CHAN_POSIS,
				CONTORL_NAMES,
				CONTROL_TYPES,
				CONTROL_CLASSES,
				CONTROL_BOUNDS );
	}
}
