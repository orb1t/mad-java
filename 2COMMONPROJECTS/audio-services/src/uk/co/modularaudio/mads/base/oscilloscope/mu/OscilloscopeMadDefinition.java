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

package uk.co.modularaudio.mads.base.oscilloscope.mu;

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

public class OscilloscopeMadDefinition
	extends AbstractNonConfigurableMadDefinition<OscilloscopeMadDefinition,OscilloscopeMadInstance>
	implements BaseMadDefinition
{
	// Indexes into the channels
	public final static int CONSUMER_CV_TRIGGER = 0;
	public final static int CONSUMER_AUDIO_SIGNAL0 = 1;
	public final static int CONSUMER_CV_SIGNAL0 = 2;
	public final static int CONSUMER_AUDIO_SIGNAL1 = 3;
	public final static int CONSUMER_CV_SIGNAL1 = 4;
	public final static int NUM_CHANNELS = 5;

	public final static String DEFINITION_ID = "oscilloscope";

	private final static String USER_VISIBILE_NAME = "Oscilloscope";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_ANALYSIS_GROUP_ID;
	private final static String CLASS_NAME = "Oscilloscope";
	private final static String CLASS_DESC = "An oscilloscope for analysing sound";

	// These must match the channel indexes given above
	private final static String[] CHAN_NAMES = new String[] {
		"Input Trigger",
		"Input Wave 0",
		"Input CV 0",
		"Input Wave 1",
		"Input CV 1"};

	private final static MadChannelType[] CHAN_TYPES = new MadChannelType[] {
		MadChannelType.CV,
		MadChannelType.AUDIO,
		MadChannelType.CV,
		MadChannelType.AUDIO,
		MadChannelType.CV };

	private final static MadChannelDirection[] CHAN_DIRS = new MadChannelDirection[] { MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER };

	private final static MadChannelPosition[] CHAN_POSIS = new MadChannelPosition[] { MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO };

	private final BaseComponentsCreationContext creationContext;

	public OscilloscopeMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classificationService ) throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBILE_NAME,
				new MadClassification( classificationService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.ALPHA ),
				new OscilloscopeIOQueueBridge(),
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
		return new OscilloscopeMadInstance(
				creationContext,
				instanceName,
				this,
				parameterValues,
				getChannelConfigurationForParameters( parameterValues ) );
	}
}
