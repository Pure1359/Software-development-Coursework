import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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

        System.out.println("Test for No concurrent access ");
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

        if (!d0.exists() || !d1.exists() || !d2.exists() || !d3.exists() || !d4.exists()) {
            fail("Some file for deck is not created");
        }

        System.out.println("Test pass for checkDeckFileExists");


    }

    @Test
    public void noCardLost(){
        ArrayList<Integer> originalCards = new ArrayList<>();

        try {
            BufferedReader readData = new BufferedReader(new FileReader("testing/deckTest.txt"));
            String content = readData.readLine();
            while (content != null){
                 originalCards.add((int) Integer.parseInt(content));
                content = readData.readLine();
            }

            Collections.sort(originalCards);

            ArrayList<Integer> checkSum = mockCardGame.checkSum();
            Collections.sort(checkSum);
            if (checkSum.equals(originalCards)){
                System.out.println("Test pass for : no card lost, total card before and after game is the same");
            } else{
                fail("The total original card and the card after game end is not the same");
            }
        } catch (IOException e) {
        }
    }
}
