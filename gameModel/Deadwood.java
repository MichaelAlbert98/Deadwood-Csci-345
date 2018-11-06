/* CSCI 345 - Object Oriented Design - Deadwood
 * Ryan Lingg and Michael Albert
 * Deadwood.java
 * 11/01/11
 */


public class Deadwood {

    //Deadwood Attributes
    public static int numPlayers;
    private static final int minPlayers = 2;
    private static final int maxPlayers = 8;
    private static int numDays;
    private static Game game;


    /* Main Method
    *
    * Controller for the Deadwood game.
    */
    public static void main(String[] args) {
        System.out.println("\nCreating Deadwood!");

        try {
            //1) Set the number of players and days for the game
            if (args.length == 0){
                numPlayers = 2;
            } else {
                getNumPlayers(args[0]);
            }
            if (numPlayers != 0) {
                setNumDays();
            } else {
                return;
            }

            //2) Create the game object and the text view model
            game = new Game(numPlayers,numDays);
            TextView tv = new TextView(game);
            System.out.println("Starting Deadwood!\n");

            //Game loop:
            game.newDay();
            while(!game.isGameOver()) {
                game.nextTurn();
                if(game.isDayOver()) {
                    game.newDay();
                }
            }
            System.out.println("\nGame Over!\n");


        } catch (NumberFormatException e){
            System.out.println("Number of players desired must be a integer between 2 and 8.");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error encountered. Exiting Deadwood.");
        }
    }


    /* getNumPlayers
     *
     * Casts and validates for the number of players from command line arg.
     */
    private static void getNumPlayers(String countStr) throws NumberFormatException {
        int playerCount;
        playerCount = Integer.parseInt(countStr);
        if ((playerCount < minPlayers) || (playerCount > maxPlayers)) {
            System.out.println("There must be between 2 and 8 players.\n");
            numPlayers = 0;
        } else {
            numPlayers = playerCount;
        }
    }

    /* setNumPlayers
    *
    * Setter for the number of players in a Deadwood game.
    */
    private static void setNumDays() throws NumberFormatException {
        if (numPlayers < 4) {
            numDays = 3;
        } else {
            numDays = 4;
        }
    }
}
