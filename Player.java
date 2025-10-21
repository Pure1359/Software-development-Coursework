
import java.util.ArrayList;

public class Player implements Runnable {
    public int playerIndex;
    private Deck leftDeck;
    private Deck rightDeck;
   

    ArrayList<Card> PlayerCard = new ArrayList<>();

    public Player(int playerIndex){
        this.playerIndex = playerIndex;
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
            
            
            // Try to acquire both left and right deck
            if (leftDeck.tryLock()) {
                if (rightDeck.tryLock()) {
                    // we can lock both deck, check to see if leftdeck is empty or not
                    if (leftDeck.isEmpty()) {
                        rightDeck.unlock();
                        leftDeck.unlock();
                    } else {
                        if (CardGame.whoWon != null){
                            break;
                        }
                        leftDeck.withDrawnCard(this);
                        if (isWon()) {
                            System.out.println("Player Index : " + playerIndex + "Win");
                            CardGame.whoWon = this;
                            CardGame.interruptAllThread();
                            CardGame.checkSum();
                        
                        } else {
                            if (CardGame.whoWon != null){
                                break;
                            }
                            Card inputCard = getDifferCard();
                            rightDeck.discarded(inputCard);
                            System.out.println("Player " + playerIndex + " discard a " + inputCard.getValue() + " right deck become " + rightDeck.getCardList());
                        }

                        leftDeck.unlock();
                        rightDeck.unlock();
                    }
                } else {
                    leftDeck.unlock();
                }
            } 
        }
       
    }

    public ArrayList<Card> getPlayerCard(){
        return this.PlayerCard;
    }
    public Card getDifferCard(){
        for (Card eachCard : PlayerCard){
            if (eachCard.getValue() != playerIndex){
                PlayerCard.remove(eachCard);
                return eachCard;
            }
        }
        return null;
    }

    public boolean isWon(){
        int firstValue = PlayerCard.getFirst().getValue();
        for (Card eachCard : PlayerCard){
            if (eachCard.getValue() != firstValue){
                return false;
            }
        }
        return true;
    }

    public Deck getLeftDeck(){
        return leftDeck;
    }

    public Deck getRightDeck(){
        return rightDeck;
    }
}
