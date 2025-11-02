To run test Suite follow this procedure :

1. Because you are reading this text, i assume that you have already extract the cardsTest.zip
2. Make sure your current working directory are under the project root (as usual)
3. Run command in terminal : 
    - (Window) java -cp ".;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar" org.junit.runner.JUnitCore TestSuite

    - (Linux)  java -cp ".:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" org.junit.runner.JUnitCore TestSuite

For example if you are on Window : (CMD)
C:\Users\XXX\OneDrive - University of Exeter\Documents\XXX\cardsTest >java -cp bin;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore TestSuite

In theory you can also run the test from the jar file (not necessary), however you need to put the cards.jar inside cardsTest folder
because cardsTest folder contain the lib folder which junit need to use, if you done so you can run test from cards.jar by

- (Window) java -cp cards.jar;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar org.junit.runner.JUnitCore TestSuite
- (Linux)  java -cp cards.jar:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore TestSuite


To run jar file follow this procdure : 

1.Make sure you current working directory are where the .jar file is installed on.
2.Run command in terminal : java -jar cards.jar
3.If ask for deck input location, please make sure that the textfile is directly under the project root (as it usual)


Please make sure that the deck input file, does not have any "new line charactor \n" at the end of the file