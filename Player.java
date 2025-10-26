
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class Player implements Runnable {
    public int playerIndex;
    private Deck leftDeck;
    private Deck rightDeck;
    private String report;
    private  BufferedWriter writetoFile;
    ArrayList<Card> playerCard = new ArrayList<>();

    // Constructor and Setter function , nothing special
    public Player(int playerIndex, String filepath){
        this.playerIndex = playerIndex;
        try {
            writetoFile = new BufferedWriter(new FileWriter(filepath));
        } catch (IOException e){
            System.out.println("Can not create the file");
        }
    }
    
    public void setLeftDeck(Deck leftDeck){
        this.leftDeck = leftDeck;
    }
    public void setRightDeck(Deck rightDeck){
        this.rightDeck = rightDeck;
    }

    @Override
    public void run(){
       
        //This is atomic action Get card from left, discard to right
        while (!Thread.currentThread().isInterrupted()) {
            // Try to acquire both left and right deck by locking left first and then locking right 
            leftDeck.lockthis();
            if (rightDeck.tryLock()) {
                
                //If leftdeck is empty we must release both left and right deck to avoid dead lock.
                if (leftDeck.isEmpty()) {
                    rightDeck.unlock();
                    leftDeck.unlock();
                } else {
                    
                    if (CardGame.whoWon != null){
                        leftDeck.unlock();
                        rightDeck.unlock();
                        break;
                    }
                    // Automatically withdrawn from left and discard to right
                    this.withDrawnCard();
                    this.discardingCard();

                    if (CardGame.whoWon != null){
                        leftDeck.unlock();
                        rightDeck.unlock();
                        break;
                    }
                    
                    if (isWon()) {
                        CardGame.whoWon = this;
                        System.out.println("Player Index : " + playerIndex + " Win");
                        writeDatatoFile("Player Index : " + CardGame.whoWon.playerIndex + "has won the game and hence interrupt this thread to stop playing");
                        CardGame.checkSum();
                        leftDeck.unlock();
                        rightDeck.unlock();
                        break;
                    }
                    //Once we finish with withdrawing and discarding, we can now release both lock
                    leftDeck.unlock();
                    rightDeck.unlock();
                }
            } else {
                leftDeck.unlock();
            }
            if (CardGame.whoWon != null){
            break;
            }
            /*
            * We start a player thread by using for loop which mean that player at the start of playerArr in CardGame are more likely to start playing first
            * For example : Player 1 are very more likely to play first before Player 5
            *             : Player 1 will have more chance to lock their left and right deck again , due to how to the thread scheduler work
            *             : To make sure player 3 , 4, 5, have more chance to play evenly we can use time out, so that there is a time, where other thread may join
            */            
            try{
                Thread.sleep(50L);
            }  catch (InterruptedException e){
                    Thread.currentThread().interrupt();
            }
        }
        System.out.println("Player Index : " + playerIndex + " thread has stopped");
        writeDatatoFile(CardGame.whoWon.playerIndex + " won the game and has interrupt this thread to stop");
       
        
    }
    //Withdrawn a card from the left deck
    public void withDrawnCard(){
        ArrayDeque<Card> leftDeckCardList = leftDeck.getCardList();
        // Use pollFirst to return and also remove the card from the queue
        Card topCard = leftDeckCardList.pollFirst();
        playerCard.add(topCard);
        writeDatatoFile("Player " + playerIndex + " withDrawn a " + topCard.getValue() + " from left deck left deck is now : " + leftDeckCardList + "player hand is : " + playerCard);
    }

    public void discardingCard(){
        ArrayDeque<Card> rightDeckCardList = rightDeck.getCardList();
        Card differentCard = getDifferCard();
        // From specification we will dispose the card to the bottom of the deck
        rightDeckCardList.addLast(differentCard);
        writeDatatoFile(" and then discard " + differentCard.getValue() + " to the right deck right deck is now " + rightDeckCardList  + "player hand is : " + playerCard +  "\n");
    }

    // A custom method for writing data to file, this save time so we don't have to keep doing try catch 
    public void writeDatatoFile(String message){
        try {
            writetoFile.write(message);
            writetoFile.flush();
        } catch (IOException e) {
            System.out.println("Can not write the message to file");
        }
    }

    // Return and remove a card from player hand, such that a card is undesirable for player
    public Card getDifferCard(){
        for (Card eachCard : playerCard){
            if (eachCard.getValue() != playerIndex){
                playerCard.remove(eachCard);
                return eachCard;
            }
        }
        // This will not be called, don't worry
        return null;
    }

    public Deck getLeftDeck(){
        return leftDeck;
    }
  
    public Deck getRightDeck(){
        return rightDeck;
    }
    //return player hand
    public ArrayList<Card> getPlayerCard(){
        return this.playerCard;
    }

    // Check to see if player contain 4 card  with the same number
    public boolean isWon(){
        int firstValue = playerCard.get(0).getValue();
        for (Card eachCard : playerCard){
            if (eachCard.getValue() != firstValue){
                return false;
            }
        }
        return true;
    }
}
