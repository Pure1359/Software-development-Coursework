
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;


import org.junit.*;
public class PlayerTest {
    

    private CardGame mockCardGame = new CardGame();
    private Player p1;
    private Player p2;
    private Player p3;
    private Player p4;
    private Player p5;

    private Deck d1;
    private Deck d2;
    private Deck d3;
    private Deck d4;
    private Deck d5;

    
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(PlayerTest.class);

        for (Failure failure : result.getFailures()) {
            System.out.println(failure.getTestHeader() + " failed: " + failure.getMessage());
        }

        if(result.wasSuccessful()) {
            System.out.println("All tests passed!");
        } else{
            System.out.println("Some Test fail!");
        }
    }



    @Before
    //Before running each test make sure to set up the player correctly first
    public void setup() {
        mockCardGame = new CardGame();
        mockCardGame.startTest(5);
        
        p1 = mockCardGame.playerArr[0];
        p2 = mockCardGame.playerArr[1];
        p3 = mockCardGame.playerArr[2];
        p4 = mockCardGame.playerArr[3];
        p5 = mockCardGame.playerArr[4];

        d1 = mockCardGame.deckArr[0];
        d2 = mockCardGame.deckArr[1];
        d3 = mockCardGame.deckArr[2];
        d4 = mockCardGame.deckArr[3];
        d5 = mockCardGame.deckArr[4];
    }

    @Test
    public void validDateDeckLocation(){
        //Check for path that does not exists
        assertFalse(mockCardGame.validateFile("testing/deckThatdoesNotExists.txt", 5));
        System.out.println("Test pass for validate location of deck file");
    }
    @Test
    public void validDateDeckContent(){
        //Each deck have invalid content such as not enough card, invalid card value, and so on
        System.out.println("Began Testing for Invalid Content of Deck : ");
        assertFalse(mockCardGame.validateFile("testing/invalidDeck.txt", 5));
        assertFalse(mockCardGame.validateFile("testing/invalidDeck2.txt", 5));
        assertFalse(mockCardGame.validateFile("testing/invalidDeck3.txt", 5));
        assertFalse(mockCardGame.validateFile("testing/invalidDeck4.txt", 5));
        assertFalse(mockCardGame.validateFile("testing/invalidDeck5.txt", 5));
       
        
        System.out.println("Test pass for validate content of deck file");
    }

    @Test
    public void validDatePlayerNumbers(){
        System.out.println("Began Testing for Invalid Number of Player");

        assertFalse(mockCardGame.validateNumberOfPlayer("-6"));
        assertFalse(mockCardGame.validateNumberOfPlayer("9a"));
        assertFalse(mockCardGame.validateNumberOfPlayer("_9"));
        assertFalse(mockCardGame.validateNumberOfPlayer("9 "));
        assertTrue(mockCardGame.validateNumberOfPlayer("5"));
        
        System.out.println("Test pass for validate amount of player");
    }

    @Test
    public void testDeckAssigning(){
        //Checking if we follow the convention that player Index i left deck is i - 1 and right deck is i, except for first player, as player player left deck is deck nth
        
        assertEquals(p1.getLeftDeck(), d1);
        assertEquals(p2.getLeftDeck(), d2);
        assertEquals(p3.getLeftDeck(), d3);
        assertEquals(p4.getLeftDeck(), d4);
        assertEquals(p5.getLeftDeck(), d5);

        assertEquals(p1.getRightDeck(), d2);
        assertEquals(p2.getRightDeck(), d3);
        assertEquals(p3.getRightDeck(), d4);
        assertEquals(p4.getRightDeck(), d5);
        assertEquals(p5.getRightDeck(), d1);

        System.out.println("Test pass for left and right deck assigning");
    }

    @Test
    public void testDeckLength(){   
        //At the start each deck must have 4 card
        assertTrue(d1.getCardList().size() == 4);
        assertTrue(d2.getCardList().size() == 4);
        assertTrue(d3.getCardList().size() == 4);
        assertTrue(d4.getCardList().size() == 4);
        assertTrue(d5.getCardList().size() == 4);

        System.out.println("Test pass for initial deck length");
    }

    @Test
    public void testDeckContent(){
        //Check if deck round robin is correct or not, from inspection we expect that :
        
        assertTrue(d1.getCardList().toString().equals("[6, 6, 5, 6]"));
        assertTrue(d2.getCardList().toString().equals("[3, 1, 5, 4]"));
        assertTrue(d3.getCardList().toString().equals("[1, 2, 3, 2]"));
        assertTrue(d4.getCardList().toString().equals("[6, 3, 6, 1]"));
        assertTrue(d5.getCardList().toString().equals("[3, 4, 2, 4]"));

        System.out.println("Test pass for initial deck content");

    }

    @Test
    public void testPlayerHandLength(){
        //Start of game all player must have 4 card
        assertTrue(p1.getPlayerCard().size() == 4);
        assertTrue(p2.getPlayerCard().size() == 4);
        assertTrue(p3.getPlayerCard().size() == 4);
        assertTrue(p4.getPlayerCard().size() == 4);
        assertTrue(p5.getPlayerCard().size() == 4);

        System.out.println("Test pass for playerHandLength");
    }

    @Test
    public void playerHandContent(){
        // From inspection we expect that : 
        assertTrue(p1.getPlayerCard().toString().equals("[3, 4, 3, 1]"));
        assertTrue(p2.getPlayerCard().toString().equals("[6, 6, 3, 5]"));
        assertTrue(p3.getPlayerCard().toString().equals("[6, 5, 6, 2]"));
        assertTrue(p4.getPlayerCard().toString().equals("[1, 3, 4, 6]"));
        assertTrue(p5.getPlayerCard().toString().equals("[6, 4, 3, 4]"));

        System.out.println("Test pass for initial content in player hand");
    }



    @Test
    //Checking for withdrawing card
    public void testWithDrawnCard(){
        Player p1 = mockCardGame.playerArr[0];
        Deck p1LeftDeck = p1.getLeftDeck();
        ArrayDeque<Card> deckQueue = p1LeftDeck.getCardList();
        Card topCard = deckQueue.getFirst();
        p1.withDrawnCard();
        //Check to see that the deck no longer contain the withdrawn  card
        assertFalse(deckQueue.contains(topCard));
        System.out.println("Test pass for removing top Card");

        try{
            //Check to see if the withdrawn action has been written to the output file
            BufferedReader readFile = new BufferedReader(new FileReader("testing/player1_output.txt"));
            assertEquals(readFile.readLine(), "player " + p1.playerIndex + " draws a " + topCard.getValue() + " from deck " + p1LeftDeck.getDeckIndex());
            System.out.println("Test pass for writing withdrawn information to output file pass");
        } catch (IOException e){
            fail("Either File is not created for the player or Unexpected Thing happen");
        }
    }

    @Test
    public void testDiscardingCard(){
        Player p1 = mockCardGame.playerArr[1];
        Deck p1rightDeck = p1.getRightDeck();
        ArrayDeque<Card> deckQueue = p1rightDeck.getCardList();
        
        Card differCard = new Card(-1);
        //Get the card that are not wanted for player
        for (Card eachCard : p2.getPlayerCard()){
            if (eachCard.getValue() != p2.playerIndex){
                differCard = eachCard;
                break;
            }
        }
    
        //Making sure that the discarding card() actually discard the first undesired card in the player hand to the bottom of the right deck or not
        p1.discardingCard();
        assertEquals(differCard, deckQueue.getLast());
        System.out.println("Test for Discarding card to bottom of right deck pass");

        //Test to see if we can write the information about discarding to output
        try{
            BufferedReader readFile = new BufferedReader(new FileReader("testing/player2_output.txt"));
            assertEquals("player " + p2.playerIndex + " discards a " + differCard.getValue() + " to deck " + p2.getRightDeck().getDeckIndex(), readFile.readLine());
            System.out.println("Test pass for writing discard to output file");
        } catch (IOException e){
            fail("Either File is not created for the player or Unexpected Thing");
        }
   
    } 
    @Test
    //Check if player hand is still 4 after withdrawn and discard
    public void testPlayerInvariant(){
        Player p3 = mockCardGame.playerArr[3];

        p3.withDrawnCard();
        p3.discardingCard();

        if (p3.getPlayerCard().size() != 4){
            fail("Player Is not invariants");
        }

        System.out.println("Test pass for player invariants, player hand contain 4 card after withdraw and discard");
    }
    
}
