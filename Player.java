
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Player implements Runnable {
    public int playerIndex;
    private Deck leftDeck;
    private Deck rightDeck;
    private String report;
    private  BufferedWriter writetoFile;
    ArrayList<Card> PlayerCard = new ArrayList<>();

    // Constructor and Setter function , nothing special
    public Player(int playerIndex){
        this.playerIndex = playerIndex;
        try {
            writetoFile = new BufferedWriter(new FileWriter("player" + playerIndex + "_output.txt"));
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
    public void run() {
        //This is atomic action Get card from left, discard to right
        while (!Thread.currentThread().isInterrupted()) {
            // Try to acquire both left and right deck by locking left first and then locking right 
            leftDeck.lockthis();
                if (rightDeck.tryLock()) {
                    // if we realize that left deck have no card to be withdrawn, we must release both left and right deck, releasing only left deck and waiting for it to be filled while right deck is locked
                    // can cause deadlock

                    if (leftDeck.isEmpty()) {
                        rightDeck.unlock();
                        leftDeck.unlock();
                    } else {
                        //Given that we successfully lock both left and right deck, we can not perform atomic withdrawal and discard to right deck

                        if (CardGame.whoWon == null){
                            Card justTaken = leftDeck.withDrawnCard(this);
                            report = "Player " + playerIndex  + " original hand is " + PlayerCard + " has drawn " + justTaken.getValue() + " leftDeck become " + leftDeck.getCardList();
                            PlayerCard.add(justTaken);
                            Card inputCard = getDifferCard();
                            rightDeck.discarded(inputCard, this);
                            report = report + " Player discarded " + inputCard.getValue() + " to right deck, right deck become : " + rightDeck.getCardList() + "player hand is " + PlayerCard; 
                            if (CardGame.whoWon == null){
                                try {
                                    writetoFile.write(report);
                                    writetoFile.newLine();
                                    
                                } catch (IOException e) {
                                    System.out.println("Can not write to file");
                                }
                                System.out.println(report);
                            }

                        } else{
                            leftDeck.unlock();
                            rightDeck.unlock();
                            break;
                        }
                        
                        if (CardGame.whoWon == null && isWon()) {
                            System.out.println("Player Index : " + playerIndex + " Win");
                            try {
                                writetoFile.write("Player" + playerIndex + " won the game");
                            } catch (IOException e) {
                            }

                            CardGame.whoWon = this;
                            CardGame.checkSum();
                        }

                        //Once we finish with withdrawing and discarding, we can now release this lock
                        
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
                Thread.sleep(60L);
            }  catch (InterruptedException e){
                    Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Exit");
        try {
            writetoFile.write(CardGame.whoWon.playerIndex + " has interrupt this player because that player" + CardGame.whoWon.playerIndex + " won");
            writetoFile.flush();
        } catch (IOException e) {
        }
    }
    //Return player hand
    public ArrayList<Card> getPlayerCard(){
        return this.PlayerCard;
    }

    // return and remove a card from player hand , such that card value != prefered denomination
    public Card getDifferCard(){
        for (Card eachCard : PlayerCard){
            if (eachCard.getValue() != playerIndex){
                PlayerCard.remove(eachCard);
                return eachCard;
            }
        }
        // This will not be called, don't worry
        return null;
    }

    // Check to see if player contain 4 card  with the same number
    public boolean isWon(){
        int firstValue = PlayerCard.getFirst().getValue();
        for (Card eachCard : PlayerCard){
            if (eachCard.getValue() != firstValue){
                return false;
            }
        }
        return true;
    }

    //Getter function, nothing special
    public Deck getLeftDeck(){
        return leftDeck;
    }

    public Deck getRightDeck(){
        return rightDeck;
    }
}
