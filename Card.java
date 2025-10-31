
public class Card {
    private final int value; // The face value of the card

    // Constructor for Card
    public Card(int value) {
        this.value = value;
    }

    // Getter method for Card
    public int getValue() {
        return value;
    }
    
    public String toString(){
        return String.valueOf(value);
    }

    // No setter method required because card value is fixed
}