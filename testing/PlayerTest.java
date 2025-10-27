

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import src.Card;
import src.CardGame;
import src.Deck;
import src.Player;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.ArrayDeque;



import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.JUnitCore;
public class PlayerTest {
    
    private CardGame mockCardGame = new CardGame();
    private Player p0;
    private Player p1;
    private Player p2;
    private Player p3;
    private Player p4;

    private Deck d0;
    private Deck d1;
    private Deck d2;
    private Deck d3;
    private Deck d4;


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
    public void setup(){
        mockCardGame.startTest(5);

        p0 = mockCardGame.playerArr[0];
        p1 = mockCardGame.playerArr[1];
        p2 = mockCardGame.playerArr[2];
        p3 = mockCardGame.playerArr[3];
        p4 = mockCardGame.playerArr[4];

        d0 = mockCardGame.deckArr[0];
        d1 = mockCardGame.deckArr[1];
        d2 = mockCardGame.deckArr[2];
        d3 = mockCardGame.deckArr[3];
        d4 = mockCardGame.deckArr[4];
    }

    @Test
    public void validDateDeckLocation(){
        assertFalse(mockCardGame.validateFile("testing/deckThatdoesNotExists.txt", 5));
        System.out.println("Test pass for validate location of deck file");
    }
    @Test
    public void validDateDeckContent(){
        assertFalse(mockCardGame.validateFile("testing/invalidDeck.txt", 5));
        assertFalse(mockCardGame.validateFile("testing/invalidDeck2.txt", 5));
        assertFalse(mockCardGame.validateFile("testing/invalidDeck3.txt", 5));
        assertFalse(mockCardGame.validateFile("testing/invalidDeck4.txt", 5));
       
        
        System.out.println("Test pass for validate content of deck file");
    }

    @Test
    public void testDeckAssigning(){
        //Checking if we follow the convention that player Index i left deck is i - 1 and right deck is i, except for i = 0 and i = nums of player - 1
        

        assertEquals(p0.getLeftDeck(), d4);
        assertEquals(p1.getLeftDeck(), d0);
        assertEquals(p2.getLeftDeck(), d1);
        assertEquals(p3.getLeftDeck(), d2);
        assertEquals(p4.getLeftDeck(), d3);

        assertEquals(p0.getRightDeck(), d0);
        assertEquals(p1.getRightDeck(), d1);
        assertEquals(p2.getRightDeck(), d2);
        assertEquals(p3.getRightDeck(), d3);
        assertEquals(p4.getRightDeck(), d4);

        System.out.println("Test pass for left and right deck assigning");
    }

    @Test
    public void testDeckLength(){   
        assertTrue(d0.getCardList().size() == 4);
        assertTrue(d1.getCardList().size() == 4);
        assertTrue(d2.getCardList().size() == 4);
        assertTrue(d3.getCardList().size() == 4);
        assertTrue(d4.getCardList().size() == 4);

        System.out.println("Test pass for initial deck length");
    }

    @Test
    public void testDeckContent(){
        assertTrue(d0.getCardList().toString().equals("[0, 3, 1, 2]"));
        assertTrue(d1.getCardList().toString().equals("[0, 5, 6, 4]"));
        assertTrue(d2.getCardList().toString().equals("[4, 6, 0, 0]"));
        assertTrue(d3.getCardList().toString().equals("[3, 0, 0, 0]"));
        assertTrue(d4.getCardList().toString().equals("[5, 0, 1, 1]"));

        System.out.println("Test pass for initial deck content");

    }

    @Test
    public void testPlayerHandLength(){
        assertTrue(p0.getPlayerCard().size() == 4);
        assertTrue(p1.getPlayerCard().size() == 4);
        assertTrue(p2.getPlayerCard().size() == 4);
        assertTrue(p3.getPlayerCard().size() == 4);
        assertTrue(p4.getPlayerCard().size() == 4);

        System.out.println("Test pass for playerHandLength");
    }

    @Test
    public void playerHandContent(){
        assertTrue(p0.getPlayerCard().toString().equals("[1, 0, 0, 6]"));
        assertTrue(p1.getPlayerCard().toString().equals("[3, 6, 3, 3]"));
        assertTrue(p2.getPlayerCard().toString().equals("[4, 4, 6, 1]"));
        assertTrue(p3.getPlayerCard().toString().equals("[2, 2, 6, 4]"));
        assertTrue(p4.getPlayerCard().toString().equals("[1, 1, 5, 4]"));

        System.out.println("Test pass for initial content in player hand");
    }



    @Test
    public void testWithDrawnCard(){
        Player p0 = mockCardGame.playerArr[0];
        Deck p0LeftDeck = p0.getLeftDeck();
        ArrayDeque<Card> deckQueue = p0LeftDeck.getCardList();
        Card topCard = deckQueue.getFirst();
        p0.withDrawnCard();
        assertFalse(deckQueue.contains(topCard));
        System.out.println("Test pass for removing top Card");
    }

    @Test
    public void testDiscardingCard(){
        Player p0 = mockCardGame.playerArr[0];
        Deck p0rightDeck = p0.getRightDeck();

        ArrayDeque<Card> deckQueue = p0rightDeck.getCardList();
        Card cardTobeRemoved = p0.getPlayerCard().get(0);
        
        p0.discardingCard();


        assertEquals(cardTobeRemoved, deckQueue.getLast());

        System.out.println("Test for Discarding card to bottom of right deck pass");
    }




    
}
