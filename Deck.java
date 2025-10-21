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

    public void lockThisDeck(){
        try {
            deckLock.lockInterruptibly();

        } catch (InterruptedException e) {
            // Thrown if other thread have won the Game
        }
    }
    public void unlockThisDeck(){
        deckLock.unlock();
    }


    public void withDrawnCard(Player player){

        try {
            deckLock.lockInterruptibly();
            // Check to see if the deck if empty, if it is you release this lock and wait for other thread to fill up 
            while (cardList.isEmpty()){
                IsEmptyCondition.await();
            }
            player.getPlayerCard().add(cardList.pollFirst());

        } catch (InterruptedException ex) {

        }
    }

    public void receiveCard(Card inputCard){
        cardList.addLast(inputCard);
        IsEmptyCondition.signalAll();
    }

    public int getDeckPriority(){
        return deckPriority;
    }
}
