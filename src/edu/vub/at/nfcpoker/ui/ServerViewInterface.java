package edu.vub.at.nfcpoker.ui;

import edu.vub.at.nfcpoker.Card;

public interface ServerViewInterface {

	public void revealCards(final Card[] cards);

	public void resetCards();
	
}
