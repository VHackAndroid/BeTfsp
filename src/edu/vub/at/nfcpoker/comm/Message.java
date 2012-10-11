package edu.vub.at.nfcpoker.comm;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import edu.vub.at.commlib.Future;
import edu.vub.at.nfcpoker.Card;
import edu.vub.at.nfcpoker.ConcretePokerServer.GameState;
import edu.vub.at.nfcpoker.Hand;

public interface Message {

	enum ClientActionType { Bet, Fold, Check };

	public static final class ClientAction {
		public ClientActionType type;
		public int extra;
		
		public ClientAction(ClientActionType type) {
			this(type, 0);
		}
		public ClientAction(ClientActionType type, int extra) {
			this.type = type;
			this.extra = extra;
		}
		
		// for kryo
		public ClientAction() {}
		
		@Override
		public String toString() {
			switch (type) {
			case Fold: case Check:
				return type.toString();
			default:
				return type.toString() + "(" + extra + ")";
			}
		}
		
		public ClientActionType getClientActionType(){
			return type;
		}
		
		public int getExtra(){
			return extra;
		}
				
	}

	public static abstract class TimestampedMessage implements Message {
		public long timestamp;
			
		public TimestampedMessage() {
			timestamp = new Date().getTime();
		}
		
		@Override
		public String toString() {
			return this.getClass().getSimpleName() + "@" + timestamp;
		}
	}
	
	public static final class StateChangeMessage extends TimestampedMessage {
		public GameState newState;

		public StateChangeMessage(GameState newState_) {
			newState = newState_;
		}
		
		// for kryo
		public StateChangeMessage() {}

		@Override
		public String toString() {
			return super.toString() + ": State change to " + newState;
		}
	}
	
	public static class ReceiveHoleCardsMessage extends TimestampedMessage {
		public Card card1, card2;
		
		public ReceiveHoleCardsMessage(Card one, Card two) {
			card1 = one;
			card2 = two;
		}
		
		//kryo
		public ReceiveHoleCardsMessage() {}
		
		@Override
		public String toString() {
			return super.toString() + ": Receive cards [" + card1 + ", " + card2 + "]";
		}
	}
	
	public static class ReceivePublicCards extends TimestampedMessage {
		public Card[] cards;
		
		public ReceivePublicCards(Card[] cards_) {
			cards = cards_;
		}
		
		//kryo
		public ReceivePublicCards() {}
		
		@Override
		public String toString() {
			StringBuilder cardsStr = new StringBuilder(": Receive cards [");
			cardsStr.append(cards[0].toString());
			for (int i = 1; i < cards.length; i++)
				cardsStr.append(", ").append(cards[i].toString());
			
			return super.toString() + cardsStr.toString() + "]";
		}
	}
	
	public static class FutureMessage extends TimestampedMessage {
		public UUID futureId;
		public Object futureValue;
		
		public FutureMessage(UUID futureId_, Object futureValue_) {
			futureId = futureId_;
			futureValue = futureValue_;
		}

		// kryo
		public FutureMessage() {}

		@Override
		public String toString() {
			return super.toString() + ": Resolve " + futureId + " with " + futureValue;
		}
	}
	
	public static class RequestClientActionFutureMessage extends TimestampedMessage {
		public UUID futureId;
		
		public RequestClientActionFutureMessage(Future<?> f) {
			futureId = f.getFutureId();
		}
		
		// kryo
		public RequestClientActionFutureMessage() {}
		
		@Override
		public String toString() {
			return super.toString() + ": Future message for " + futureId;
		}
	}

	public static class ClientActionMessage extends TimestampedMessage {
		
		public int userId;
		public ClientAction action;
		
		public ClientActionMessage(ClientAction action, int id) {
			this.action = action;
			this.userId = id;
		}

		// kryo
		public ClientActionMessage() {}
		
		public ClientAction getClientAction(){
			return action;
		}
		
		@Override
		public String toString() {
			return super.toString() + ": Client action information message, client" + userId + " -> " + action.toString();
		}
	}
	

	 public class RoundWinnersDeclarationMessage extends TimestampedMessage implements Message {
			
			public Set<Integer> bestPlayers;
			public Hand bestHand;
			public int chips;

			public RoundWinnersDeclarationMessage(Set<Integer> bestPlayers, Hand bestHand, int amountOfChips) {
				this.bestPlayers = bestPlayers;
				this.bestHand = bestHand;
				this.chips = amountOfChips;
			}

			// kryo
			public RoundWinnersDeclarationMessage() {}
			
			@Override
			public String toString() {
				return super.toString() + ": Round winners" + this.bestPlayers.toString();
			}
	}

	public static class ToastMessage extends TimestampedMessage {
		
		public String message;
		
		public ToastMessage(String message) {
			this.message = message;
		}

		// kryo
		public ToastMessage() {}
		
		@Override
		public String toString() {
			return super.toString() + ": Client toast information message -> " + message;
		}
	}

	public static class CheatMessage extends TimestampedMessage {

		public int amount;
		
		public CheatMessage(int amount) {
			this.amount = amount;
		}

		// kryo
		public CheatMessage() {}
		
		@Override
		public String toString() {
			return super.toString() + ": Client cheat information message, client -> " + amount;
		}
	}

	public static class NicknameMessage extends TimestampedMessage {

		public String nickname;
		
		public NicknameMessage(String nickname) {
			this.nickname = nickname;
		}

		// kryo
		public NicknameMessage() {}
		
		@Override
		public String toString() {
			return super.toString() + ": Client nickname information message, client -> " + nickname;
		}
	}
	 
	 public class SetIDMessage extends TimestampedMessage implements Message {

			public int id;

			public SetIDMessage(Integer id) {
				this.id = id;
			}

			// kryo
			public SetIDMessage() {}
			
			@Override
			public String toString() {
				return super.toString() + ": set ID to " + this.id;
			}
	}
}
