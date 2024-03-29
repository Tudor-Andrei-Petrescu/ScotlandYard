## ScotlandYard Coursework

## Motivation
The aim of the coursework is to become familiar with the four fundamental principles of Object Oriented Programming ( Inheritance, Abstraction, Encapsulation, Polymorphism) and to become comfortable with writing code in an OOP manner and using design patterns.

## Description

[The game follows the classic ScotlandYard board game](https://www.ravensburger.org/spielanleitungen/ecm/Spielanleitungen/26646%20anl%202050897_2.pdf?ossl=pds_text_Spielanleitung) : "Mister X is on the run in London travelling by taxi, bus and underground. Only particularly clever detectives will be able to catch Mister X! Mister X tries to stay one step ahead of the detectives and keep them guessing until the last move of the game. The detectives try to pick up the trail and track down Mister X".

The core logic for the game can be found in [`MyGameState.java`](cw-model/src/main/java/uk/ac/bris/cs/scotlandyard/model/MyGameState.java) as well as [`MyModelFactory.java`](cw-model/src/main/java/uk/ac/bris/cs/scotlandyard/model/MyModelFactory.java).

__The following alterations to the boardgame version have been made__ : 
 * Police or Bobbies will not be modelled.
 * The Ferry will be modelled.
 * Mr X and the detectives will be given variable (user-specified) amounts of tickets at the start, the normal rules for tickets follow:
 * When a detective moves, the ticket used will be given to Mr X.
 * Used Mr X tickets are discarded.
 * The number of rounds in a game is variable (>0) specified by an initial setup rather than fixed to 22 rounds as in the board game.
 * In the manual, the round count is defined as the number of transitions between Mr X and the detectives as a whole, this number is different from the number of slots on Mr X's Travel Log because Mr X can use double moves which occupies two slots (e.g. a 22 round game with two double move tickets means Mr X can have up to 24 moves).
For practical reasons, we've simplified this rule so the game can be set up with a variable max number of moves for Mr X (i.e. the slot count in Mr X's travel log), such that the game is over when Mr X's travel log is completely full, instead of some abritary number of rounds.
 * Mr X cannot move into a detective location.
 * Mr X loses if it is his turn and he cannot make any move himself anymore.
 * Detectives lose if it is their turn and none of them can move, if some can move the others are just skipped.
 * `Ticket.SECRET` represents a black ticket, this is used for Mr X's secret moves

## Deployment Instructions

The following steps apply for [ IntelliJ IDEA Community edition ](https://www.jetbrains.com/idea/download/#section=windows) . Open the project from the cw-model root. When selecting the SDK configure it, as such: 

 * Version: 17

 * Vendor: Oracle OpenJDK

 * Location: (leave default, don't modify)

To run the game, right click on the `Main` class (`/src/main/java/uk/ac/bris/cs/scotlandyard/Main.java`) and select `Run Main.main()` . 
