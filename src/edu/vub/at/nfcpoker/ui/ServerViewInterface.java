package edu.vub.at.nfcpoker.ui;

import android.content.Context;
import edu.vub.at.nfcpoker.Card;
import edu.vub.at.nfcpoker.PokerGameState;
import edu.vub.at.nfcpoker.PlayerState;

public interface ServerViewInterface {

	// Table
	
	public void revealCards(final Card[] cards);

	public void resetCards();
	
	public void updateGameState(PokerGameState newState);

	public void updatePoolMoney(int chipsPool);
	
	// Players

	public void addPlayer(PlayerState player);

	public void updatePlayerStatus(PlayerState player);
	
	public void removePlayer(PlayerState player);

	// Other
	
	public Context getContext();

	
}
