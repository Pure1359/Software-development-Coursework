import java.util.ArrayDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Deck {
    private final int deckPriority;
    private ReentrantLock deckLock = new ReentrantLock();
    private Condition IsEmptyCondition = deckLock.newCondition();
    private volatile ArrayDeque<Card> cardList = new ArrayDeque<>();

    public Deck(int deckPriority){
        this.deckPriority = deckPriority;
    }

    public boolean tryLock(){
        return deckLock.tryLock();
    }

    public void unlock(){
        deckLock.unlock();
    }


    public void withDrawnCard(Player player){
        if (CardGame.whoWon != null){
            return;
        }
        Card polledCard = cardList.pollFirst();
        player.getPlayerCard().add(polledCard);
        System.out.println("Player " + player.playerIndex + " withdraw a " + polledCard + " left deck become " + cardList + "player hand is " + player.getPlayerCard());
    }

    public void receiveCard(Card inputCard){
        cardList.addLast(inputCard);
    }

    public void discarded(Card inputCard){
        if (CardGame.whoWon != null){
            return;
        }
        cardList.addLast(inputCard);
    }

    public int getDeckPriority(){
        return deckPriority;
    }

    public ArrayDeque<Card> getCardList(){
        return cardList;
    }

    public boolean isEmpty(){
        return (cardList.isEmpty());
    }
}
