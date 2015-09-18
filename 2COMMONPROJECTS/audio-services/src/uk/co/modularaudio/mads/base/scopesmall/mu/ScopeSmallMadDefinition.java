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

package uk.co.modularaudio.mads.base.scopesmall.mu;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.scopegen.mu.ScopeGenMadDefinition;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class ScopeSmallMadDefinition
	extends ScopeGenMadDefinition<ScopeSmallMadDefinition, ScopeSmallMadInstance>
{
	public static final String DEFINITION_ID = "scope";

	private final static String USER_VISIBLE_NAME = "Scope";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_ANALYSIS_GROUP_ID;
	private final static String CLASS_NAME = "Scope";
	private final static String CLASS_DESC = "A signal analysis oscilloscope";


	public ScopeSmallMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classService )
		throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.RELEASED ) );
	}
}