To run test Suite follow this procedure :

1. Because you are reading this text, i assume that you have already extract the cardsTest.zip
2. Make sure you are directly under the project root (as usual)
3. Run command in terminal : 
    - (Window) java -cp bin;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore TestSuite
    - (Linux)  java -cp bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore TestSuite

For example if you are on Window : (CMD)
C:\Users\XXX\OneDrive - University of Exeter\Documents\XXX\cardsTest(PROJECT ROOT)>java -cp bin;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore TestSuite

To run jar file follow this procdure : 

1.Make sure you are directly under the project root (as usual)
2.Run command in terminal : java -jar cards.jar
3.If ask for deck input location, please make sure that the textfile is directly under the project root (as it usual)


Please make sure that the deck input file, does not have any "new line charactor \n" at the end of the file