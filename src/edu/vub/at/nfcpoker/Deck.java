package edu.vub.at.nfcpoker;

//From http://www.dreamincode.net/forums/topic/116864-how-to-make-a-poker-game-in-java/

import java.util.Random;
import java.util.ArrayList;

public class Deck {
	private ArrayList<Card> cards;

	public Deck() {
		cards = new ArrayList<Card>();

		for (short suit=0; suit<=3; suit++) {
			for (short rank=0; rank<=12; rank++) {
			   cards.add(new Card(suit,rank));
			 }
		}

		shuffle();
	}

	public void shuffle() {
		Random generator = new Random();

		for (int idx = cards.size() - 1; idx > 0; idx--) {
			int victim = generator.nextInt(idx + 1);

			Card temp = (Card) cards.get(victim);
			cards.set(victim , cards.get(idx));
			cards.set(idx, temp);
		}
	}

	public Card drawFromDeck() {	   
		return cards.remove( cards.size()-1 );
	}

	public int getTotalCards() {
		return cards.size();  //we could use this method when making a complete poker game to see if we needed a new deck
	}

	public Card[] drawCards(int i) {
		Card[] ret = new Card[i];
		for (int j = 0; j < i; i++)
			ret[i] = drawFromDeck();
		
		return ret;
	}
} 

