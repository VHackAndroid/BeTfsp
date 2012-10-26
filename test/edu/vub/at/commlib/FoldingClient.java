package edu.vub.at.commlib;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import edu.vub.at.nfcpoker.Card;
import edu.vub.at.nfcpoker.ConcretePokerServer.GameState;
import edu.vub.at.nfcpoker.comm.Message.ReceiveHoleCardsMessage;
import edu.vub.at.nfcpoker.comm.Message.ReceivePublicCards;
import edu.vub.at.nfcpoker.comm.PokerServer;

public class FoldingClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CommLibConnectionInfo clci = CommLib.discover(PokerServer.class, args[0]);
			System.out.println("Discovered server at " + clci.getAddress());
			clci.connect(new Listener() {
				@Override
				public void received(Connection c, Object m) {
					super.received(c, m);
					
					System.out.println("Received message " + m.toString());
					
					if (m instanceof GameState) {
						GameState newGameState = (GameState) m;
						switch (newGameState) {
			            	case STOPPED:
			            		System.out.println("Game state changed to STOPPED");
			            		break;
			            	case WAITING_FOR_PLAYERS:
			            		System.out.println("Game state changed to WAITING_FOR_PLAYERS");
			            		break;
			            	case PREFLOP:
			            		System.out.println("Game state changed to PREFLOP");
			            		break;
			            	case FLOP:
			            		System.out.println("Game state changed to FLOP");
			            		break;
			            	case TURN:
			            		System.out.println("Game state changed to TURN");
			            		break;
			            	case RIVER:
			            		System.out.println("Game state changed to RIVER");
			            		break;
			            	case END_OF_ROUND:
			            		System.out.println("Game state changed to END_OF_ROUND");
			            		break;
						}
					}
					
					if (m instanceof ReceivePublicCards) {
						ReceivePublicCards newPublicCards = (ReceivePublicCards) m;
						System.out.print("Received public cards: ");
						Card[] cards = newPublicCards.cards;
						for (int i = 0; i < cards.length; i++) {
							System.out.print(cards[i].toString() + ", ");
						}
						System.out.println();
					}
					
					if (m instanceof ReceiveHoleCardsMessage) {
						ReceiveHoleCardsMessage newHoleCards = (ReceiveHoleCardsMessage) m;
						System.out.print("Received hand cards: " + newHoleCards.toString());
					}
						
				}
			});
			while (true) {
				Thread.sleep(10000);
			}
		} catch (IOException e) {
			System.err.println("Could not discover server: ");
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	

}
