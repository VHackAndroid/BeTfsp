package edu.vub.at.nfcpoker;

//From http://www.dreamincode.net/forums/topic/116864-how-to-make-a-poker-game-in-java/

public class Card {
	private short rank, suit;

	private static String[] suits = { "hearts", "spades", "diamonds", "clubs" };
	private static String[] ranks  = { "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King" };

	public static String rankAsString( int __rank ) {
		return ranks[__rank];
	}

	Card(short suit, short rank) {
		this.rank=rank;
		this.suit=suit;
	}

	public @Override String toString() {
		  return ranks[rank] + " of " + suits[suit];
	}

	public short getRank() {
		 return rank;
	}

	public short getSuit() {
		return suit;
	}
}

