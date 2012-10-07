package edu.vub.at.nfcpoker;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HandWithScore implements Comparable<HandWithScore>{

	@Override
	public int compareTo(HandWithScore other) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static HandWithScore makeBestHand(Set<Card> base, Collection<Card> holeCards) {
		Set<Card> pool = new HashSet<Card>(base);
		pool.addAll(holeCards);
		// todo create all hands and fid the best one.
		return null;
	}

	public Object getScore() {
		// TODO Auto-generated method stub
		return null;
	}

}
