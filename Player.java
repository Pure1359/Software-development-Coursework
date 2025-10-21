
import java.util.ArrayList;

public class Player implements Runnable {
    private int playerIndex;
    private Deck leftDeck;
    private Deck rightDeck;
   

    ArrayList<Card> PlayerCard = new ArrayList<>();

    @Override
    public void run() {
        //This is atomic action Get card from left, discard to right
        while (!Thread.currentThread().isInterrupted()){
            Deck FirstDeckToLock;
            Deck SecondDeckToLock;
            if (leftDeck.getDeckPriority() > rightDeck.getDeckPriority()){
                FirstDeckToLock = leftDeck;
                SecondDeckToLock = rightDeck;
            } else{
                FirstDeckToLock = rightDeck;
                SecondDeckToLock = leftDeck;
            }

            FirstDeckToLock.lockThisDeck();
            SecondDeckToLock.lockThisDeck();

            // withdrawn from left deck and then discard to right deck if the player still not win
            leftDeck.withDrawnCard(this);
            // check if the player have won

            if (this.isWon()){
                CardGame.whoWon = this;
                break;
            }
            rightDeck.receiveCard(getDifferCard());

            SecondDeckToLock.unlockThisDeck();
            FirstDeckToLock.unlockThisDeck();
        }
    }

    public ArrayList<Card> getPlayerCard(){
        return this.PlayerCard;
    }
    public Card getDifferCard(){
        for (Card eachCard : PlayerCard){
            if (eachCard.getValue() != playerIndex){
                return eachCard;
            }
        }
        return null;
    }

    public boolean isWon(){
        for (Card eachCard : PlayerCard){
            if (eachCard.getValue() != playerIndex){
                return false;
            }
        }
        return true;
    }
}
