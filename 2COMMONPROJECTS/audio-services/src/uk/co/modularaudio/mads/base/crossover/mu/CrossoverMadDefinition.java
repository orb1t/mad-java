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

package uk.co.modularaudio.mads.base.crossover.mu;

import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.BaseMadDefinition;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.helper.AbstractNonConfigurableMadDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class CrossoverMadDefinition
	extends AbstractNonConfigurableMadDefinition<CrossoverMadDefinition,CrossoverMadInstance>
	implements BaseMadDefinition
{
	// Indexes into the channels
	public final static int CONSUMER_IN_LEFT = 0;
	public final static int CONSUMER_IN_RIGHT = 1;
	public final static int CONSUMER_IN_CV_FREQUENCY = 2;
	public final static int PRODUCER_OUT_LOW_LEFT = 3;
	public final static int PRODUCER_OUT_LOW_RIGHT = 4;
	public final static int PRODUCER_OUT_HIGH_LEFT = 5;
	public final static int PRODUCER_OUT_HIGH_RIGHT = 6;
	public final static int NUM_CHANNELS = 7;

	public static final String DEFINITION_ID = "crossover";

	private final static String USER_VISIBLE_NAME = "Crossover";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_PROCESSING_GROUP_ID;
	private final static String CLASS_NAME = "Crossover";
	private final static String CLASS_DESC = "A digital crossover.";

	// These must match the channel indexes given above
	private final static String[] CHAN_NAMES = new String[] { "Input Wave Left",
		"Input Wave Right",
		"Input CV Frequency",
		"Output Wave Low Left",
		"Output Wave Low Right",
		"Output Wave High Left",
		"Output Wave High Right"};

	private final static MadChannelType[] CHAN_TYPES = new MadChannelType[] { MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.CV,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO,
		MadChannelType.AUDIO };

	private final static MadChannelDirection[] CHAN_DIRS = new MadChannelDirection[] { MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER,
		MadChannelDirection.PRODUCER };

	private final static MadChannelPosition[] CHAN_POSIS = new MadChannelPosition[] { MadChannelPosition.STEREO_LEFT,
		MadChannelPosition.STEREO_RIGHT,
		MadChannelPosition.MONO,
		MadChannelPosition.STEREO_LEFT,
		MadChannelPosition.STEREO_RIGHT,
		MadChannelPosition.STEREO_LEFT,
		MadChannelPosition.STEREO_RIGHT };

	private final BaseComponentsCreationContext creationContext;

	public CrossoverMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.RELEASED ),
				new CrossoverIOQueueBridge(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSIS );
		this.creationContext = creationContext;
	}

	@Override
	public MadInstance<?, ?> createInstance( final Map<MadParameterDefinition, String> parameterValues, final String instanceName )
	{
		return new CrossoverMadInstance(
				creationContext,
				instanceName,
				this,
				parameterValues,
				getChannelConfigurationForParameters( parameterValues ) );
	}
}
