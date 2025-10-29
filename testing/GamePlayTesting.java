import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import src.CardGame;
import src.ConcurrentAccessException;
import src.Deck;
import src.Player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class GamePlayTesting {
    private  CardGame mockCardGame;
    private  Player p1, p2, p3, p4, p5;
    private  Deck d1, d2, d3, d4, d5;

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
    public  void setup(){
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
    //Test to see if 2 or more thread access the same deck at the same time (exception for game with only 1 player)
    public void detectConcurrentAccessTesting(){
        try{
            mockCardGame.InitialDataAndStartThread();
            
        } catch (ConcurrentAccessException c){
            fail("Test Fail : Concurrent access Detected");
        }

        System.out.println("Test pass for No concurrent access ");

    }

    @Test
    //Check for round robin, from the output file (we already check the actual card content in the PlayerTest)
    public void playerOutputInitialHandChecking(){
        //restart the game
        mockCardGame.InitialDataAndStartThread();

        // Upon inspecting the deckfile we know that initial hand for each player:
        String[] expectedHands = {
            "player 1 initial hand 1 0 0 6",
            "player 2 initial hand 3 6 3 3",
            "player 3 initial hand 4 4 6 1",
            "player 4 initial hand 2 2 6 4",
            "player 5 initial hand 1 1 5 4"
        };

        for (Player eachPlayer : mockCardGame.playerArr) {

            String fileName = "testing/player" + eachPlayer.playerIndex + "_output.txt";

            try (BufferedReader readFile = new BufferedReader(new FileReader(fileName))) {
                String actual = readFile.readLine();
                String expected = expectedHands[eachPlayer.playerIndex - 1]; 
                assertEquals(expected, actual);
            
            } catch (IOException e) {

            }
        }
        System.out.println("Test pass for : initial card output file writing");
    }

    @Test
    //Check for writing the file correctly or not to deck content after game end
    public void deckOutputAfterGameEndChecking(){
        
        mockCardGame.InitialDataAndStartThread();

        for (Deck eachDeck : mockCardGame.deckArr){
            String fileName = "testing/deck" + eachDeck.getDeckIndex() + "_output.txt";

            try (BufferedReader readFile = new BufferedReader(new FileReader(fileName))) {
                String actual = readFile.readLine();
                //Because the cardlist is in ArrayList format, [n1, n2, n3 ...] we have to change to the file format : deckx contents: n1, n2 ,n3 ,...
                String expected = "deck" + eachDeck.getDeckIndex() + " contents: " +eachDeck.getCardList().toString().replace("[", "").replace("]", "").replace(",", "");
                assertEquals(expected, actual);

            } catch (IOException e) {

            }
        }

        System.out.println("Test pass for : deck content output file content after game end");
    }

    @Test 
    //Check if the output file for each player exists or not
    public void checkPlayerFileExists(){
        File p1 = new File("testing/player1_output.txt");
        File p2 = new File("testing/player2_output.txt");
        File p3 = new File("testing/player3_output.txt");
        File p4 = new File("testing/player4_output.txt");
        File p5 = new File("testing/player5_output.txt");

        if (!p5.exists() || !p1.exists() || !p2.exists() || !p3.exists() || !p4.exists()){
            fail("Some file for player is not created");
        }

        System.out.println("Test pass : All file for each Player exists");
    }

    @Test
    public void checkDeckFileExists(){
        //Check if the output file for each deck exists or not
        File d5 = new File("testing/deck5_output.txt");
        File d1 = new File("testing/deck1_output.txt");
        File d2 = new File("testing/deck2_output.txt");
        File d3 = new File("testing/deck3_output.txt");
        File d4 = new File("testing/deck4_output.txt");

        if (!d5.exists() || !d1.exists() || !d2.exists() || !d3.exists() || !d4.exists()) {
            fail("Some file for deck is not created");
        }

        System.out.println("Test pass for checkDeckFileExists");


    }

    @Test
    //To make sure that thread safe can do this checking the Total Card before game start == Total Card left after game start 
    //(Read more in report)
    public void noCardLost(){
        ArrayList<Integer> originalCards = new ArrayList<>();

        try {
            BufferedReader readData = new BufferedReader(new FileReader("testing/deckTest.txt"));
            String content = readData.readLine();
            while (content != null){
                 originalCards.add((int) Integer.parseInt(content));
                content = readData.readLine();
            }
            //We can check for content if Sort(Original Array) == Sort (after)
            Collections.sort(originalCards);

            ArrayList<Integer> after = mockCardGame.getAllCard();
            Collections.sort(after);

            //If both contain the exact same card value , then no card is lost during the game, or no card appear out of nowhere
            if (after.equals(originalCards)){
                System.out.println("Test pass for : no card lost, total card before and after game is the same");
            } else{
                fail("The total original card and the card after game end is not the same");
            }
        } catch (IOException e) {
        }
    }

    
}
