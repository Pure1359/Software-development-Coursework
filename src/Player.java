package src;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class Player implements Runnable {
    public int playerIndex;
    private Deck leftDeck;
    private Deck rightDeck;
    private  BufferedWriter writetoFile;
    ArrayList<Card> playerCard = new ArrayList<>();

    // Constructor , specifying playerIndex and filepath
    public Player(int playerIndex, String filepath){
        this.playerIndex = playerIndex;
        try {
            writetoFile = new BufferedWriter(new FileWriter(filepath));
        } catch (IOException e){
            System.out.println("Can not create the file");
        }
    }
    // Setter for both left and right deck
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
            try{
                leftDeck.lockthis();
            } catch (InterruptedException e){
                break;
            }
            if (rightDeck.tryLock()) {
                
                //If leftdeck is empty we must release both left and right deck to avoid dead lock.
                if (leftDeck.isEmpty()) {
                    rightDeck.unlock();
                    leftDeck.unlock();
                } else {
                    
                    //Guard against continuing playing even if someone is already won
                    if (CardGame.whoWon != null){
                        leftDeck.unlock();
                        rightDeck.unlock();
                        break;
                    }
                    // Automatically withdrawn from left and discard to right (this is atomic action)
                    this.withDrawnCard();
                    this.discardingCard();

                    //Another guard to avoid registering the move played if someone have already won the game
                    if (CardGame.whoWon != null){
                        leftDeck.unlock();
                        rightDeck.unlock();
                        break;
                    }
                    //If you won the game release both lock, because we will interrupt all thread including those that are blocked (waiting for leftDeck lock)
                    if (isWon()) {
                        leftDeck.unlock();
                        rightDeck.unlock();
                        CardGame.interruptAllThread();
                        break;
                    }
                    //Once we finish with withdrawing and discarding, we can now release both lock
                    leftDeck.unlock();
                    rightDeck.unlock();
                }
            } else {
                // If we can not lock the right deck, we release the left deck, this reduce change of dead lock , as we are not locking the left deck forever (read more in report)
                leftDeck.unlock();
            }
            
            /*
            Avoid Greedy Player
            * We start a player thread by using for loop which mean that player at the start of playerArr in CardGame are more likely to start playing first
            * For example : Player 1 are very more likely to play first before Player 5
            *             : Player 1 will have more chance to lock their left and right deck again , due to how to the thread scheduler work
            *             : To make sure player 3 , 4, 5, have more chance to play evenly we can use time out, so that there is a time, where other thread may join
                          : Otherwise it might look like : Player play : 1 1 1 0 0 0 2 1 1 2 3 4 1 5 and so on, as you can see 3,4,5 are less likely to player early, even in reality they are allow to play
            */            
            try{
                Thread.sleep(30L);
            }  catch (InterruptedException e){
                    Thread.currentThread().interrupt();
            }
            
        }
        writeLastLine();
        

    }
    //This will write at the end of the last after someone win the game
    public void writeLastLine(){
        //The line will be different if you are the winner or the loser of the game
        //If you lose you get : 
        if (CardGame.whoWon.playerIndex != this.playerIndex){
            writeDatatoFile("player " + CardGame.whoWon.playerIndex + " has informed player " + this.playerIndex + " that player " + CardGame.whoWon.playerIndex + " has won\n");
            writeDatatoFile("player " + playerIndex  + " exits" + "\n");
            writeDatatoFile("player " + playerIndex + " hand:" + getPlayerHand());
        } else{
            //If you win you get ("final hand" instead of "hand") + (no interrupt message)
            writeDatatoFile("player " + playerIndex  + " exits" + "\n");
            writeDatatoFile("player " + playerIndex + " final hand:" + getPlayerHand());
        }
    }
    //Withdrawn a card from the left deck
    public void withDrawnCard(){
        ArrayDeque<Card> leftDeckCardList = leftDeck.getCardList();
        // Use pollFirst to return and also remove the card from the queue
        Card topCard = leftDeckCardList.pollFirst();
        playerCard.add(topCard);
        writeDatatoFile("player " + playerIndex + " draws a " + topCard.getValue() + " from deck " + leftDeck.getDeckIndex() + "\n");
    }

    public void discardingCard(){
        ArrayDeque<Card> rightDeckCardList = rightDeck.getCardList();
        //Get the number that are not the player preference
        Card differentCard = getDifferCard();
        // From specification we will dispose the card to the bottom of the deck
        rightDeckCardList.addLast(differentCard);
        writeDatatoFile("player " + playerIndex + " discards a " + differentCard.getValue() + " to deck " + rightDeck.getDeckIndex() + "\n");
        writeDatatoFile("player " + playerIndex + " current hand is" + getPlayerHand() + "\n");
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
    private Card getDifferCard(){
        for (Card eachCard : playerCard){
            if (eachCard.getValue() != playerIndex){
                playerCard.remove(eachCard);
                return eachCard;
            }
        }
        // This will not be called, don't worry
        return null;
    }
    //Getter method
    public Deck getLeftDeck(){
        return leftDeck;
    }
  
    public Deck getRightDeck(){
        return rightDeck;
    }
    //return player hand in string format
    public String getPlayerHand(){
        String message = "";

        for (Card eachCard : playerCard){
            message += " " + eachCard.getValue();
        }

        return message;
    }
    //get the player hand in arrayList format
    public ArrayList<Card> getPlayerCard(){
        return playerCard;
    }

    // Check to see if player contain 4 card  with the same number
    public boolean isWon(){
        int firstValue = playerCard.get(0).getValue();
        for (Card eachCard : playerCard){
            if (eachCard.getValue() != firstValue){
                return false;
            }
        }
        CardGame.whoWon = this;
        System.out.println("player " + playerIndex + " wins");
        writeDatatoFile("player " + playerIndex + " wins" + "\n");
        return true;
    }
}
