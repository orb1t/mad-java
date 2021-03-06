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

package test.uk.co.modularaudio.util.statetransition;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import uk.co.modularaudio.util.statetransition.StateTransitionEngine;
import uk.co.modularaudio.util.statetransition.StateTransitionEngine.StateEventHandler;
import uk.co.modularaudio.util.statetransition.StateTransitionEngine.StateTransitionHandler;

public class TestStateTransitions extends TestCase
{
	private static Log log = LogFactory.getLog( TestStateTransitions.class.getName() );

	private enum TestState
	{
		NOTPRESSED_NOMOUSE_NOFOCUS,
		NOTPRESSED_NOMOUSE_FOCUS,
		NOTPRESSED_MOUSE_NOFOCUS,
		NOTPRESSED_MOUSE_FOCUS,
		PRESSED_NOMOUSE_NOFOCUS,
		PRESSED_NOMOUSE_FOCUS,
		PRESSED_MOUSE_NOFOCUS,
		PRESSED_MOUSE_FOCUS
	};

	private enum TestEvent
	{
		MOUSE_ENTER,
		MOUSE_LEAVE,
		FOCUS_RECEIVE,
		FOCUS_LOSE,
		MOUSE_PRESS,
		MOUSE_RELEASE
	};

	@Test
	public void testCreatingEngine()
	{
		final StateEventHandler[][] eventHandlers = StateTransitionEngine.getEmptyEventHandlersArray(
				TestState.values().length,
				TestEvent.values().length );
		final StateTransitionHandler[] transitionHandlers = StateTransitionEngine.getEmptyTransitionHandlersArray(
				TestState.values().length );

		// Fill in some event handlers
		final StateEventHandler goToNPMNFEventHandler = new StateEventHandler()
		{
			@Override
			public int processEventCreateTransition( final int currentState, final int event )
			{
					log.trace("Received mouse enter");
					return TestState.NOTPRESSED_MOUSE_NOFOCUS.ordinal();
			}
		};
		final StateEventHandler goToNPNMNFEventHandler = new StateEventHandler()
		{
			@Override
			public int processEventCreateTransition( final int currentState, final int event )
			{
				log.trace("Received mouse leave");
				return TestState.NOTPRESSED_NOMOUSE_NOFOCUS.ordinal();
			}
		};

		eventHandlers[TestState.NOTPRESSED_NOMOUSE_NOFOCUS.ordinal()][TestEvent.MOUSE_ENTER.ordinal()] =
				goToNPMNFEventHandler;
		eventHandlers[TestState.NOTPRESSED_MOUSE_NOFOCUS.ordinal()][TestEvent.MOUSE_LEAVE.ordinal()] =
				goToNPNMNFEventHandler;

		final StateTransitionEngine engine = new StateTransitionEngine( eventHandlers, transitionHandlers );

		int currentState = TestState.NOTPRESSED_NOMOUSE_NOFOCUS.ordinal();

		currentState = engine.processEvent( currentState, TestEvent.MOUSE_ENTER.ordinal() );
		assertTrue( currentState == TestState.NOTPRESSED_MOUSE_NOFOCUS.ordinal() );
	}

}
