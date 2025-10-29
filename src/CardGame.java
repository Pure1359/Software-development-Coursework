package src;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Scanner;

public class CardGame {
    // Each player need to know the most up to date fresh version of whoWon in order to stop playing the game fast enough
    // Hence we use volatile
    public static volatile  Player whoWon = null;

    
    public static Player[] playerArr = null;
    public static Deck[] deckArr = null;
    public static Thread[] threadList = null;
    public static void main(String[] args) {

        startGame();
        
    }

    public static void startGame(){
        //numString will be convert to int, later, we need it to be in string first so that we can validate and parse the input
        String numString;
        //filename for deck
        String filename;
        //Once numString is validated, it will be converted to numberOfPlayer
        int numberOfPlayer;
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        
       
        do{
            System.out.println("Please enter the number of players");
            numString = myObj.nextLine();  // Read user input
        } while (!validateNumberOfPlayer(numString));
        //Given that the numString pass the validation we then convert it to int
        numberOfPlayer = Integer.parseInt(numString);
            

        //Validate the location and the content of the deck
        do {
            System.out.println("Please enter location of pack to load");
            filename = myObj.nextLine();  // Read user input  
        } while (!validateFile(filename, numberOfPlayer));


        myObj.close(); // close the scanner object
        

        //From specification we have n deck where n is number of player, i also have n thread , thread/Player
        playerArr = new Player[numberOfPlayer];
        threadList = new Thread[numberOfPlayer];
        deckArr = new Deck[numberOfPlayer];

        
        //Each player implement runnable, that is why we can do threadlist = new Thread(playerArr[i])
        for (int i = 0; i < numberOfPlayer; i++) {
            //Specifying the index of player, and the output file name (starting from 1 to n)
            playerArr[i] = new Player(i + 1, "player" + (i + 1) + "_output.txt");
            threadList[i] = new Thread(playerArr[i]);
        }
        
        //Specifying the file name for deck output
        for (int i = 0; i < numberOfPlayer; i++){
            //deck output file name starting from 1 to n.
            deckArr[i] = new Deck(i + 1, "deck" + (i + 1) + "_output.txt");
        }

        //This is part of design to avoid deadlock (see more in report) , For all player Index = i, their leftdeck is i -1 and rightdeck is i
        //If the player is the first person , then deck need to be loop around so leftdeck is at deckArr[numberOfplayer - 1]
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

        //began round robin distribution of card to player
        try {
            BufferedReader readFile = new BufferedReader(new FileReader(filename));
            
           
            String read = readFile.readLine();
            //In reality the player index range is from 1 to n, however we start at 0 so that modding operation remain easy
            //The output file for the player will be using the correct value of player (1 - n)
            //This labelling is also applied to deck
            int playerIndex = 0;
            int deckIndex = 0;
            int cardTaken = 0;


            while (read != null){
                if (cardTaken < 4 * numberOfPlayer){
                    // Use modding operation for loop around after reaching the last player (to ensure round robin)
                    playerIndex = playerIndex % numberOfPlayer;
                    playerArr[playerIndex].getPlayerCard().add(new Card(Integer.parseInt(read)));
                    playerIndex++;
                    cardTaken++;
                } else{
                    //if all player hand are full we can then start distributing the card to deck in round robin
                    // Use modding operation for loop around after reaching the last deck (to ensure round robin)
                    deckIndex = deckIndex % numberOfPlayer;
                    deckArr[deckIndex].receiveCard(new Card(Integer.parseInt(read)));
                    deckIndex++;
                }
            
                read = readFile.readLine();
            }
            //Display the initial information about the player.
            logging(playerArr);
            readFile.close();

            //Check if anyone won the game immediately and write the initial hand on the first line of the file
            InitialDataAndStartThread();
            
            
            //after game end we write to file the remaining deck content
            for (Deck eachDeck : deckArr){
                eachDeck.writeDeckContent();
            }

        } catch (IOException e){
            System.out.println(e.getMessage());
        }

        
    }

    //For testing configuration (5 player)
    public static void startTest(int numsPlayer){
        playerArr = new Player[numsPlayer];
        threadList = new Thread[numsPlayer];
        deckArr = new Deck[numsPlayer];
        //Set the destination player and deck output file to testing folder
        for (int i = 0; i < numsPlayer; i++) {
            playerArr[i] = new Player(i + 1, "testing/player" + (i + 1) + "_output.txt");
            threadList[i] = new Thread(playerArr[i]);
        }
        // Set the destination for deck output file to testing folder
        for (int i = 0; i < numsPlayer; i++){
            deckArr[i] = new Deck(i + 1, "testing/deck" + (i + 1) + "_output.txt");
        }
        //assign left and right deck as usual
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
            
            // begin round robin distribution to the player, same logic as in startGame
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
            readFile.close();
            
            for (Deck eachDeck : deckArr){
                eachDeck.writeDeckContent();
            }

        } catch(IOException e){

        }
    }

    public static void InitialDataAndStartThread(){
        boolean winAtStart = false;
            
        for (Player eachPlayer : playerArr){
            eachPlayer.writeDatatoFile("player " + eachPlayer.playerIndex + " initial hand" + eachPlayer.getPlayerHand() + "\n");
        }

        //before start thread let check to see if there are anyone who win the game immediately
        for (Player eachPlayer : playerArr){
            if (eachPlayer.isWon()){
                winAtStart = true;
                break;
            }
        }
        if (!winAtStart){
            //Began the game
            startThread();
        } else{
            //Write the ending message to all the player, no thread is start, the game is already end
            for (Player eachPlayer : playerArr){
                eachPlayer.writeLastLine();
            }
        }
    }
    
    private static void startThread(){
        //To help as with the testing that program is thread safe we will detect all the uncaught exception in this array (the uncaught exception here are Custom exception class : ConcurrentAccessException)
        ArrayList<Throwable> exceptionList = new ArrayList<>(); 


        for (Thread eachThread : threadList){
                eachThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    //When the thread throw an uncaugtexception we interrupt all the thread (this will never happen because the player and deck class is thread-safe)
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
                System.out.println("Unexpected behavior occur!");
            } 
        }
        //If there are any exception detected we will throw the exception to the user terminal
        for (Throwable eachThrowable : exceptionList){
            if (eachThrowable instanceof ConcurrentAccessException){
                //IF there are only 1 player, then we have only 1 deck, the player left and right deck both refer to the same deck, hence this is the only exception of ConcurrentAccessException
                if (playerArr.length == 1){
                    break;
                }
                //If there are more than 1 player throw, this error to indicate that the program is not thread safe (this wil never happen as the program is thread safe)
                throw new ConcurrentAccessException("Concurrent Access Detected!");
            }
        }     
    }
    //Validate the deckfile content 
    public static boolean validateFile(String path, int playerAmount){
       try{
            BufferedReader readFile = new BufferedReader(new FileReader(path));
            String read = readFile.readLine();
            int cardAmount = 0;
            while (read != null){
                //We do not accept "" empty string between the number, start or at end of file
                if (read.equals("")){
                    System.out.println("Empty line detected. Please remove any extra newlines or whitespace between numbers, or at the end of the file.");
                }
                int value = Integer.parseInt(read);
                // From specification we do not accept card that have negative value 
                if (value >= 0){
                    cardAmount++;
                    read = readFile.readLine();
                } else{
                    throw new NumberFormatException("Negative value detected");
                }
            }
            // From specification we invalidate deck that contain not 8n card
            if (cardAmount != playerAmount * 8){
                System.out.println("Invalid amount of card is not valid for " + playerAmount + " amount of players");
                return false;
            }
        //IOException usually for file not existing
       } catch (FileNotFoundException e){
            System.out.println("No deck found on such path given :" + e.getMessage());
            return false;
        //Any other issues causing parseInt will be thrown here
       } catch (NumberFormatException n){
            System.out.println("Invalid content in the deck specified : " + n.getMessage());
            return false;
       } catch (IOException I){
            System.out.println("Unexpected IO Operation error occur : " + I.getMessage());
       }

       return true;

    }
    //We validate the string of player number
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
    //Log to terminal  the start of the round : Initial hand , Initial left and right deck
    public static void logging(Player[] playerArr){
        for (Player eachPlayer : playerArr){
                Deck leftDeck = eachPlayer.getLeftDeck();
                Deck rightDeck = eachPlayer.getRightDeck();
                ArrayDeque<Card> LeftcardQueue = leftDeck.getCardList();
                ArrayDeque<Card> RightcardQueue = rightDeck.getCardList();
                System.out.println("Player index : " + eachPlayer.playerIndex + " initial hand is " + eachPlayer.getPlayerCard() + " left deck = " + LeftcardQueue + " right Deck = " + RightcardQueue);
            }
    }
    //Helper function for testing , get all the card in each player hand + all card remain in the deck, to help compare before and after the game if there are any card lost
    public static ArrayList<Integer> getAllCard(){
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

    //Interrupt all thread
    public static void interruptAllThread(){
        for (Thread eachThread : threadList){
            eachThread.interrupt();
        }
    }
}
