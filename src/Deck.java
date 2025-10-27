package src;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class Deck {
    private final int deckPriority;
    private final ReentrantLock deckLock = new ReentrantLock();
    private volatile ArrayDeque<Card> cardList = new ArrayDeque<>();
    private AtomicBoolean inUsed = new AtomicBoolean(false);
    private  BufferedWriter writetoFile;

    public Deck(int deckPriority, String filepath){
        this.deckPriority = deckPriority;
        try{
            writetoFile = new BufferedWriter(new FileWriter(filepath));
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public boolean tryLock(){
        if(deckLock.tryLock()){
           
            detectConcurrentAccess();
            return true;
        } else{
            return false;
        }
    }

    public void unlock(){
        // Very important must set the flag before unlock, otherwise we might get fake result suggesting concurrent access occur.
        inUsed.set(false);
        deckLock.unlock();
    }

    public void lockthis(){
        deckLock.lock();
        detectConcurrentAccess();
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

    private void detectConcurrentAccess(){
        if (!inUsed.compareAndSet(false, true)){
            throw new ConcurrentAccessException("Concurrent access Dectected");
        }
    }
    public void writeDeckContent(){
        try{
            writetoFile.write("Deck content : ");
            for (Card eachCard : cardList){
                writetoFile.write(eachCard.getValue() + " ");
            }
            writetoFile.flush();
            writetoFile.close();
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}
