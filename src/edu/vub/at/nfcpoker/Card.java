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

