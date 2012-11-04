package edu.vub.at.nfcpoker.ui;

import android.content.Context;
import edu.vub.at.nfcpoker.Card;
import edu.vub.at.nfcpoker.GameState;
import edu.vub.at.nfcpoker.PlayerState;

public class DummServerView implements ServerViewInterface {

	@Override
	public void revealCards(Card[] cards) { }

	@Override
	public void resetCards() { }

	@Override
	public void showStateChange(GameState newState) { }
	
	@Override
	public void updatePoolMoney(int chipsPool) { }

	@Override
	public void removePlayer(Integer i) { }

	@Override
	public Context getContext() {
		return null;
	}

	@Override
	public void addPlayer(PlayerState player) { }

	@Override
	public void updatePlayerStatus(PlayerState player) { }

}
