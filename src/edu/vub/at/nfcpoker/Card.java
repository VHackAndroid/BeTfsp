package edu.vub.at.nfcpoker;

//From http://www.dreamincode.net/forums/topic/116864-how-to-make-a-poker-game-in-java/

public class Card {
	public short rank, suit;

	private static String[] suits = { "hearts", "spades", "diamonds", "clubs" };
	private static String[] ranks  = { "a", "2", "3", "4", "5", "6", "7", "8", "9", "10", "j", "q", "k" };

	public static String rankAsString( int __rank ) {
		return ranks[__rank];
	}
	
	// For cryo
	Card() {}

	public Card(short suit, short rank) {
		this.rank=rank;
		this.suit=suit;
	}

	public @Override String toString() {
		  String s = suits[suit];
		  return s + "_" + ranks[rank] + s.substring(0, 1);
	}

	public short getRank() {
		 return rank;
	}

	public short getSuit() {
		return suit;
	}
}

