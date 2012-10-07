package edu.vub.at.nfcpoker;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import edu.vub.at.commlib.CommLib;
import edu.vub.at.commlib.CommLibConnectionInfo;
import edu.vub.at.commlib.Future;
import edu.vub.at.commlib.UUIDSerializer;
import edu.vub.at.nfcpoker.comm.Message;
import edu.vub.at.nfcpoker.comm.Message.ClientAction;
import edu.vub.at.nfcpoker.comm.Message.ClientActionMessage;
import edu.vub.at.nfcpoker.comm.Message.ClientActionType;
import edu.vub.at.nfcpoker.comm.Message.FutureMessage;
import edu.vub.at.nfcpoker.comm.Message.ReceiveHoleCardsMessage;
import edu.vub.at.nfcpoker.comm.Message.ReceivePublicCards;
import edu.vub.at.nfcpoker.comm.Message.RequestClientActionFutureMessage;
import edu.vub.at.nfcpoker.comm.Message.RoundWinnersDeclarationMessage;
import edu.vub.at.nfcpoker.comm.Message.StateChangeMessage;
import edu.vub.at.nfcpoker.comm.Message.SetIDMessage;
import edu.vub.at.nfcpoker.comm.PokerServer;
import edu.vub.at.nfcpoker.ui.ServerActivity;
import edu.vub.at.nfcpoker.ui.ServerViewInterface;

public class ConcretePokerServer extends PokerServer  {
	
	@SuppressWarnings("serial")
	public class RoundEndedException extends Exception {}

	private boolean isDedicated = false;

	Runnable exporterR = new Runnable() {	
		@Override
		public void run() {
			Log.d("PokerServer", "Starting export");
			String address = "192.168.1.106";
			String port = "" + CommLib.SERVER_PORT;
			String dedicated = "" + isDedicated;
			CommLibConnectionInfo clci = new CommLibConnectionInfo(PokerServer.class.getCanonicalName(), new String[] {address, port, dedicated});
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
				k.register(UUID.class, new UUIDSerializer());
				s.bind(CommLib.SERVER_PORT);
				s.start();
				s.addListener(new Listener() {
					@Override
					public void connected(Connection c) {
						super.connected(c);
						Log.d("PokerServer", "Client connected: " + c.getRemoteAddressTCP());
						gameLoop.addClient(c);
					}
					
					@Override
					public void received(Connection c, Object msg) {
						super.received(c, msg);
						if (msg instanceof FutureMessage) {
							FutureMessage fm = (FutureMessage) msg;
							Log.d("PokerServer", "Resolving future " + fm.futureId + "(" + CommLib.futures.get(fm.futureId) + ") with value " + fm.futureValue);
							CommLib.resolveFuture(fm.futureId, fm.futureValue);
						}
					}
					
					@Override
					public void disconnected(Connection c) {
						super.disconnected(c);
						Log.d("PokerServer", "Client disconnected: " + c.getRemoteAddressTCP());
						gameLoop.removeClient(c);
					}
				});
			} catch (IOException e) {
				Log.e("PokerServer", "Server thread crashed", e);
			}
		};
	};
	
	int nextClientID = 0;

	private ServerViewInterface gui;
	
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

	public ConcretePokerServer(ServerViewInterface gui, boolean isDedicated) {
		this.gui = gui;
		this.isDedicated = isDedicated;
	}
	
	public void start() {		
		new Thread(serverR).start();
		new Thread(exporterR).start();
	}
	
	private GameLoop gameLoop = new GameLoop();
	
	class GameLoop implements Runnable {
		
		private static final int INITIAL_MONEY = 2000;

		public GameLoop() {
			gameState = GameState.STOPPED;
			Collections.shuffle(names);
		}

		// todo: what if client disconnects before next round?
		public void removeClient(Connection c) {
			Log.d("PokerServer", "Client disconnected: " + c);
			synchronized(this) {
				for (Integer i : clientsInGame.keySet()) {
					if (clientsInGame.get(i) == c) {
						clientsInGame.remove(i);
						Future<ClientAction> fut = actionFutures.get(i);
						if (fut != null && ! fut.isResolved()) 
							fut.resolve(new ClientAction(Message.ClientActionType.Fold, 0));
						return;
					}
				}
				
				for (Integer i : newClients.keySet()) {
					if (newClients.get(i) == c) {
						newClients.remove(i);
						return;
					}
				}
			}
		}

		public TreeMap<Integer, Connection> newClients = new TreeMap<Integer, Connection>();
		public TreeMap<Integer, Connection> clientsInGame = new TreeMap<Integer, Connection>();
		public TreeMap<Integer, Future<ClientAction>> actionFutures = new TreeMap<Integer, Future<ClientAction>>();  
		public TreeMap<Integer, Integer> playerMoney = new TreeMap<Integer, Integer>();
		public TreeMap<Integer, String> playerNames = new TreeMap<Integer, String>();

		public GameState gameState;
		int chipsPool = 0;
			
		public void run() {
			while (true) {
				gui.resetCards();
				chipsPool = 0;
				synchronized(this) {
					actionFutures.clear();
					for (Integer id : newClients.navigableKeySet()) {
						setupPlayer(id, newClients.get(id));
					}
					newClients.clear();
					for (Integer i : clientsInGame.keySet()) {
						if (clientsInGame.get(i) == null)
							clientsInGame.remove(i);
					}
					if (clientsInGame.size() < 2) {
						try {
							Log.d("PokerServer", "# of clients < 2, changing state to stopped");
							newState(GameState.WAITING_FOR_PLAYERS);
							this.wait();
						} catch (InterruptedException e) {
							Log.wtf("PokerServer", "Thread was interrupted");
						}
					}
					//todo what if clientsInGame.size drops below two?
				}
				
				TreeMap<Integer, Card[]> holeCards = new TreeMap<Integer, Card[]>();
				Set<Card> cardPool = new HashSet<Card>();
				try {
					Deck deck = new Deck();
					
					// hole cards
					newState(GameState.PREFLOP);
					for (Integer clientNum : clientsInGame.navigableKeySet()) {
						Card preflop[] = deck.drawCards(2);
						holeCards.put(clientNum, preflop);
						Connection c = clientsInGame.get(clientNum);
						c.sendTCP(new ReceiveHoleCardsMessage(preflop[0], preflop[1]));
					}
					createActionFutures();
					roundTable();
					
					
					// flop cards
					Card[] flop = deck.drawCards(3);
					cardPool.addAll(Arrays.asList(flop));
					gui.revealCards(flop);
					newState(GameState.FLOP);
					broadcast(new ReceivePublicCards(flop));
					createActionFutures();
					roundTable();

					// turn cards
					Card[] turn = deck.drawCards(1);
					cardPool.add(turn[0]);
					gui.revealCards(turn);
					newState(GameState.TURN);
					broadcast(new ReceivePublicCards(turn));
					createActionFutures();
					roundTable();
					
					// river cards
					Card[] river = deck.drawCards(1);
					cardPool.add(river[0]);
					gui.revealCards(river);
					newState(GameState.RIVER);
					broadcast(new ReceivePublicCards(river));
					createActionFutures();
					roundTable();					
				} catch (RoundEndedException e1) {
					/* ignore */
					Log.d("PokerServer", "Everybody folded at round " + gameState);
				}
				
				// results
				boolean endedPrematurely = gameState != GameState.RIVER;
				newState(GameState.END_OF_ROUND);
				
				Set<Integer> remainingPlayers = new HashSet<Integer>();
				for (Integer player : actionFutures.navigableKeySet()) {
					Future<ClientAction> fut = actionFutures.get(player);
					if (fut != null
							&& fut.isResolved()
							&& fut.unsafeGet().getClientActionType() != Message.ClientActionType.Fold) {
						remainingPlayers.add(player);
					}
				}
				
				if (endedPrematurely) {
					if (remainingPlayers.size() == 1) {
						addMoney(remainingPlayers.iterator().next(), chipsPool);
						broadcast(new RoundWinnersDeclarationMessage(remainingPlayers, null));
					}
				} else {
					TreeMap<Integer, Hand> hands = new TreeMap<Integer, Hand>();
					for (Integer player : remainingPlayers) {
							hands.put(player, Hand.makeBestHand(cardPool, Arrays.asList(holeCards.get(player))));
					}
					
					if (!hands.isEmpty()) {
						Iterator<Integer> it = hands.keySet().iterator();
						Set<Integer> bestPlayers = new HashSet<Integer>();
						Integer firstPlayer = it.next();
						bestPlayers.add(firstPlayer);
						Hand bestHand = hands.get(firstPlayer);
						
						while (it.hasNext()) {
							int nextPlayer = it.next();
							Hand nextHand = hands.get(nextPlayer);
							int comparison = nextHand.compareTo(bestHand);
							if (comparison > 0)  {
								bestHand = nextHand;
								bestPlayers.clear();
								bestPlayers.add(nextPlayer);
							} else if (comparison == 0) {
								bestPlayers.add(nextPlayer);
							}
						}
						
						for (Integer player: bestPlayers)
							addMoney(player, chipsPool / bestPlayers.size());
						broadcast(new RoundWinnersDeclarationMessage(bestPlayers, bestHand));
					}
				}
				
				// finally, sleep
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					Log.wtf("PokerServer", "Thread.sleep was interrupted", e);
				}
			}
		}

		private void addMoney(Integer player, int chips) {
			int current = playerMoney.get(player);
			current += chips;
			playerMoney.put(player, current);
			gui.setPlayerMoney(player, current);
		}

		private void setupPlayer(Integer id, Connection connection) {
			final String name = generateName(id);
			playerNames.put(id, name);
			playerMoney.put(id, INITIAL_MONEY);
			gui.addPlayer(id, name, INITIAL_MONEY);
			
			clientsInGame.put(id, connection);
		}

		public void createActionFutures() {
			for (Integer i : clientsInGame.navigableKeySet()) {
				Future<ClientAction> oldFut = actionFutures.get(i);
				if (oldFut != null && oldFut.isResolved() && oldFut.unsafeGet().getClientActionType() == ClientActionType.Fold) {
					actionFutures.put(i, oldFut);
				} else {
					Future<ClientAction> fut = CommLib.createFuture();
					actionFutures.put(i, fut);
					Log.d("PokerServer", "Creating & Sending new future " + fut.getFutureId() + " to " + i);
					clientsInGame.get(i).sendTCP(new RequestClientActionFutureMessage(fut));
					if (oldFut != null && !oldFut.isResolved())
						oldFut.setFutureListener(null);
				}
			}
		}

		@SuppressWarnings("unchecked")
		public void roundTable() throws RoundEndedException {
			TreeMap<Integer, Future<ClientAction>> clonedFutures;
			synchronized (this) {
				clonedFutures = (TreeMap<Integer, Future<ClientAction>>) actionFutures.clone(); 
			}
			
			int playersRemaining = 0;
			
			for (Integer i: clonedFutures.navigableKeySet()) {
				Future<ClientAction> fut = clonedFutures.get(i);
				ClientAction ca = fut.get();
				broadcast(new ClientActionMessage(ca, i));
				switch (ca.type) { // todo
				case Fold: 
					break;
				case Bet:
					addMoney(i, -ca.getExtra());
					addChipsToPool(ca.getExtra());
				default:
					playersRemaining++;
				}
			}
			if (playersRemaining <= 1)
				throw new RoundEndedException();
		}

		private void addChipsToPool(int extra) {
			chipsPool += extra;
		}

		private void newState(GameState newState) {
			gameState = newState;
			broadcast(new StateChangeMessage(newState));
			gui.showStatechange(newState);
		}
		
		private void broadcast(Message m) {
			for (Connection c : clientsInGame.values())
				if (c != null)
					c.sendTCP(m);
		}

		public void addClient(Connection c) {
			Log.d("PokerServer", "Adding client " + c.getRemoteAddressTCP());
			synchronized(this) {
				newClients.put(nextClientID, c);
			}
			c.sendTCP(new StateChangeMessage(gameLoop.gameState));
			c.sendTCP(new SetIDMessage(nextClientID));
			nextClientID++;
			if (newClients.size() >= 2) {
				if (gameState == GameState.STOPPED) {
					Log.d("PokerServer", "Two or more clients connected, game can start");
					new Thread(this).start();
				} else if (gameState == GameState.WAITING_FOR_PLAYERS) {
					synchronized(this) { this.notifyAll(); }
				}
			}
		}
	}
	
	public static ArrayList<String> names = new ArrayList<String>(Arrays.asList( "Dries", "Lode", "Elisa", "Wolf", "Tom", "Kevin", "Andoni" ));

	public static String generateName(int id) {
		return names.get(id % names.size())+"(" + id + ")";
	}
}
