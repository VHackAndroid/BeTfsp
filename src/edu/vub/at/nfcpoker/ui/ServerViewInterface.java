package edu.vub.at.nfcpoker.ui;

import edu.vub.at.nfcpoker.Card;
import edu.vub.at.nfcpoker.ConcretePokerServer.GameState;

public interface ServerViewInterface {

	public void revealCards(final Card[] cards);

	public void resetCards();

	public void addPlayer(int clientID, String clientName, int initialMoney);

	public void showStatechange(GameState newState);
	
}
