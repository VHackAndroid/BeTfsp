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

package edu.vub.at.nfcpoker.ui;

import java.util.List;

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

	@Override
	public void setPlayerButtons(PlayerState dealer, PlayerState smallBlind, PlayerState bigBlind) { }

	@Override
	public void showWinners(List<PlayerState> remainingPlayers, int chipsPool) { }

	@Override
	public void resetGame() {
		// TODO Auto-generated method stub
		
	}

}
