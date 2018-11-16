
/* CSCI 345 - Object Oriented Design - Deadwood
 * Ryan Lingg and Michael Albert
 * TextView.java
 * 11/13/11
 */

import gameModel.*;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;

import com.sun.corba.se.spi.orbutil.fsm.Input;

public class TextView {

    Game gameRef;
    GameView gameView;
    ArrayList<PlayerView> PlayerViews = new ArrayList<PlayerView>();
    ArrayList<SceneView> SceneViews = new ArrayList<SceneView>();

    /*
     * TextView Constructor
     *
     * This method builds a new TextView model from the game. It keeps track of all
     * of the observers that are attached to the subjects with ArrayLists.
     */
    TextView(Game gameLocation) {
        // Game:
        this.gameRef = gameLocation;
        gameView = new GameView(this.gameRef);
        // Players:
        for (Player player : this.gameRef.playerList) {
            PlayerView p = new PlayerView(player);
            PlayerViews.add(p);
        }
        // Scenes:
        ArrayList<Room> roomList = this.gameRef.board.roomList;
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getScene() != null) {
                SceneView s = new SceneView(roomList.get(i).getScene());
                SceneViews.add(s);
            }
        }
    }


    /* Text Command List
     *
     * List of the recognised text commands that can be entered by the player using TextView
     */
    public static class textCommandList {
        public static final String ACTIVEPLAYER = "active player?";
        public static final String STATS = "stats";
        public static final String WHO = "who";
        public static final String WHERE = "where";
        public static final String MOVE = "move";
        public static final String UPGRADE = "upgrade";
        public static final String TAKEROLE = "take role";
        public static final String ACT = "act";
        public static final String REHEARSE = "rehearse";
        public static final String ENDTURN = "end turn";
        public static final String BLANK = "";
    }

    /*
     * Start Listener
     *
     * This method is called when the user needs to input data. It demands input
     * until the subject has reached a valid outcome.
     */
    public void startListener() {
        Scanner scanner = new Scanner(System.in);
        String inputAction;
        ArrayList<String> activePlayerActionSet = new ArrayList<String>();

        startGameMessage();
        this.gameRef.newDay();
        this.gameRef.activePlayer.startPlayerTurn();
        activePlayerActionSet = determinePlayerActionSet();
        // Switch on player movement
        while (!this.gameRef.isGameOver()) {
            displayPlayerPrompt("Choose an action:", activePlayerActionSet);


            inputAction = scanner.nextLine().toLowerCase();
            switch (inputAction) {

            // Player Statistics
            case TextView.textCommandList.ACTIVEPLAYER:
            case TextView.textCommandList.STATS:
                activePlayerInfo();
                break;

            // Player Location
            case TextView.textCommandList.WHERE:
                activePlayerLocation();
                break;

            // Player Name
            case TextView.textCommandList.WHO:
                activePlayerName();
                break;

            // Player Move
            case TextView.textCommandList.MOVE:
                if (activePlayerActionSet.contains(TextView.textCommandList.MOVE)) {
                    if (movementPhase(scanner)) {
                        activePlayerActionSet.remove(TextView.textCommandList.MOVE);
                        activePlayerActionSet = determinePlayerActionSet();
                        activePlayerActionSet.remove("move");
                    }
                } else {
                    System.out.println("Movement not allowed.");
                }
                break;

            // Player Take Role:
                case TextView.textCommandList.TAKEROLE:
                    takeRolePhase(scanner);
                    break;

            // Player Act:
               case TextView.textCommandList.ACT:
               if (activePlayerActionSet.contains(TextView.textCommandList.ACT)) {
                  actingPhase();
                  activePlayerActionSet.remove(TextView.textCommandList.ACT);
                  activePlayerActionSet.remove(TextView.textCommandList.REHEARSE);
               }
               else {
                  System.out.println("Cannot currently act.");
               }
               break;

            // Player Rehearse:
               case TextView.textCommandList.REHEARSE:
               if (activePlayerActionSet.contains(TextView.textCommandList.REHEARSE)) {
                  this.gameRef.activePlayer.addRehearse();
                  activePlayerActionSet.remove(TextView.textCommandList.ACT);
                  activePlayerActionSet.remove(TextView.textCommandList.REHEARSE);
               }
               else {
                  System.out.println("Cannot currently rehearse.");
               }
               break;

            // Player Upgrade:
            case TextView.textCommandList.UPGRADE:
                if (activePlayerActionSet.contains(TextView.textCommandList.UPGRADE)) {
                    if (upgradePhase(scanner)) {
                        activePlayerActionSet.remove(TextView.textCommandList.UPGRADE);
                    }
                } else {
                    System.out.println("Upgrade not allowed.");
                }
                break;

            // Player End Turn
            case "end turn":
                activePlayerActionSet = endPlayerTurn();
                break;

            case "":
                break;

            default:
                System.out.println("Command not recognized.");
                break;
            }

            if (activePlayerActionSet.size() == 0) {
                activePlayerActionSet = endPlayerTurn();
            }

        }


        System.out.println("Game over!");
        scanner.close();
    }


/**** Player Information Functions ****/

    // Active Players Information:
    private void activePlayerInfo() {
        System.out.printf("The active player is %s located in %s. Their rank is %d, and have $%d and %dcr. ",
                this.gameRef.activePlayer.getName(), this.gameRef.activePlayer.getLocation().getName(),
                this.gameRef.activePlayer.getRank(), this.gameRef.activePlayer.getCash(),
                this.gameRef.activePlayer.getCredits());
        if (gameRef.activePlayer.getCurrentRole() == null) {
            System.out.printf("They are currently not in a role.\n");
        } else {
            System.out.printf("They are currently working in the scene %s.\n",
                    this.gameRef.activePlayer.getCurrentRole().getName());
        }
    }

    // Active Player Name:
    private void activePlayerName() {
        System.out.printf("The active player is %s($%d, %dcr).\n", this.gameRef.activePlayer.getName(),
                this.gameRef.activePlayer.getCash(), this.gameRef.activePlayer.getCredits());
    }

    private void activePlayerLocation() {
        if (this.gameRef.activePlayer.getCurrentRole() == null) {
            System.out.printf("The active player is located in %s wrapped.\n",
                    this.gameRef.activePlayer.getLocation().getName());
        } else {
            System.out.printf("The active player is located in %s shooting %s %s.\n",
                    this.gameRef.activePlayer.getLocation(),
                    this.gameRef.activePlayer.getLocation().getScene().getName(),
                    this.gameRef.activePlayer.getLocation().getScene().getSceneNum());
        }
    }

/**** Movement Functions ****/

    /* Movement Phase
     *
     * Displays the valid moves for the player then retireves player input about desired movement.
     * Checks if the movement is valid then updates the players location.
     */
    private Boolean movementPhase(Scanner scanner) {
        // Prompt section:
        displayPlayerPrompt("Which room do you want to move to?", this.gameRef.activePlayer.getLocation().getAdjRooms());
        // Response section:
        String input = scanner.nextLine();
        input = toNoun(input);
        Room dest = null;
        if ((dest = Board.nameToRoom.get(input)) != null) {
            if (this.gameRef.activePlayer.getLocation().getAdjRooms().contains(input)) {
                this.gameRef.activePlayer.movePlayer(dest);
                return true;
            } else {
                System.out.println("You cannot move to that room.");
            }
        } else {
            System.out.println("Room does not exist.");
        }
        return false;
    }

/**** Role Functions ****/

    /* Take Role
     *
     * 
     */
    private void takeRolePhase(Scanner scanner){
        ArrayList<String> availableRoleNames = new ArrayList<String>();
        for (int i=0;i<this.gameRef.activePlayer.getLocation().getScene().getSceneRoles().size();i++) {
            availableRoleNames.add(gameRef.activePlayer.getLocation().getScene().getSceneRoles().get(i).getName());
        }
        displayPlayerPrompt("Select a role to take:", availableRoleNames);
        String input = scanner.nextLine();
        if (availableRoleNames.contains(input)) {
           for (int i=0;i<availableRoleNames.size();i++) {
              if (this.gameRef.activePlayer.getLocation().getScene().getSceneRoles().get(i).getName().equals(input)) {
                 if (this.gameRef.activePlayer.getLocation().getScene().getSceneRoles().get(i).isRoleAvailable(this.gameRef.activePlayer.getRank())) {
                    System.out.println("Took role.");
                    this.gameRef.activePlayer.setCurrentRole(this.gameRef.activePlayer.getLocation().getScene().getSceneRoles().get(i));
                    return;
                 }
              }
           }
              System.out.println("Could not take role. Your rank is too low or it is already taken.");
              return;
        } 
        System.out.println("Please enter a valid role name.");  
        return;   
    }

    /* Acting Phase
     *
     * Randomly determines if work was success, then pays out to player based on outcome.
     */
    private void actingPhase() {
       Random rand = new Random();
       int roll = rand.nextInt(6)+1 + this.gameRef.activePlayer.timesRehearsed();
       if (roll >= this.gameRef.activePlayer.getLocation().getScene().getBudget()) {
         if (this.gameRef.activePlayer.getCurrentRole().getOnCard()) {
            System.out.println("Success! You earned two credits.");
            this.gameRef.activePlayer.addCredits(2);
            this.gameRef.activePlayer.getLocation().setShots(-1);
         }
         else {
            System.out.println("Success! You earned one dollar and one credit.");
            this.gameRef.activePlayer.addCash(1);
            this.gameRef.activePlayer.addCredits(1);
            this.gameRef.activePlayer.getLocation().setShots(-1);
         }
       }
       else {
         if (this.gameRef.activePlayer.getCurrentRole().getOnCard()) {
            System.out.println("Failure. You earned nothing.");
         }
         else {
            System.out.println("Failure. You earned one dollar.");
            this.gameRef.activePlayer.addCash(1);
         }
       }
    }


/**** Upgrade Functions ****/

    /* Upgrade Phase
     *
     * Retireves player input about desired upgrade.
     * Then calls upgrade with data to check if upgrade is valid and performs upgrade.
     */
    private Boolean upgradePhase(Scanner scanner) {
        String input;
        int desiredRank;
        String desiredPaymentType;

        // Check Location
        if (this.gameRef.activePlayer.getLocation().getName().equals("Office")) {
            // Check Rank
            if (this.gameRef.activePlayer.getRank() == 6) {
                System.out.println("You are already the max actor rank.");
            } else {
                System.out.print("Select the desired rank you wish to upgrade too: ");
                input = scanner.nextLine().toLowerCase();
                // Check Input
                try {
                    desiredRank = Integer.parseInt(input);
                    if ((desiredRank <= this.gameRef.activePlayer.getRank())) {
                        System.out.println("Invlid rank. You must choose a rank higher than your current rank.");
                    } else if (desiredRank > 6) {
                        System.out.println("Invlaid rank. You cannot choose a acting rank higher than the max of 6.");
                    } else {
                        System.out.print("Select the desired payment type ('Cash' or 'Credit'): ");
                        input = scanner.nextLine();
                        desiredPaymentType = input.toLowerCase();
                        if (input.equals("cash") || input.equals("credit")) {
                            return upgrade(desiredPaymentType, desiredRank);
                        } else {
                            System.out.println("Invalid payment type selected.");
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Desired rank must only contain a integer.");
                }
            }
        } else {
            System.out.println("You must be in the casting office to upgrade your actor rank.");
        }
        return false;
    }

    /* Upgrade
     *
     * Once a valid rank and money type have been input is entered this method checks if upgrade is
     * valid, and  upgrades the player if so, else notitifies the player of invalid choice
     */
    public boolean upgrade(String moneyType, int rank) {
        Boolean canUpgrade = validUpgrade(moneyType, rank);
        if (canUpgrade == false) {
            return false;
        }

        if (moneyType.equals("cash")) {
            if (rank == 2) {
                this.gameRef.activePlayer.setRank(2);
                this.gameRef.activePlayer.removeCash(4);
                System.out.println("Upgraded to rank 2 and lost 4 cash.");
            } else if (rank == 3) {
                this.gameRef.activePlayer.setRank(3);
                this.gameRef.activePlayer.removeCash(10);
                System.out.println("Upgraded to rank 3 and lost 10 cash.");
            } else if (rank == 4) {
                this.gameRef.activePlayer.setRank(4);
                this.gameRef.activePlayer.removeCash(18);
                System.out.println("Upgraded to rank 4 and lost 18 cash.");
            } else if (rank == 5) {
                this.gameRef.activePlayer.setRank(5);
                this.gameRef.activePlayer.removeCash(20);
                System.out.println("Upgraded to rank 5 and lost 28 cash.");
            } else {
                this.gameRef.activePlayer.setRank(6);
                this.gameRef.activePlayer.removeCash(40);
                System.out.println("Upgraded to rank 6 and lost 40 cash.");
            }
        }

        else {
            if (rank == 2) {
                this.gameRef.activePlayer.setRank(2);
                this.gameRef.activePlayer.removeCredits(5);
                System.out.println("Upgraded to rank 2 and lost 5 credits.");
            } else if (rank == 3) {
                this.gameRef.activePlayer.setRank(3);
                this.gameRef.activePlayer.removeCredits(10);
                System.out.println("Upgraded to rank 3 and lost 10 credits.");
            } else if (rank == 4) {
                this.gameRef.activePlayer.setRank(4);
                this.gameRef.activePlayer.removeCredits(15);
                System.out.println("Upgraded to rank 4 and lost 15 credits.");
            } else if (rank == 5) {
                this.gameRef.activePlayer.setRank(5);
                this.gameRef.activePlayer.removeCredits(20);
                System.out.println("Upgraded to rank 5 and lost 20 credits.");
            } else {
                this.gameRef.activePlayer.setRank(6);
                this.gameRef.activePlayer.removeCredits(25);
                System.out.println("Upgraded to rank 6 and lost 25 credits.");
            }
        }
        return true;
    }


    /* Valid Upgrade Checker
     *
     * Makes sure input is valid and that player can afford upgrade, return false if not
     */
    private Boolean validUpgrade(String moneyType, int rank) {
       if (moneyType.equals("cash")) {
            if ((rank == 2 && this.gameRef.activePlayer.getCash() < 4) || (rank == 3 && this.gameRef.activePlayer.getCash() < 10) || (rank == 4 && this.gameRef.activePlayer.getCash() < 18)
                    || (rank == 5 && this.gameRef.activePlayer.getCash() < 28) || (rank == 6 && this.gameRef.activePlayer.getCash() < 40)) {
                System.out.println("Not have enough cash to upgrade to this rank.");
                return false;
            }
        } else if (moneyType.equals("credits")) {
            if ((rank == 2 && this.gameRef.activePlayer.getCredits() < 5) || (rank == 3 && this.gameRef.activePlayer.getCredits() < 10) || (rank == 4 && this.gameRef.activePlayer.getCredits() < 15)
                    || (rank == 5 && this.gameRef.activePlayer.getCredits() < 20) || (rank == 6 && this.gameRef.activePlayer.getCredits() < 25)) {
                System.out.println("Not have enough credits to upgrade to this rank.");
                return false;
            }
        }
        return true;
    }

    /* End Player Turn
     *
     * This function tells the active player object their turn has ended,
     * updates the day if needed, and starts the next day if the game has not ended.
     */
    private ArrayList<String> endPlayerTurn() {
        this.gameRef.activePlayer.endPlayerTurn();
        this.gameRef.nextPlayer();
        if (this.gameRef.isDayOver()) {
            this.gameRef.newDay();
        }
        if (!this.gameRef.isGameOver()) {
            this.gameRef.activePlayer.startPlayerTurn();
        }
        return determinePlayerActionSet();
    }


/**** General Helpers ****/

    /* Determine Player Action Set
     *
     * Based on the players location and whether or not they are in a role,
     * this method returns an arraylist of the possible valid actions they can
     * at the start of their turn.
     */
    private ArrayList<String> determinePlayerActionSet() {
        ArrayList<String> activePlayerActionSet = new ArrayList<String>();
        if (this.gameRef.activePlayer.getCurrentRole() != null) {
            activePlayerActionSet.add("act");
            if (this.gameRef.activePlayer.timesRehearsed()+1 <
            this.gameRef.activePlayer.getLocation().getScene().getBudget()) {
               activePlayerActionSet.add("rehearse");
            }
        } else {
            activePlayerActionSet.add("move");
            if (this.gameRef.activePlayer.getLocation().getScene() != null) {
                if (this.gameRef.activePlayer.getLocation().getScene().areRolesAvailable(this.gameRef.activePlayer.getRank())){
                    activePlayerActionSet.add("take role");
                }
            }
            if (this.gameRef.activePlayer.getLocation().getName().equals("Office")) {
                activePlayerActionSet.add("upgrade");
            }
            activePlayerActionSet.add("end turn");
        }
        return activePlayerActionSet;
    }


     /* Display Player Prompt
     *
     * Given a prompt and a list of options, this method displays them in
     * text format to the command line so the user knows what to choose from.
     */
    private void displayPlayerPrompt(String prompt, ArrayList<String> options) {
        System.out.printf("%s (Options: ", prompt);
        int i = 0;
        for (Object o : options.toArray()) {
            if (i != 0) {
                System.out.print(", ");
            }
            String s = String.valueOf(o);
            System.out.printf("'%s'", s);
            i++;
        }
        System.out.println(")");
    }

    /* Word To Noun
     *
     * Capitalizes the first character in each word of a string.
     */
    private String toNoun(String input){
        for (int i = 0; i < input.length(); i++){
            if (i == 0) {
                input = input.substring(0,1).toUpperCase() + input.substring(1).toLowerCase();
            } else if (input.charAt(i-1) == ' '){
                input = input.substring(0,i) + input.substring(i,i+1).toUpperCase() + input.substring(i+1).toLowerCase();
            }
        }
        return input;
    }

    /* Starting Game Message
     *
     * Prints Deadwood Ascii Text Art Logo
     */
    private void startGameMessage() {
        System.out.printf("%s%s%s%s%s%s%s", "   _____                 _                         _ \n",
                "  |  __ \\               | |                       | |\n",
                "  | |  | | ___  __ _  __| |_      _____   ___   __| |\n",
                "  | |  | |/ _ \\/ _` |/ _` \\ \\ /\\ / / _ \\ / _ \\ / _` |\n",
                "  | |__| |  __/ (_| | (_| |\\ V  V / (_) | (_) | (_| |\n",
                "  |_____/ \\___|\\__,_|\\__,_| \\_/\\_/ \\___/ \\___/ \\__,_|\n\n",
                "            By Ryan Lingg and Michael Albert               \n");
    }
}
