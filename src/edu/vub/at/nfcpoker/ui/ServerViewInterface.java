package edu.vub.at.nfcpoker.ui;

import android.content.Context;
import edu.vub.at.nfcpoker.Card;
import edu.vub.at.nfcpoker.GameState;
import edu.vub.at.nfcpoker.PlayerState;

public interface ServerViewInterface {

	// Table
	
	public void revealCards(final Card[] cards);

	public void resetCards();
	
	public void showStateChange(GameState newState);

	public void updatePoolMoney(int chipsPool);
	
	// Players

	public void addPlayer(PlayerState player);

	public void updatePlayerStatus(PlayerState player);
	
	public void removePlayer(Integer i);

	// Other
	
	public Context getContext();

	
}
