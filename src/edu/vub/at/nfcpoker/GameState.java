package edu.vub.at.nfcpoker;

public enum GameState {
	STOPPED, WAITING_FOR_PLAYERS, PREFLOP, FLOP, TURN, RIVER, END_OF_ROUND;
	
	@Override
	public String toString() {
		switch (this) {
		case STOPPED:
			return "STOPPED";
		case WAITING_FOR_PLAYERS:
			return "Waiting for other players";
		case PREFLOP:
			return "Pre-flop";
		case FLOP:
			return "Flop";
		case TURN:
			return "Turn";
		case RIVER:
			return "River";
		case END_OF_ROUND:
			return "Round ended";
		default:
			return "";
		}
	}
}
