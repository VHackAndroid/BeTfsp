package edu.vub.at.nfcpoker.ui;

import android.content.Context;
import edu.vub.at.nfcpoker.Card;
import edu.vub.at.nfcpoker.PokerGameState;
import edu.vub.at.nfcpoker.PlayerState;

public class DummServerView implements ServerViewInterface {

	@Override
	public Context getContext() {
		return null;
	}
	
	@Override
	public void revealCards(Card[] cards) { }

	@Override
	public void resetCards() { }

	@Override
	public void updateGameState(PokerGameState newState) { }
	
	@Override
	public void updatePoolMoney(int chipsPool) { }

	@Override
	public void addPlayer(PlayerState player) { }

	@Override
	public void updatePlayerStatus(PlayerState player) { }

	@Override
	public void removePlayer(PlayerState i) { }

}
