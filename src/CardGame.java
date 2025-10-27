package src;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Scanner;

public class CardGame {
    public static volatile  Player whoWon = null;
    public static Player[] playerArr = null;
    public static Deck[] deckArr = null;
    public static Thread[] threadList = null;
    public static void main(String[] args) {

        startGame();
        
    }

    public static void startGame(){
        String numString;
        String filename;
        int numberOfPlayer;
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        
       
        do{
            System.out.println("Please enter the number of players");
            numString = myObj.nextLine();  // Read user input
        } while (!validateNumberOfPlayer(numString));

        numberOfPlayer = Integer.parseInt(numString);
            

        do {
            System.out.println("Please enter location of pack to load");
            filename = myObj.nextLine();  // Read user input
        } while (!validateFile(filename, numberOfPlayer));


        myObj.close(); // close the scanenr object

        playerArr = new Player[numberOfPlayer];
        threadList = new Thread[numberOfPlayer];
        deckArr = new Deck[numberOfPlayer];
        
        for (int i = 0; i < numberOfPlayer; i++) {
            playerArr[i] = new Player(i, "output/player" + i + "_output.txt");
            threadList[i] = new Thread(playerArr[i]);
        }

        for (int i = 0; i < numberOfPlayer; i++){
        deckArr[i] = new Deck(i, "output/deck" + i + "_output.txt");
        }

        for (int i = 0; i < numberOfPlayer; i++) {
            Player player = playerArr[i];
            if (i == 0) {
                player.setLeftDeck(deckArr[numberOfPlayer - 1]);
                player.setRightDeck(deckArr[i]);
            } else {
                player.setLeftDeck(deckArr[i - 1]);
                player.setRightDeck(deckArr[i]);
            }
        }

        //Round robin distribution to player
        try {
            BufferedReader readFile = new BufferedReader(new FileReader("deckfile.txt"));
            
            // begin round robin distribution to the player
            String read = readFile.readLine();

            int playerIndex = 0;
            int deckIndex = 0;
            int cardTaken = 0;

            while (read != null){
            if (cardTaken < 4 * numberOfPlayer){
                playerIndex = playerIndex % numberOfPlayer;
                playerArr[playerIndex].getPlayerCard().add(new Card(Integer.parseInt(read)));
                playerIndex++;
                cardTaken++;
            } else{
                deckIndex = deckIndex % numberOfPlayer;
                deckArr[deckIndex].receiveCard(new Card(Integer.parseInt(read)));
                deckIndex++;
            }
            
            read = readFile.readLine();
            }

            logging(playerArr);
            readFile.close();

            startThread();
            System.out.println(checkSum());

            for (Deck eachDeck : deckArr){
                eachDeck.writeDeckContent();
            }

        } catch (IOException e){
            System.out.println(e.getMessage());
        }

        
    }

    
    public static void startTest(int numsPlayer){
        playerArr = new Player[numsPlayer];
        threadList = new Thread[numsPlayer];
        deckArr = new Deck[numsPlayer];
        
        for (int i = 0; i < numsPlayer; i++) {
            playerArr[i] = new Player(i, "testing/player" + i + "_output.txt");
            threadList[i] = new Thread(playerArr[i]);
        }

        for (int i = 0; i < numsPlayer; i++){
        deckArr[i] = new Deck(i, "testing/deck" + i + "_output.txt");
        }

        for (int i = 0; i < numsPlayer; i++) {
            Player player = playerArr[i];
            if (i == 0) {
                player.setLeftDeck(deckArr[numsPlayer - 1]);
                player.setRightDeck(deckArr[i]);
            } else {
                player.setLeftDeck(deckArr[i - 1]);
                player.setRightDeck(deckArr[i]);
            }
        }

        //Round robin distribution to player
        try {
            BufferedReader readFile = new BufferedReader(new FileReader("testing/deckTest.txt"));
            
            // begin round robin distribution to the player
            String read = readFile.readLine();

            int playerIndex = 0;
            int deckIndex = 0;
            int cardTaken = 0;

            while (read != null){
            if (cardTaken < 4 * numsPlayer){
                playerIndex = playerIndex % numsPlayer;
                playerArr[playerIndex].getPlayerCard().add(new Card(Integer.parseInt(read)));
                playerIndex++;
                cardTaken++;
            } else{
                deckIndex = deckIndex % numsPlayer;
                deckArr[deckIndex].receiveCard(new Card(Integer.parseInt(read)));
                deckIndex++;
            }
            
            read = readFile.readLine();
            }
        } catch(IOException e){

        }
    }

    public static void startThread(){

        ArrayList<Throwable> exceptionList = new ArrayList<>();

        for (Thread eachThread : threadList){
                eachThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread thisThread, Throwable RunTimeError) {
                        exceptionList.add(RunTimeError);
                        interruptAllThread();
                    }
                    
                });
                eachThread.start();
        }
        for (Thread eachThread : threadList){
            try {
                eachThread.join();
            } catch (InterruptedException e){
               System.out.println("Something wrong occur");
            } 
        }

        for (Throwable eachThrowable : exceptionList){
            if (eachThrowable instanceof ConcurrentAccessException){
                throw new ConcurrentAccessException("Concurrent Access Detected!");
            }
        }     
    }
    public static boolean validateFile(String path, int playerAmount){
       try{
            BufferedReader readFile = new BufferedReader(new FileReader(path));
            String read = readFile.readLine();
            int cardAmount = 0;
            while (read != null){
                int value = Integer.parseInt(read);
                if (value >= 0){
                    cardAmount++;
                    read = readFile.readLine();
                } else{
                    throw new NumberFormatException("Negative value detected");
                }
            }

            if (cardAmount != playerAmount * 8){
                System.out.println("Invalid amount of card is not valid for " + playerAmount + " amount of players");
                return false;
            }
            
       } catch (IOException e){
            System.out.println("No deck found on such path given :" + e.getMessage());
            return false;
       } catch (NumberFormatException n){
            System.out.println("Invalid content in the deck specified : " + n.getMessage());
            return false;
       }

       return true;

    }

    public static boolean validateNumberOfPlayer(String numString){
        try{
            int tempN = Integer.parseInt(numString);
            if (tempN <= 0){
                System.out.println("Invalid Number of player, must be > 0");
                return false;
            }
        } catch (NumberFormatException n){
            System.out.println("Invalid number input : " + n.getMessage());
            return false;
        }

        return true;
    }
    public static void logging(Player[] playerArr){
        for (Player eachPlayer : playerArr){
                Deck leftDeck = eachPlayer.getLeftDeck();
                Deck rightDeck = eachPlayer.getRightDeck();
                ArrayDeque<Card> LeftcardQueue = leftDeck.getCardList();
                ArrayDeque<Card> RightcardQueue = rightDeck.getCardList();
                System.out.println(eachPlayer.getPlayerCard().toString() + "left deck = " + LeftcardQueue + " right Deck = " + RightcardQueue);
            }
    }

    public static ArrayList<Integer> checkSum(){
        ArrayList<Integer> allCard = new ArrayList<>();
        for (Player eachPlayer : playerArr){
            for (Card eachCard : eachPlayer.getPlayerCard()){
                allCard.add(eachCard.getValue());
            }
        }

        for (Deck eachDeck : deckArr){
            for (Card eachCard : eachDeck.getCardList()){
                allCard.add(eachCard.getValue());
            }
        }
        
        return allCard;

    }
    private static void interruptAllThread(){
        for (Thread eachThread : threadList){
            eachThread.interrupt();
        }
    }
}
