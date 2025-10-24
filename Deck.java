import java.util.ArrayDeque;
import java.util.concurrent.locks.ReentrantLock;

public class Deck {
    private final int deckPriority;
    private final ReentrantLock deckLock = new ReentrantLock();

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

    public void lockthis(){
        try {
            deckLock.lockInterruptibly();
          
        } catch (InterruptedException e) {
        }
    }

    public ArrayDeque<Card> getDeckCardList(){
        return cardList;
    }
    public void receiveCard(Card inputCard){
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
