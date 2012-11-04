package edu.vub.at.nfcpoker.comm;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import com.esotericsoftware.minlog.Log;

import edu.vub.at.commlib.Future;
import edu.vub.at.nfcpoker.Card;
import edu.vub.at.nfcpoker.GameState;
import edu.vub.at.nfcpoker.PlayerState;
import edu.vub.at.nfcpoker.Hand;

public interface Message {

	enum ClientActionType { Bet, Fold, Check, AllIn, Unknown };

	public static final class ClientAction {
		public ClientActionType actionType;
		public int roundMoney;
		public int extraMoney;

		public ClientAction(ClientActionType actionType) {
			this(actionType, 0, 0);
		}
		public ClientAction(ClientActionType actionType, int roundMoney, int extraMoney) {
			this.actionType = actionType;
			this.roundMoney = roundMoney;
			this.extraMoney = extraMoney;
		}

		// for kryo
		public ClientAction() {}

		@Override
		public String toString() {
			switch (actionType) {
			case Fold: case Check:
				return actionType.toString();
			case Bet: case AllIn:
				return actionType.toString() + "(" + roundMoney + " / " + extraMoney + ")";
			default:
				Log.warn("wePoker - Message", "Unsupported action type");
				return actionType.toString() + "(" + roundMoney + " / " + extraMoney + ")";
			}
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
		public int round;

		public RequestClientActionFutureMessage(Future<?> f, int round_) {
			futureId = f.getFutureId();
			round = round_;
		}

		// kryo
		public RequestClientActionFutureMessage() {}

		@Override
		public String toString() {
			return super.toString() + ": Future message for " + futureId + ". Round: " + round + ".";
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

		public Set<PlayerState> bestPlayers;
		public Set<String> bestPlayerNames;
		public boolean showCards;
		public Hand bestHand;
		public int chips;

		public RoundWinnersDeclarationMessage(Set<PlayerState> bestPlayers, Set<String> bestNames, boolean showCards, Hand bestHand, int amountOfChips) {
			this.bestPlayers = bestPlayers;
			this.bestPlayerNames = bestNames;
			this.showCards = showCards;
			this.bestHand = bestHand;
			this.chips = amountOfChips;
		}

		// kryo
		public RoundWinnersDeclarationMessage() {}

		@Override
		public String toString() {
			return super.toString() + ": Round winners" + this.bestPlayers.toString();
		}

		public String winMessageString() {
			String s = "" + chips + "chips won by ";
			Iterator<String> playersIt = bestPlayerNames.iterator();
			while (playersIt.hasNext()) {
				s = s + " - " + playersIt.next();
			}
			return s;
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

	public static class SmallBlindMessage extends TimestampedMessage {

		public int clientId;
		public int amount;

		public SmallBlindMessage(int clientId, int amount) {
			this.clientId = clientId;
			this.amount = amount;
		}

		// kryo
		public SmallBlindMessage() {}

		@Override
		public String toString() {
			return super.toString() + ": Client small blind information message, client -> " + amount;
		}
	}

	public static class BigBlindMessage extends TimestampedMessage {

		public int clientId;
		public int amount;

		public BigBlindMessage(int clientId, int amount) {
			this.clientId = clientId;
			this.amount = amount;
		}

		// kryo
		public BigBlindMessage() {}

		@Override
		public String toString() {
			return super.toString() + ": Client big blind information message, client -> " + amount;
		}
	}

	public static class PoolMessage extends TimestampedMessage {

		public int poolMoney;

		public PoolMessage(int poolMoney) {
			this.poolMoney = poolMoney;
		}

		// kryo
		public PoolMessage() {}

		@Override
		public String toString() {
			return super.toString() + ": Client pool money information message -> " + poolMoney;
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

	public static class SetClientParameterMessage extends TimestampedMessage {

		public String nickname;
		public int avatar;
		public int money;

		public SetClientParameterMessage(String nickname, int avatar, int money) {
			this.nickname = nickname;
			this.avatar = avatar;
			this.money = money;
		}

		// kryo
		public SetClientParameterMessage() {}

		@Override
		public String toString() {
			return super.toString() + ": Client parameter information message, "+
					"nickname -> " + nickname+ ", "+
					"avatar -> " + avatar + ", "+
					"money -> " + money;
		}
	}
}
