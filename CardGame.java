import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Scanner;

class CardGame {
    public static volatile  Player whoWon = null;
    public static Player[] playerArr = null;
    public static Deck[] deckArr = null;
    public static Thread[] threadList = null;
    public static void main(String[] args) {

        if (args.length > 0 && args[0].equals("test")){
            System.out.println("testing");
            startTest(5);
        } else{
            startGame(false);
        }
        
    }

    public static void startGame(boolean testing){

        String filename;
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
    
        System.out.println("Please enter the number of players");
        int n = myObj.nextInt();  // Read user input
        myObj.nextLine();

        

        do {
            System.out.println("Please enter location of pack to load");
            filename = myObj.nextLine();  // Read user input
        } while (!validateFile(filename));


        myObj.close(); // close the scanenr object

        playerArr = new Player[n];
        threadList = new Thread[n];
        deckArr = new Deck[n];
        
        for (int i = 0; i < n; i++) {
            playerArr[i] = new Player(i, "player" + i + "_output.txt");
            threadList[i] = new Thread(playerArr[i]);
        }

        for (int i = 0; i < n; i++){
        deckArr[i] = new Deck(i, "deck" + i + "_output.txt");
        }

        for (int i = 0; i < n; i++) {
            Player player = playerArr[i];
            if (i == 0) {
                player.setLeftDeck(deckArr[n - 1]);
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
            if (cardTaken < 4 * n){
                playerIndex = playerIndex % n;
                playerArr[playerIndex].getPlayerCard().add(new Card(Integer.parseInt(read)));
                playerIndex++;
                cardTaken++;
            } else{
                deckIndex = deckIndex % n;
                deckArr[deckIndex].receiveCard(new Card(Integer.parseInt(read)));
                deckIndex++;
            }
            
            read = readFile.readLine();
            }

            logging(playerArr);
            readFile.close();

            startThread();

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
    public static boolean validateFile(String path){
       try{
            BufferedReader readFile = new BufferedReader(new FileReader(path));
            String read = readFile.readLine();
            while (read != null){
                int value = Integer.parseInt(read);
                if (value >= 0){
                    read = readFile.readLine();
                } else{
                    throw new NumberFormatException("Negative value detected");
                }
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
    public static void logging(Player[] playerArr){
        for (Player eachPlayer : playerArr){
                Deck leftDeck = eachPlayer.getLeftDeck();
                Deck rightDeck = eachPlayer.getRightDeck();
                ArrayDeque<Card> LeftcardQueue = leftDeck.getCardList();
                ArrayDeque<Card> RightcardQueue = rightDeck.getCardList();
                System.out.println(eachPlayer.getPlayerCard().toString() + "left deck = " + LeftcardQueue + " right Deck = " + RightcardQueue);
            }
    }

    public static void checkSum(){
        ArrayList<Card> useless = new ArrayList<Card>();
        for (Player eachPlayer : playerArr){
            for (Card eachCard : eachPlayer.getPlayerCard()){
                useless.add(eachCard);
            }
        }

        for (Deck eachDeck : deckArr){
            for (Card eachCard : eachDeck.getCardList()){
                useless.add(eachCard);
            }
        }
        System.out.println(useless.size());
        System.out.println(useless);

    }
    private static void interruptAllThread(){
        for (Thread eachThread : threadList){
            eachThread.interrupt();
        }
    }
}
