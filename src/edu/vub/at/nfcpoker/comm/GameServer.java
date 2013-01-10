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

package edu.vub.at.nfcpoker.comm;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import edu.vub.at.commlib.CommLib;
import edu.vub.at.commlib.CommLibConnectionInfo;
import edu.vub.at.commlib.UUIDSerializer;
import edu.vub.at.nfcpoker.PokerGame;
import edu.vub.at.nfcpoker.comm.Message.SetIDMessage;
import edu.vub.at.nfcpoker.comm.Message.FutureMessage;
import edu.vub.at.nfcpoker.comm.Message.SetClientParameterMessage;
import edu.vub.at.nfcpoker.comm.Message.SetNicknameMessage;
import edu.vub.at.nfcpoker.ui.ServerViewInterface;

public class GameServer extends PokerServer  {

	protected Server currentServer;
	private int nextClientID = 0;
	
	private PokerGame gameLoop;
	private boolean isDedicated;
	private String serverAddress;
	private String broadcastAddress;
	private ConcurrentSkipListMap<Integer, Connection> connections = new ConcurrentSkipListMap<Integer, Connection>();
	
	public GameServer(ServerViewInterface gui, boolean isDedicated, String serverAddress, String broadcastAddress) {
		this.gameLoop = new PokerGame(gui);
		this.isDedicated = isDedicated;
    	this.serverAddress = serverAddress;
    	this.broadcastAddress = broadcastAddress;
	}
	
	Thread exporterThread = new Thread() {	
		@Override
		public void run() {
			while (true) {
				String port = "" + CommLib.SERVER_PORT;
				String dedicated = "" + isDedicated;
				Log.d("wePoker - Server", "Starting export thread, advertising " + broadcastAddress + ":" + port + " D:"+isDedicated);
				CommLibConnectionInfo clci = new CommLibConnectionInfo(
						PokerServer.class.getCanonicalName(),
						new String[] {serverAddress, port, dedicated});
				try {
					try {
						CommLib.export(clci, broadcastAddress);
					} catch (IOException e) {
						Log.e("wePoker - Server", "Export failed", e);
					}

					Thread.sleep(2000);
				} catch (InterruptedException e) {
					Log.e("wePoker - Server", "Interrupted - stopping", e);
					return;
				}
			}
		}
	};
	
	Thread serverThread = new Thread() {
		public void run() {
			try {
				Log.d("wePoker - Server", "Starting server thread");
				Server s = new Server();
				currentServer = s;
				Kryo k = s.getKryo();
				k.setRegistrationRequired(false);
				k.register(UUID.class, new UUIDSerializer());
				s.bind(CommLib.SERVER_PORT);
				s.start();
				s.addListener(new Listener() {
					@Override
					public void connected(Connection c) {
						super.connected(c);
						Log.d("wePoker - Server", "Client connected: " + c.getRemoteAddressTCP());
						addClient(c);
					}
					
					@Override
					public void received(Connection c, Object msg) {
						super.received(c, msg);
						if (msg instanceof FutureMessage) {
							FutureMessage fm = (FutureMessage) msg;
							Log.d("wePoker - Server", "Resolving future " + fm.futureId + "(" + CommLib.futures.get(fm.futureId) + ") with value " + fm.futureValue);
							CommLib.resolveFuture(fm.futureId, fm.futureValue);
						}
						if (msg instanceof SetClientParameterMessage) {
							SetClientParameterMessage cm = (SetClientParameterMessage) msg;
							Log.d("wePoker - Server", "Got SetIDReplyMessage: "+cm.toString());
							registerClient(c, cm.nickname, cm.avatar, cm.money);
							gameLoop.broadcast(cm);
						}
						
						if (msg instanceof SetNicknameMessage) {
							SetNicknameMessage snm = (SetNicknameMessage) msg;
							Log.d("wePoker - Server", "Got SetNicknameMessage: "+snm.toString());
							setNickname(c, snm.nickname);
							gameLoop.broadcast(snm);
						}
					}
					
					@Override
					public void disconnected(Connection c) {
						super.disconnected(c);
						Log.d("wePoker - Server", "Client disconnected: " + c);
						removeClient(c);
					}
				});
			} catch (IOException e) {
				Log.e("wePoker - Server", "Server thread crashed", e);
			}
		};
	};
	

	public void start() {		
		Log.d("wePoker - Server", "Starting server and exporter threads...");
		serverThread.start();
		gameLoop.start();
		if (broadcastAddress != null)
			exporterThread.start();
	}
	
	public void stop() {
		if (gameLoop.isFinished())
			return;
		currentServer.stop();
		gameLoop.finish();
		if (exporterThread.isAlive())
			exporterThread.interrupt();
	}

	public void addClient(Connection c) {
		Log.d("wePoker - Server", "Adding client " + c.getRemoteAddressTCP());
		connections.put(nextClientID, c);
		c.sendTCP(new SetIDMessage(nextClientID));
		nextClientID++;
	}
	
	public void registerClient(Connection c, String nickname, int avatar, int money) {
		for (Integer i : connections.keySet()) {
			if (connections.get(i) == c) {
				gameLoop.addPlayer(c, i, nickname, avatar, money);
				return;
			}
		}
	}
	
	public void setNickname(Connection c, String nickname) {
		for (Integer i : connections.keySet()) {
			if (connections.get(i) == c) {
				gameLoop.setNickname(i, nickname);
				return;
			}
		}
	}
	
	public void removeClient(Connection c) {
		//Log.d("wePoker - Server", "Client removed: " + c);
		for (Integer i : connections.keySet()) {
			if (connections.get(i) == c) {
				gameLoop.removePlayer(i);
				connections.remove(i);
				return;
			}
		}
	}
	
	public void reset() {
		gameLoop.reset();
	}
}

