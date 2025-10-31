

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

    // Constructor , specifying playerIndex and output filepath
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
        //Perform atomic action
        while (!Thread.currentThread().isInterrupted()) {
            //We are using same lock ordering, lock left first then lock right
            try{
                leftDeck.lockthis();
                //lockthis() will call the re-entrant lock of leftdeck to lockInterruptibly() which is interruptible lock
            } catch (InterruptedException e){
                //When any player win, they will call CardGame.interruptAllThread() 
                //lockThis() will throw interruptedException, when the thread is in Blocked State and is interrupted via CardGame.InterruptAllThread()
                break;
            }
            //Right deck must use try lock, if right deck use lockthis(), deadlock will happen sooner or later.
            //Right deck if use lockthis(), will mean that the left deck will still be locked in the mean time, disrupting the flow of the game
            if (rightDeck.tryLock()) {
               
                //If leftdeck is empty we must release both left and right deck to avoid dead lock.
                if (leftDeck.isEmpty()) {
                    rightDeck.unlock();
                    leftDeck.unlock();
                } else { 
                    //Guard against making move when if someone is already won
                    if (CardGame.whoWon != null){
                        break;
                    }
                    // Automatically withdrawn from left and discard to right (this is atomic action)
                    this.withDrawnCard();
                    this.discardingCard();

                    //Another guard to avoid registering the move played if someone have already won the game
                    if (CardGame.whoWon != null){
                        break;
                    }
                    //If you won the game we will interrupt all thread including those that are blocked (waiting for leftDeck lock)
                    if (isWon()) {
                        CardGame.interruptAllThread();
                        break;
                    }
                    //if all goes well with withdrawing and discarding, we can now release both lock
                    leftDeck.unlock();
                    rightDeck.unlock();
                }
            } else {
                // If we can not lock the right deck, we release the left deck, we do not want to hold the left deck while waiting for right deck to be release as it can cause dead lock
                leftDeck.unlock();
            }
            
            /*
            Avoid Greedy Player
            Read more in Report
            */            
            try{
                Thread.sleep(30L);
            }  catch (InterruptedException e){
                break;
            }
        }
        //When we break out of the while loop, or Thread.isInterrupted is True we know that someone has won
        //Our last job is to write the very ending of each player output file
        writeLastLine();
       
    }

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
        //log action to the output file
        writeDatatoFile("player " + playerIndex + " draws a " + topCard.getValue() + " from deck " + leftDeck.getDeckIndex() + "\n");
    }

    public void discardingCard(){
        ArrayDeque<Card> rightDeckCardList = rightDeck.getCardList();
        //Get the number that are not the player preference
        Card differentCard = getDifferCard();
        // From specification we will dispose the card to the bottom of the deck
        rightDeckCardList.addLast(differentCard);
        //log the action
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

    // Return and remove a card from player hand, such that the card number != player preferred denomination
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
    //Use for logging the player hand after each atomic withdraw and discard move
    public String getPlayerHand(){
        String message = "";

        for (Card eachCard : playerCard){
            message += " " + eachCard.getValue();
        }
        return message;
    }

    //get the player hand in arrayList format, use for the initialization of the game, by add card to player hand (round robin)
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
        //If you won then set volatile flag:
        CardGame.whoWon = this;
        System.out.println("player " + playerIndex + " wins");
        writeDatatoFile("player " + playerIndex + " wins" + "\n");
        return true;
    }
}
