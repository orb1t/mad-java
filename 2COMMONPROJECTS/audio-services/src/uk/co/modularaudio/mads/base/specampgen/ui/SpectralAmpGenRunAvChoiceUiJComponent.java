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

package uk.co.modularaudio.mads.base.specampgen.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.mads.base.specampgen.mu.SpectralAmpGenMadDefinition;
import uk.co.modularaudio.mads.base.specampgen.mu.SpectralAmpGenMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class SpectralAmpGenRunAvChoiceUiJComponent<D extends SpectralAmpGenMadDefinition<D, I>,
	I extends SpectralAmpGenMadInstance<D, I>,
	U extends SpectralAmpGenMadUiInstance<D, I>>
	extends JPanel
	implements IMadUiControlInstance<D, I, U>
{
	private static final long serialVersionUID = -6447604891826738340L;

	public enum RunningAverage
	{
		OFF( "Off" ),
		SHORT( "Short Average" ),
		LONG( "Long Average" ),
		FALL( "Fall" ),
		FAST_FALL( "Fast Fall" ),
		PEAK_HOLD( "Peak Hold" ),
		PEAK_GRAB( "Peak Grab" ),
		PEAK_DETECT( "Peak Detect" );

		private RunningAverage( final String label )
		{
			this.label = label;
		}

		private String label;

		public String getLabel()
		{
			return label;
		}
	};

	public final static RunningAverage DEFAULT_RUNNING_AVERAGE = RunningAverage.FAST_FALL;

	private final static Map<String, RunningAverage> LABEL_TO_MAPPING = new HashMap<>();

	static
	{
		LABEL_TO_MAPPING.put( RunningAverage.OFF.getLabel(), RunningAverage.OFF );
		LABEL_TO_MAPPING.put( RunningAverage.SHORT.getLabel(), RunningAverage.SHORT );
		LABEL_TO_MAPPING.put( RunningAverage.LONG.getLabel(), RunningAverage.LONG );
		LABEL_TO_MAPPING.put( RunningAverage.FALL.getLabel(), RunningAverage.FALL );
		LABEL_TO_MAPPING.put( RunningAverage.FAST_FALL.getLabel(), RunningAverage.FAST_FALL );
		LABEL_TO_MAPPING.put( RunningAverage.PEAK_HOLD.getLabel(), RunningAverage.PEAK_HOLD );
		LABEL_TO_MAPPING.put( RunningAverage.PEAK_GRAB.getLabel(), RunningAverage.PEAK_GRAB );
		LABEL_TO_MAPPING.put( RunningAverage.PEAK_DETECT.getLabel(), RunningAverage.PEAK_DETECT );
	}

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public SpectralAmpGenRunAvChoiceUiJComponent( final D definition,
			final I instance,
			final U uiInstance,
			final int controlIndex )
	{
		setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );
		msh.addColumnConstraint( "[grow 0][fill]" );

		setLayout( msh.createMigLayout() );

		final LWTCLabel label = new LWTCLabel( LWTCControlConstants.STD_LABEL_COLOURS, "Peaks:" );
		label.setBorder( BorderFactory.createEmptyBorder() );
		label.setFont( LWTCControlConstants.LABEL_FONT );
		add( label, "align center, right" );

		model = new DefaultComboBoxModel<String>();
		model.addElement( RunningAverage.OFF.getLabel() );
		model.addElement( RunningAverage.SHORT.getLabel() );
		model.addElement( RunningAverage.LONG.getLabel() );
		model.addElement( RunningAverage.FALL.getLabel() );
		model.addElement( RunningAverage.FAST_FALL.getLabel() );
		model.addElement( RunningAverage.PEAK_HOLD.getLabel() );
		model.addElement( RunningAverage.PEAK_GRAB.getLabel() );
		model.addElement( RunningAverage.PEAK_DETECT.getLabel() );

		model.setSelectedItem( DEFAULT_RUNNING_AVERAGE.getLabel() );

		rotaryChoice = new LWTCRotaryChoice( LWTCControlConstants.STD_ROTARY_CHOICE_COLOURS,
				model,
				false );

		model.addListDataListener( new ListDataListener()
		{

			@Override
			public void intervalRemoved( final ListDataEvent e )
			{
			}

			@Override
			public void intervalAdded( final ListDataEvent e )
			{
			}

			@Override
			public void contentsChanged( final ListDataEvent e )
			{
				final String val = (String)model.getSelectedItem();
				final RunningAverage ra = LABEL_TO_MAPPING.get( val );
				uiInstance.setDesiredRunningAverage( ra );
			}
		} );

		this.add( rotaryChoice, "grow");
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage ,
			final MadTimingParameters timingParameters ,
			final int U_currentGuiTime , int framesSinceLastTick )
	{
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public String getControlValue()
	{
		return (String)model.getSelectedItem();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		model.setSelectedItem( value );
	}
}
