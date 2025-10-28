package src;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class Deck {
    //Deck must have lock
    private final ReentrantLock deckLock = new ReentrantLock();
    //Use arraydeque (not inherently thread safe) because deck is FIFO .
    private volatile ArrayDeque<Card> cardList = new ArrayDeque<>();
    //Use to help detect concurrent access during testing (2 or more thread acess this deck at the same time)
    private AtomicBoolean inUsed = new AtomicBoolean(false);
    private  BufferedWriter writetoFile;

    public Deck(int deckIndex, String filepath){
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
        // Very important we must set the flag to false before unlock, otherwise if we set the flag after unlock, might get fake result suggesting concurrent access occur even if it not.
        inUsed.set(false);
        deckLock.unlock();
    }

    public void lockthis() throws InterruptedException{
        //The thread will never know if someone won the game, if it keep waiting for access to this deck, hence this lock need to be interruptable
        deckLock.lockInterruptibly();
    }

    
    
    public void receiveCard(Card inputCard){
        cardList.addLast(inputCard);
    }
    //Getter for cardList for this deck
    public ArrayDeque<Card> getCardList(){
        return cardList;
    }

    public boolean isEmpty(){
        return (cardList.isEmpty());
    }

    private void detectConcurrentAccess(){
        //IF there are only 1 player , then their left and right deck refer to the same object in heap, hence we will get ConcurrentAcessException (which is not an issues).
        if(CardGame.playerArr.length == 1){
            return;
        }
        //if some other thread is using this deck, then throw and ConccurrentAccessException
        //if not set the flag to true (Thread.currentThread() is the only thread that is using this deck)
        if (!inUsed.compareAndSet(false, true)){
            throw new ConcurrentAccessException("Concurrent access Dectected");
        }
    }
    //Write the remain deck content for at the end of game
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
