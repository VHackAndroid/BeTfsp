package edu.vub.at.nfcpoker;

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
import edu.vub.at.nfcpoker.comm.Message.FutureMessage;
import edu.vub.at.nfcpoker.comm.Message.SetClientParameterMessage;
import edu.vub.at.nfcpoker.comm.Message.SetIDMessage;
import edu.vub.at.nfcpoker.comm.Message.StateChangeMessage;
import edu.vub.at.nfcpoker.comm.PokerServer;
import edu.vub.at.nfcpoker.ui.ServerViewInterface;

public class ConcretePokerServer extends PokerServer  {
	
	@SuppressWarnings("serial")
	public class RoundEndedException extends Exception {}

	int nextClientID = 0;
	private boolean isDedicated = true;
	private GameLoop gameLoop;
	private String broadcastAddress;
	private String serverAddress;
	private ConcurrentSkipListMap<Integer, Connection> connections = new ConcurrentSkipListMap<Integer, Connection>();

	public ConcretePokerServer(ServerViewInterface gui, boolean isDedicated, String serverAddress, String broadcastAddress) {
		this.gameLoop = new GameLoop(gui);
		this.isDedicated = isDedicated;
    	this.serverAddress = serverAddress;
    	this.broadcastAddress = broadcastAddress;
	}
	
	Runnable exporterR = new Runnable() {	
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
					CommLib.export(clci, broadcastAddress);
				} catch (IOException e) {
					Log.e("wePoker - Server", "Export failed", e);
				}
				try { Thread.sleep(2000);
				} catch (InterruptedException e) { }
			}
		}
	};
	
	Runnable serverR = new Runnable() {
		public void run() {
			try {
				Log.d("wePoker - Server", "Starting server thread");
				Server s = new Server();
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
		new Thread(serverR).start();
		if (broadcastAddress != null)
			new Thread(exporterR).start();
	}

	public void addClient(Connection c) {
		Log.d("wePoker - Server", "Adding client " + c.getRemoteAddressTCP());
		connections.put(nextClientID, c);
		c.sendTCP(new StateChangeMessage(gameLoop.gameState));
		c.sendTCP(new SetIDMessage(nextClientID));
		nextClientID++;
	}
	
	public void registerClient(Connection c, String nickname, int avatar, int money) {
		for (Integer i : connections.keySet()) {
			if (connections.get(i) == c) {
				gameLoop.addPlayerInformation(c, i, nickname, avatar, money);
				return;
			}
		}
	}
	
	public void removeClient(Connection c) {
		Log.d("wePoker - Server", "Client removed: " + c);
		for (Integer i : connections.keySet()) {
			if (connections.get(i) == c) {
				gameLoop.removeClientInGame(i);
				connections.remove(i);
				return;
			}
		}
	}
	
	public enum GameState {
		STOPPED, WAITING_FOR_PLAYERS, PREFLOP, FLOP, TURN, RIVER, END_OF_ROUND;
		
		@Override
		public String toString() {
			switch (this) {
			case STOPPED:
				return "STOPPED";
			case WAITING_FOR_PLAYERS:
				return "Waiting for other players";
			case PREFLOP:
				return "Pre-flop";
			case FLOP:
				return "Flop";
			case TURN:
				return "Turn";
			case RIVER:
				return "River";
			case END_OF_ROUND:
				return "Round ended";
			default:
				return "";
			}
		}
	};
}

