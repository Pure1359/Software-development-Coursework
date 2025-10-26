import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import static org.junit.Assert.fail;

import java.io.File;

public class GamePlayTesting {
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
        Result result = JUnitCore.runClasses(GamePlayTesting.class);

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
    public void detectConcurrentAccessTesting(){
        
        try{
            mockCardGame.startThread();
        } catch (ConcurrentAccessException c){
            fail("Test Fail : Concurrent acess Dectected");
        }

        System.out.println("Test passs");
    }

    @Test
    public void checkSum(){
    }

    @Test 
    public void checkWriteToPlayerFile(){
        File p0 = new File("player0_output.txt");
        File p1 = new File("player1_output.txt");
        File p2 = new File("player2_output.txt");
        File p3 = new File("player3_output.txt");
        File p4 = new File("player4_output.txt");

        if (!p0.exists() || !p1.exists() || !p2.exists() || !p3.exists() || !p4.exists()){
            fail("Some file for player is not created");
        }

        System.out.println("Test pass : All file for each Player exists");
    }

    @Test
    public void checkDeckFileExists(){
        File d0 = new File("deck0_output.txt");
        File d1 = new File("deck1_output.txt");
        File d2 = new File("deck2_output.txt");
        File d3 = new File("deck3_output.txt");
        File d4 = new File("deck4_output.txt");

    }
}
