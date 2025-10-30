package src;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class Deck {
    private final int deckIndex;
    //Use Reentrantlock for each deck
    private final ReentrantLock deckLock = new ReentrantLock();
    //Use ArrayDeque (not thread safe) simplify production code and allow easy adding to top and discard to bottom
    private volatile ArrayDeque<Card> cardList = new ArrayDeque<>();
    //Use to help detect concurrent access during testing (2 or more thread access this deck at the same time)
    private AtomicBoolean inUsed = new AtomicBoolean(false);

    private  BufferedWriter writetoFile;

    public Deck(int deckIndex, String filepath){
        //Set the deckIndex
        this.deckIndex = deckIndex;
        try{
            //Create a new file 
            writetoFile = new BufferedWriter(new FileWriter(filepath));
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    //Getter
    public int getDeckIndex(){
        return deckIndex;
    }
    //Try lock method : If can gain : return true and then gain lock
    //                  If not then : return false, and stop attempting to gain lock (no blocked state)
    public boolean tryLock(){
        if(deckLock.tryLock()){  
            //Check if there are any other thread accessing this same deck     
            detectConcurrentAccess();
            return true;
        } else{
            return false;
        }
    }

    public void unlock(){
        // Very important we must set the flag to false before unlock, if not then calling unlock first allow other thread to access this deck before setting inUsed to false
        // This is not really a ConcurrentAccess error, but the detectConcurrentAccess() will flag this as error when in reality it is not
        inUsed.set(false);
        deckLock.unlock();
        
    }

    public void lockthis() throws InterruptedException{
        //The thread will never know if someone won the game, if it keep blocked for access to this deck, hence this lock need to be interruptible
        deckLock.lockInterruptibly();
        detectConcurrentAccess();
    }
    //Use for receiving the card on the initialization (round robin card distribution the deck)
    public void receiveCard(Card inputCard){
        cardList.addLast(inputCard);
    }
    //Getter for cardList for this deck
    public ArrayDeque<Card> getCardList(){
        return cardList;
    }
    //Check if deck is empty or not (help prevent dead lock, see player thread run method)
    public boolean isEmpty(){
        return (cardList.isEmpty());
    }

    private void detectConcurrentAccess(){
        //IF there are only 1 player , then their left and right deck refer to the same object in heap, hence we will get ConcurrentAcessException (which is not an issues).
        if(CardGame.playerArr.length == 1){
            return;
        }
        //if some other thread is using this deck, then throw ConcurrentAccessException
        //if not set the flag to true (Thread.currentThread() is the only thread that is using this deck)
        if (!inUsed.compareAndSet(false, true)){
            throw new ConcurrentAccessException("Concurrent access Dectected");
        }
    }
    //Write the remain deck content for at the end of game
    public void writeDeckContent(){
        try{
            writetoFile.write("deck" + deckIndex + " contents:");
            for (Card eachCard : cardList){
                writetoFile.write(" " + eachCard.getValue());
            }
            writetoFile.flush();
            writetoFile.close();
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}
