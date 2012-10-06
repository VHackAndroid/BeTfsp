package edu.vub.at.nfcpoker;

import java.io.IOException;
import java.util.TreeMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import android.app.Activity;
import android.util.Log;
import edu.vub.at.commlib.CommLib;
import edu.vub.at.commlib.CommLibConnectionInfo;
import edu.vub.at.nfcpoker.comm.PokerServer;

public class ConcretePokerServer extends PokerServer  {
	
	Runnable exporterR = new Runnable() {	
		@Override
		public void run() {
			Log.d("PokerServer", "Starting export");
			String address = "192.168.1.106";
			String port = "" + CommLib.SERVER_PORT;
			CommLibConnectionInfo clci = new CommLibConnectionInfo(PokerServer.class.getCanonicalName(), new String[] {address, port});
			try {
				CommLib.export(clci);
			} catch (IOException e) {
				Log.e("PokerServer", "Exporter thread crashed", e);
			}
		}
	};
	
	Runnable serverR = new Runnable() {
		public void run() {
			try {
				Log.d("PokerServer", "Starting serverR");
				Server s = new Server();
				Kryo k = s.getKryo();
				k.setRegistrationRequired(false);
				s.bind(CommLib.SERVER_PORT);
				s.start();
				s.addListener(new Listener() {
					@Override
					public void connected(Connection c) {
						super.connected(c);
						Log.d("PokerServer", "Client connected: " + c.getRemoteAddressTCP());
						addClient(c);
					}
				});
			} catch (IOException e) {
				Log.e("PokerServer", "Server thread crashed", e);
			}
		};
	};
	
	public Runnable gameLoopR = new Runnable() {
		@Override
		public void run() {
			gameLoop();
		}
	};
	
	int nextClientID = 0;
	private TreeMap<Integer, Connection> newClients = new TreeMap<Integer, Connection>();
	private TreeMap<Integer, Connection> clientsInGame = new TreeMap<Integer, Connection>();
	
	public enum GameState {
		STOPPED, WAITING_FOR_PLAYERS, HOLE, FLOP, TURN, RIVER, END_OF_ROUND;
	};
	
	public GameState gameState;
	
	public boolean isGameRunning() {
		return gameState != GameState.STOPPED && gameState != GameState.WAITING_FOR_PLAYERS;
	}

	public ConcretePokerServer(Activity gui) {
	}
	
	public void addClient(Connection c) {
		synchronized(this) {
			newClients.put(nextClientID++, c);
		}
		if (newClients.size() >= 2 && !isGameRunning()) {
			Log.d("PokerServer", "Two or more clients connected, game can start");
			new Thread(gameLoopR).start();
		}
	}

	public void start() {		
		new Thread(serverR).start();
		new Thread(exporterR).start();
	}

	private void gameLoop() {
		synchronized(this) {
			clientsInGame.putAll(newClients);
			newClients.clear();
		}
	}
	
}
