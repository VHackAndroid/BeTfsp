package edu.vub.at.nfcpoker.ui;

import android.content.Context;
import edu.vub.at.nfcpoker.Card;
import edu.vub.at.nfcpoker.ConcretePokerServer.GameState;

public class DummServerView implements ServerViewInterface {

	@Override
	public void revealCards(Card[] cards) { }

	@Override
	public void resetCards() { }

	@Override
	public void addPlayer(int clientID, String clientName, int initialMoney) { }

	@Override
	public void showStatechange(GameState newState) { }

	@Override
	public void setPlayerMoney(Integer player, int current) { }

	@Override
	public void removePlayer(Integer i) { }

	@Override
	public Context getContext() {
		return null;
	}

}
