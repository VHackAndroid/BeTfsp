package edu.vub.at.nfcpoker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;

import android.util.Log;

import com.esotericsoftware.kryonet.Connection;

import edu.vub.at.commlib.CommLib;
import edu.vub.at.commlib.Future;
import edu.vub.at.nfcpoker.comm.Message;
import edu.vub.at.nfcpoker.comm.Message.ClientAction;
import edu.vub.at.nfcpoker.comm.Message.ClientActionMessage;
import edu.vub.at.nfcpoker.comm.Message.ClientActionType;
import edu.vub.at.nfcpoker.comm.Message.ReceiveHoleCardsMessage;
import edu.vub.at.nfcpoker.comm.Message.ReceivePublicCards;
import edu.vub.at.nfcpoker.comm.Message.RequestClientActionFutureMessage;
import edu.vub.at.nfcpoker.comm.Message.RoundWinnersDeclarationMessage;
import edu.vub.at.nfcpoker.comm.Message.StateChangeMessage;
import edu.vub.at.nfcpoker.ui.ServerViewInterface;

public class GameLoop implements Runnable {
	
	@SuppressWarnings("serial")
	public class RoundEndedException extends Exception {}


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
	private ConcurrentSkipListMap<Integer, PlayerState> playerState  = new ConcurrentSkipListMap<Integer, PlayerState>();
	private int chipsPool = 0;
	
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
			
			Set<Card> cardPool = new HashSet<Card>();
			try {
				Deck deck = new Deck();
				
				// Reset player actions
				for (PlayerState player : playerState.values()) {
					player.gameMoney = 0;
					player.gameHoleCards = null;
				}
				
				// hole cards
				for (Integer clientNum : clientsInGame.navigableKeySet()) {
					Card preflop[] = deck.drawCards(2);
					playerState.get(clientNum).gameHoleCards = preflop;
					Connection c = clientsInGame.get(clientNum);
					c.sendTCP(new ReceiveHoleCardsMessage(preflop[0], preflop[1]));
				}
				newState(GameState.PREFLOP);
				// Small and big blind
				PlayerState smallBlind = playerState.get(clientsIdsInRoundOrder.get(0));
				addBet(smallBlind, SMALL_BLIND);
				broadcast(new Message.SmallBlindMessage(smallBlind.clientId, SMALL_BLIND));
				PlayerState bigBlind = playerState.get(clientsIdsInRoundOrder.get(1));
				addBet(bigBlind, BIG_BLIND);
				broadcast(new Message.BigBlindMessage(bigBlind.clientId, BIG_BLIND));
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
			
			Set<PlayerState> remainingPlayers = new HashSet<PlayerState>();
			for (PlayerState player : playerState.values()) {
				if (player.roundActionType != ClientActionType.Fold &&
					player.roundActionType != ClientActionType.Unknown) {
					remainingPlayers.add(player);
				}
			}
			
			if (endedPrematurely) {
				// If only one player left
				if (remainingPlayers.size() == 1) {
					addMoney(remainingPlayers.iterator().next(), chipsPool);

					HashSet<String> winnerNames = new HashSet<String>();
					winnerNames.add(remainingPlayers.iterator().next().name);
					
					broadcast(new RoundWinnersDeclarationMessage(remainingPlayers, winnerNames, false, null, chipsPool));
				} else {
					Log.wtf("wePoker - Server", "Ended prematurely with more than one player?");
				}
			} else {
				// Calculate who has the best cards
				TreeMap<PlayerState, Hand> hands = new TreeMap<PlayerState, Hand>();
				for (PlayerState player : remainingPlayers) {
					hands.put(player, Hand.makeBestHand(cardPool, Arrays.asList(player.gameHoleCards)));
				}
				if (!hands.isEmpty()) {
					Iterator<PlayerState> it = hands.keySet().iterator();
					Set<PlayerState> bestPlayers = new HashSet<PlayerState>();
					PlayerState firstPlayer = it.next();
					bestPlayers.add(firstPlayer);
					Hand bestHand = hands.get(firstPlayer);
					
					while (it.hasNext()) {
						PlayerState nextPlayer = it.next();
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
					for (PlayerState player: bestPlayers) {
						addMoney(player, chipsPool / bestPlayers.size());
						winnerNames.add(player.name);
					}
					
					broadcast(new RoundWinnersDeclarationMessage(bestPlayers, winnerNames, true, bestHand, chipsPool));
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
	
	public synchronized void removeClientInGame(Integer clientId) {
		clientsInGame.remove(clientId);
		PlayerState player = playerState.get(clientId);
		if (player != null) {
			player.roundActionType = ClientActionType.Fold;
		}
		Future<ClientAction> fut = actionFutures.get(clientId);
		if (fut != null && ! fut.isResolved()) {
			fut.resolve(new ClientAction(Message.ClientActionType.Fold, 0, 0));
		}
		gui.removePlayer(clientId);
	}
	
	private void cycleClientsInGame() {
		if (clientsIdsInRoundOrder.size() <= 1) return;
		clientsIdsInRoundOrder.add(clientsIdsInRoundOrder.elementAt(0));
		clientsIdsInRoundOrder.removeElementAt(0);
	}
	
	private Vector<Integer> getClientsRoundOrder() {
		@SuppressWarnings("unchecked")
		Vector<Integer> v = (Vector<Integer>) clientsIdsInRoundOrder.clone();
		if (v.size() < 2) return v;
		if (gameState == GameState.PREFLOP) {
			// Move the players that have a small or big blind (only for preflop)
			v.add(v.elementAt(0));
			v.removeElementAt(0);
			v.add(v.elementAt(0));
			v.removeElementAt(0);
		}
		return v;
	}
	
	private void askClientActions(int clientId, int round) {
		PlayerState player = playerState.get(clientId);
		if ((player.roundActionType == ClientActionType.Fold) ||
			(player.roundActionType == ClientActionType.AllIn)) {
			return;
		}
		
		Future<ClientAction> fut = CommLib.createFuture();
		actionFutures.put(clientId, fut);
		Log.d("wePoker - Server", "Creating & Sending new future " + fut.getFutureId() + " to " + clientId);
		Connection c = clientsInGame.get(clientId);
		if (c == null) {
			// If client disconnected -> Fold
			player.roundActionType = ClientActionType.Fold;
			broadcast(new ClientActionMessage(new ClientAction(ClientActionType.Fold), clientId));
			return;
		}
		c.sendTCP(new RequestClientActionFutureMessage(fut, round));
		if (fut != null && !fut.isResolved()) {
			fut.setFutureListener(null);
		}
	}
	
	private boolean verifyClientActions(int clientId, int round, int minBet) {
		PlayerState player = playerState.get(clientId);
		if ((player.roundActionType == ClientActionType.Fold) ||
			(player.roundActionType == ClientActionType.AllIn)) {
			return true;
		}
		
		Future<ClientAction> fut = actionFutures.get(clientId);
		if (fut == null) return true;
		ClientAction ca = fut.get();
		if (ca == null) return true;
		
		switch (ca.actionType) {
		case Unknown:
			return false;
		case Fold:
		case AllIn:
			// TODO Block all-in for round 2 (client side?)
			return true;
		case Check:
		case Bet:
			// Client sends diffMoney
			if (player.roundMoney + ca.extraMoney < minBet) {
				// Can happen when a player checks before another player bets
				// We should ask a new action to the player
				player.roundActionType = ClientActionType.Unknown;
				return false;
			} else {
				return true;
			}
		default:
			Log.d("wePoker - Server", "Unknown client action message (processClientActions)");
			return false;
		}
	}
	
	// Processes Client Actions
	// - Updates money
	// - Broadcasts actions
	// Returns minimum bet
	private int processClientActions(int clientId, int round, int minBet) {
		PlayerState player = playerState.get(clientId);
		if (player.roundActionType == ClientActionType.Fold ||
			player.roundActionType == ClientActionType.AllIn) {
			return minBet;
		}
		
		Future<ClientAction> fut = actionFutures.get(clientId);
		if (fut == null) {
			broadcast(new ClientActionMessage(new ClientAction(ClientActionType.Fold, player.roundMoney, 0), clientId));
			player.roundActionType = ClientActionType.Fold;
			return minBet;
		}
		ClientAction ca = fut.get();
		if (ca == null) {
			broadcast(new ClientActionMessage(new ClientAction(ClientActionType.Fold, player.roundMoney, 0), clientId));
			player.roundActionType = ClientActionType.Fold;
			return minBet;
		}

		broadcast(new ClientActionMessage(ca, clientId));
		player.roundActionType = ca.actionType;
		
		switch (player.roundActionType) {
		case Fold:
			gui.updatePlayerStatus(player);
			return minBet;
		case Check: // Or CALL
		case Bet:
		case AllIn:
			// Client sends diffMoney
			addBet(player, ca.extraMoney);
			if (player.roundMoney < minBet) {
				Log.wtf("wePoker - Server", "Invalid extra money");
				return minBet;
			}
			return player.roundMoney;
		default:
			Log.d("wePoker - Server", "Unknown client action message");
			return minBet;
		}
	}
	
	private void addBet(PlayerState player, int extra) {
		player.roundMoney += extra;
		player.money -= extra;
		gui.updatePlayerStatus(player);
		addChipsToPool(extra);
	}
	
	private void addMoney(PlayerState player, int extra) {
		player.money += extra;
		gui.updatePlayerStatus(player);
	}
	
	private void roundTable() throws RoundEndedException {
		int minBet = 0;
		boolean increasedBet = true;

		Vector<Integer> clientOrderAfterBlinds = getClientsRoundOrder();

		// Reset player actions
		for (PlayerState player : playerState.values()) {
			player.roundActionType = ClientActionType.Unknown;
			player.roundMoney = 0;
			actionFutures.remove(player.clientId);
		}

		if (clientOrderAfterBlinds.size() < 2) {
			throw new RoundEndedException();
		}
		
		// Two table rounds if needed
		for (int tableRound = 0; tableRound < 2 && increasedBet; tableRound++) {
			int playersRemaining = clientOrderAfterBlinds.size();
			increasedBet = false;
			
			// Ask the client actions (in parallel)
			for (int clientId : clientOrderAfterBlinds) {
				askClientActions(clientId, tableRound);
			}
			
			// Process the client action (one-by-one, in round order)
			for (int clientId : clientOrderAfterBlinds) {
				// Keep asking for valid input
				while (!verifyClientActions(clientId, tableRound, minBet)) {
					askClientActions(clientId, tableRound);
				}
				int newMinBet = processClientActions(clientId, tableRound, minBet);
				if (newMinBet > minBet) {
					increasedBet = true;
					minBet = newMinBet;
				}
				PlayerState player = playerState.get(clientId);
				if (player.roundActionType == ClientActionType.Fold) {
					playersRemaining--;
				}
				if (playersRemaining <= 1) {
					throw new RoundEndedException();
				}
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
		clientsIdsInRoundOrder.add(clientId);
		PlayerState player = new PlayerState(clientId, money, nickname, avatar);
		playerState.put(clientId, player);
		gui.addPlayer(player);
		c.sendTCP(new StateChangeMessage(gameState));
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
