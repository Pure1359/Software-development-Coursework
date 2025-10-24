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
        
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
       
        System.out.println("Please enter the number of players");
        int n = myObj.nextInt();  // Read user input
        myObj.nextLine();

        System.out.println("Please enter location of pack to load");
        String filename = myObj.nextLine();  // Read user input
        myObj.close(); // close the scanenr object

        playerArr = new Player[n];
        threadList = new Thread[n];
        deckArr = new Deck[n];
          
        for (int i = 0; i < n; i++) {
            playerArr[i] = new Player(i);
            threadList[i] = new Thread(playerArr[i]);
        }

        for (int i = 0; i < n; i++){
          deckArr[i] = new Deck(i);
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

            for (Thread eachThread : threadList){
                eachThread.start();
            }

            for (Thread eachThread : threadList){
                try {
                    eachThread.join();
                } catch (InterruptedException e){
                }
            }

            readFile.close();

        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        } catch (IOException k){

        }
    
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
}
