/*
 * wePoker: Play poker with your friends, wherever you are!
 * Copyright (C) 2012, The AmbientTalk team.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

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
		for (int j = 0; j < i; j++)
			ret[j] = drawFromDeck();
		
		return ret;
	}
} 

