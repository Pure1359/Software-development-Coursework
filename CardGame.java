import java.util.Scanner;  // Import the Scanner class

class CardGame {
  public static void main(String[] args) {
    Scanner myObj = new Scanner(System.in);  // Create a Scanner object
   
    System.out.println("Please enter the number of players");
    int n = myObj.nextInt();  // Read user input
    myObj.nextLine();

    System.out.println("Please enter location of pack to load");
    String filename = myObj.nextLine();  // Read user input

    myObj.close(); // close scanner object
  }
}