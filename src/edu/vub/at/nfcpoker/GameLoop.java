package edu.vub.at.nfcpoker;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;

import android.util.Log;

import com.esotericsoftware.kryonet.Connection;

import edu.vub.at.commlib.CommLib;
import edu.vub.at.commlib.Future;
import edu.vub.at.nfcpoker.ConcretePokerServer.GameState;
import edu.vub.at.nfcpoker.ConcretePokerServer.RoundEndedException;
import edu.vub.at.nfcpoker.comm.Message;
import edu.vub.at.nfcpoker.comm.Message.ClientAction;
import edu.vub.at.nfcpoker.comm.Message.ClientActionMessage;
import edu.vub.at.nfcpoker.comm.Message.ClientActionType;
import edu.vub.at.nfcpoker.comm.Message.ReceiveHoleCardsMessage;
import edu.vub.at.nfcpoker.comm.Message.ReceivePublicCards;
import edu.vub.at.nfcpoker.comm.Message.RequestClientActionFutureMessage;
import edu.vub.at.nfcpoker.comm.Message.RoundWinnersDeclarationMessage;
import edu.vub.at.nfcpoker.comm.Message.SetIDMessage;
import edu.vub.at.nfcpoker.comm.Message.StateChangeMessage;
import edu.vub.at.nfcpoker.ui.ServerViewInterface;

class GameLoop implements Runnable {
	
	// Game settings
	private static final int INITIAL_MONEY = 2000;
	private static final int DELAY_GAME_START = 5000;

	// Blinds
	private static final int SMALL_BLIND = 5;
	private static final int BIG_BLIND = 10;
	
	// Communication
	private ConcurrentSkipListMap<Integer, Future<ClientAction>> actionFutures = new ConcurrentSkipListMap<Integer, Future<ClientAction>>();  

	// Connections
	private ConcurrentSkipListMap<Integer, Connection> clientsInGame = new ConcurrentSkipListMap<Integer, Connection>();
	
	// Rounds
	public volatile GameState gameState;
	private Vector<Integer> clientsIdsInRoundOrder = new Vector<Integer>();
	private ConcurrentSkipListMap<Integer, Integer> playerMoney = new ConcurrentSkipListMap<Integer, Integer>();
	private int chipsPool = 0;
	private Timer delayGameStart;
	
	// GUI
	private ServerViewInterface gui;
	
	public GameLoop(ServerViewInterface gui) {
		this.gameState = GameState.STOPPED;
		this.gui = gui;
	}
	
	public void run() {
		while (true) {
			chipsPool = 0;
			gui.resetCards();
			updatePoolMoney();
			synchronized(this) {
				actionFutures.clear();
				for (Integer i : clientsInGame.keySet()) {
					if (clientsInGame.get(i) == null)
						removeClientInGame(i);
				}
				if (clientsInGame.size() < 2) {
					try {
						Log.d("wePoker - Server", "# of clients < 2, changing state to stopped");
						newState(GameState.WAITING_FOR_PLAYERS);
						this.wait();
					} catch (InterruptedException e) {
						Log.wtf("wePoker - Server", "Thread was interrupted");
					}
				}
			}
			
			TreeMap<Integer, Card[]> holeCards = new TreeMap<Integer, Card[]>();
			Set<Card> cardPool = new HashSet<Card>();
			try {
				Deck deck = new Deck();
				
				// hole cards
				for (Integer clientNum : clientsInGame.navigableKeySet()) {
					Card preflop[] = deck.drawCards(2);
					holeCards.put(clientNum, preflop);
					Connection c = clientsInGame.get(clientNum);
					c.sendTCP(new ReceiveHoleCardsMessage(preflop[0], preflop[1]));
				}
				newState(GameState.PREFLOP);
				// Small and big blind
				addBet(clientsIdsInRoundOrder.get(0), SMALL_BLIND);
				broadcast(new Message.SmallBlindMessage(clientsIdsInRoundOrder.get(0), SMALL_BLIND));
				addBet(clientsIdsInRoundOrder.get(1), BIG_BLIND);
				broadcast(new Message.BigBlindMessage(clientsIdsInRoundOrder.get(1), BIG_BLIND));
				// Do a round
				roundTable();
				
				
				// flop cards
				Card[] flop = deck.drawCards(3);
				cardPool.addAll(Arrays.asList(flop));
				gui.revealCards(flop);
				broadcast(new ReceivePublicCards(flop));
				newState(GameState.FLOP);
				roundTable();

				// turn cards
				Card[] turn = deck.drawCards(1);
				cardPool.add(turn[0]);
				gui.revealCards(turn);
				broadcast(new ReceivePublicCards(turn));
				newState(GameState.TURN);
				roundTable();
				
				// river cards
				Card[] river = deck.drawCards(1);
				cardPool.add(river[0]);
				gui.revealCards(river);
				broadcast(new ReceivePublicCards(river));
				newState(GameState.RIVER);
				roundTable();					
			} catch (RoundEndedException e1) {
				/* ignore */
				Log.d("wePoker - Server", "Everybody folded at round " + gameState);
			}
			
			// results
			boolean endedPrematurely = gameState != GameState.RIVER;
			newState(GameState.END_OF_ROUND);
			
			Set<Integer> remainingPlayers = new HashSet<Integer>();
			for (Integer player : actionFutures.navigableKeySet()) {
				Future<ClientAction> fut = actionFutures.get(player);
				if (fut != null
						&& fut.isResolved()
						&& fut.unsafeGet().actionType != Message.ClientActionType.Fold) {
					remainingPlayers.add(player);
				}
			}
			
			if (endedPrematurely) {
				if (remainingPlayers.size() == 1) {
					addMoney(remainingPlayers.iterator().next(), chipsPool);

					HashSet<String> winnerNames = new HashSet<String>();
					winnerNames.add(playerNames.get(remainingPlayers.iterator().next()));
					
					broadcast(new RoundWinnersDeclarationMessage(remainingPlayers, winnerNames, null, chipsPool));
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
					
					HashSet<String> winnerNames = new HashSet<String>();
					for (Integer player: bestPlayers) {
						addMoney(player, chipsPool / bestPlayers.size());
						winnerNames.add(playerNames.get(player));
					}
					
					broadcast(new RoundWinnersDeclarationMessage(bestPlayers, winnerNames, bestHand, chipsPool));
				}
			}
			
			cycleClientsInGame();
			
			// finally, sleep
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				Log.wtf("wePoker - Server", "Thread.sleep was interrupted", e);
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
		gui.addPlayer(id, name, INITIAL_MONEY);
		
		addClientInGame(id, connection);
	}
	
	private void addClientInGame(Connection connection, int clientID, String nickName, int initialMoney) {
		clientsInGame.put(clientID, connection);
		clientsIdsInRoundOrder.add(clientID);
		playerNames.put(clientID, name);
		playerMoney.put(clientID, INITIAL_MONEY);
		gui.addPlayer(clientID, nickName, initialMoney);
	}
	
	public synchronized void removeClientInGame(Integer clientId) {
		clientsInGame.remove(clientId);
		Future<ClientAction> fut = actionFutures.get(clientId);
		if (fut != null && ! fut.isResolved()) 
			fut.resolve(new ClientAction(Message.ClientActionType.Fold, 0, 0));
		gui.removePlayer(clientId);
	}
	
	private void cycleClientsInGame() {
		if (clientsIdsInRoundOrder.size() <= 1) return;
		clientsIdsInRoundOrder.add(clientsIdsInRoundOrder.elementAt(0));
		clientsIdsInRoundOrder.removeElementAt(0);
	}
	
	private void resetClientRoundActions() {
		// Reset the previous action (unless folded or AllIn)
		Iterator<Integer> clientsIterator = clientsIdsInRoundOrder.iterator();
		while (clientsIterator.hasNext()) {
			Integer i = clientsIterator.next();
			Future<ClientAction> oldFut = actionFutures.get(i);
			if (oldFut != null &&
				oldFut.isResolved() &&
				oldFut.unsafeGet().actionType == ClientActionType.Fold) {
				// Keep fold
			} else if (oldFut != null &&
				oldFut.isResolved() &&
				oldFut.unsafeGet().actionType == ClientActionType.AllIn) {
				// Keep all in
			} else {
				actionFutures.remove(i);
			}
		}
	}
	
	private Vector<Integer> getClientsRoundOrder() {
		@SuppressWarnings("unchecked")
		Vector<Integer> v = (Vector<Integer>) clientsIdsInRoundOrder.clone();
		if (gameState == GameState.PREFLOP) {
			// Move the players that have a small or big blind (only for preflop)
			v.add(v.elementAt(0));
			v.removeElementAt(0);
			v.add(v.elementAt(0));
			v.removeElementAt(0);
		}
		return v;
	}
	
	private void askBets(Vector<Integer> clients, Map<Integer, ClientAction> clientAction) {
		for (int c : clients) {
			boolean askClient = true;
			Future<ClientAction> oldFut = actionFutures.get(c);
			// Check if the previous action is still valid
			if (oldFut != null &&
					oldFut.isResolved() &&
					oldFut.unsafeGet().actionType == ClientActionType.Fold) {
				// If the player folds, keep fold
				askClient = false;
			} else if (oldFut != null &&
					oldFut.isResolved() &&
					oldFut.unsafeGet().actionType == ClientActionType.Bet) {
				// TODO minbet en prev bets! Client sends Diff!
				// If the player bet is OK, keep it &&
				oldFut.unsafeGet().extra >= minBet
				askClient = false;
				askClient = false;
			} else if (oldFut != null &&
					oldFut.isResolved() &&
					oldFut.unsafeGet().actionType == ClientActionType.AllIn) {
				// If the player is all in, keep it
				ca = oldFut.unsafeGet();
			} else {
				Future<ClientAction> fut = CommLib.createFuture();
				actionFutures.put(i, fut);
				Log.d("wePoker - Server", "Creating & Sending new future " + fut.getFutureId() + " to " + i);
				Connection c = clientsInGame.get(i);
				if (c == null) {
					// If client disconnected -> Fold
					broadcast(new ClientActionMessage(new ClientAction(ClientActionType.Fold), i));
					break;
				}
				c.sendTCP(new RequestClientActionFutureMessage(fut, r));
				if (oldFut != null && !oldFut.isResolved())
					oldFut.setFutureListener(null);
				ca = fut.get();
			}
			if (ca == null) continue;
		}
	}
	
	private void addBet(int clientId, int amount) {
		clientBets.set(clientId, amount);
		addMoney(clientId, -amount);
		addChipsToPool(amount);
	}
	
	private void verifyBets(Vector<Integer> clients, Map<Integer, ClientAction> clientActions) {
		
	}
	
	private void roundTable() throws RoundEndedException {
		
		int minBet = 0;
		boolean increasedBet = true;
		
		resetClientRoundActions();

		if (clientsIdsInRoundOrder.size() < 2) {
			throw new RoundEndedException();
		}

		Vector<Integer> clientOrderAfterBlinds = getClientsRoundOrder();
		Map<Integer, ClientAction> clientActions = new TreeMap<Integer, ClientAction>();
		
		while (!verifyBets(clientOrderAfterBlinds, clientActions)) {
			askBets(clientOrderAfterBlinds, clientBet);
		}
		
		// Two table rounds if needed
		for (int r = 0; r < 2 && increasedBet; r++) {
			increasedBet = false;
			int playersRemaining = clientsIdsInRoundOrderAfterBlinds.size();
			Iterator<Integer> clientsIterator2 = clientsIdsInRoundOrderAfterBlinds.iterator();
			while (clientsIterator2.hasNext()) {
				Integer i = clientsIterator2.next();
				while (true) {
					ClientAction ca;
					Future<ClientAction> oldFut = actionFutures.get(i);
					// Check if the previous action is still valid
					if (oldFut != null &&
							oldFut.isResolved() &&
							oldFut.unsafeGet().actionType == ClientActionType.Fold) {
						// If the player folds, keep fold
						ca = oldFut.unsafeGet();
					} else if (oldFut != null &&
							oldFut.isResolved() &&
							oldFut.unsafeGet().actionType == ClientActionType.Bet &&
							oldFut.unsafeGet().extra >= minBet) {
						// If the player bet is OK, keep it
						ca = oldFut.unsafeGet();
					} else if (oldFut != null &&
							oldFut.isResolved() &&
							oldFut.unsafeGet().actionType == ClientActionType.AllIn) {
						// If the player is all in, keep it
						ca = oldFut.unsafeGet();
					} else {
						Future<ClientAction> fut = CommLib.createFuture();
						actionFutures.put(i, fut);
						Log.d("wePoker - Server", "Creating & Sending new future " + fut.getFutureId() + " to " + i);
						Connection c = clientsInGame.get(i);
						if (c == null) {
							// If client disconnected -> Fold
							broadcast(new ClientActionMessage(new ClientAction(ClientActionType.Fold), i));
							break;
						}
						c.sendTCP(new RequestClientActionFutureMessage(fut, r));
						if (oldFut != null && !oldFut.isResolved())
							oldFut.setFutureListener(null);
						ca = fut.get();
					}
					if (ca == null) continue;
					
					switch (ca.type) {
					case Fold: 
						broadcast(new ClientActionMessage(ca, i));
						playersRemaining--;
						break;
					case Check: // And CALL (client sends diffMoney!)
						broadcast(new ClientActionMessage(ca, i));
						addMoney(i, -ca.getExtra());
						addChipsToPool(ca.getExtra());
						break;
					case AllIn: // Client sends diffMoney
						if (ca.getExtra() > minBet) {
							minBet = ca.extra;
							increasedBet = true; // ask for call or fold in second round
						}
						broadcast(new ClientActionMessage(ca, i));
						addMoney(i, -ca.getExtra());
						addChipsToPool(ca.getExtra());
						break;
					case Bet:
						if (minBet > ca.getExtra()) {
							actionFutures.remove(i);
							continue; // ask for a new bet
						}
						broadcast(new ClientActionMessage(ca, i));
						if (ca.getExtra() > minBet) {
							minBet = ca.extra;
							increasedBet = true; // ask for call or fold in second round
						}
						//minBet = ca.getExtra();
						addMoney(i, -ca.getExtra());
						addChipsToPool(ca.getExtra());
						break;
					default:
						Log.d("wePoker - Server", "Unknown client action message");
						broadcast(new ClientActionMessage(ca, i));
					}
					break;
				}
				if (playersRemaining <= 1)
					throw new RoundEndedException();
			}
		}
	}
	
	
	private void addChipsToPool(int extra) {
		chipsPool += extra;
		updatePoolMoney();
	}

	private void updatePoolMoney() {
		broadcast(new Message.PoolMessage(chipsPool));
		gui.updatePoolMoney(chipsPool);
	}

	private void newState(GameState newState) {
		gameState = newState;
		broadcast(new StateChangeMessage(newState));
		gui.showStateChange(newState);
	}

	public synchronized void addPlayerInformation(Connection c, int clientId, String nickname, int avatar, int money) {
		clientsInGame.put(clientId, c);
		playerMoney.put(clientId, money);
		gui.setPlayerNickname(clientId, nickname);
		gui.setPlayerAvatar(clientId, avatar);
		gui.setPlayerMoney(clientId, money);
	}
	
	public synchronized void removePlayerInformation(int clientId) {
		clientsInGame.remove(clientId);
		gui.removePlayer(clientId);
	}
	
	public synchronized void broadcast(Message m) {
		for (Connection c : clientsInGame.values())
			if (c != null)
				c.sendTCP(m);
	}
}
