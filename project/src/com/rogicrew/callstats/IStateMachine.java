package com.rogicrew.callstats;

public interface IStateMachine {
	public static enum State
	{
		STATE_OUTGOING,
		STATE_INCOMING,
		STATE_MISSING,
		STATE_SETTINGS,
		STATE_OUTGOING_BY_NAME,
		STATE_INCOMING_BY_NAME,
		STATE_ABOUT,
	}
	
	public void changeStateTo(State newState);
	public State getState();
}
