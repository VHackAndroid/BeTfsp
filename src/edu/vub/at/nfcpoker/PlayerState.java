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

import com.esotericsoftware.kryonet.Connection;

import edu.vub.at.nfcpoker.comm.Message.ClientActionType;

public class PlayerState implements Comparable<PlayerState> {
	// Connection
	public transient volatile Connection connection;
	public volatile int clientId;
	
	// Player statistics
	public volatile int money;
	public volatile String name;
	public volatile int avatar;
	
	// Game-specific
	public volatile int gameMoney;
	public volatile Card[] gameHoleCards;
	
	// Round-specific (roundTable)
	public volatile ClientActionType roundActionType;
	public volatile int roundMoney;
	
	public PlayerState(Connection connection, int clientId, int money, String name, int avatar) {
		this.connection = connection;
		this.clientId = clientId;
		
		this.money = money;
		this.name = name;
		this.avatar = avatar;
		
		this.gameMoney = 0;
		this.gameHoleCards = null;
		
		this.roundActionType = ClientActionType.Unknown;
		this.roundMoney = 0;
	}

	// for kryo
	public PlayerState() {}
	
	public String toString() {
		return "Player ("+clientId+"): "+name+" - "+money+" - "+avatar+" - "+
				gameMoney+" - "+gameHoleCards+" - "+
				roundActionType+" - "+roundMoney;
	}

	@Override
	public int compareTo(PlayerState another) {
		PlayerState p2 = (PlayerState) another;
		return Integer.valueOf(p2.clientId).compareTo(clientId);
	}
}
