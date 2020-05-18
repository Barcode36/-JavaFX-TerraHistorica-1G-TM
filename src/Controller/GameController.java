package Controller;

import Model.*;
import Model.CardsAndTiles.CardsAndTiles;
import View.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.io.Serializable;
public class GameController implements Initializable, Serializable {

   final int ROW_NUMBER = 9;

   final int COLUMN_NUMBER = 13;

   private int selection = -1;
   public FileManager fm = new FileManager();
   public File save = fm.createSave("15");

   @FXML
   Pane mapPane;
   @FXML
   BorderPane borderPane;
   @FXML
   Button favorTilesButton;
   @FXML
   Button religions;
   @FXML
   Button bonusCardsButton;
   @FXML
   Button townTilesButton;
   @FXML
   Button scoringTilesButton;
   @FXML
   Button exchangeResourcesButton;
   @FXML
   Button scoreTable;
   @FXML
   Button passRound;
   @FXML
   Button skipTurn;
   @FXML
   Label testText;
   @FXML
   Button specialActions;
   @FXML
   Button terraform;
   @FXML
   Button upgradeShipping;
   @FXML
   Button upgradeStruct;
   @FXML
   Button sendPriest;
   @FXML
   Button powerActions;
   @FXML
   Button upgradeSpade;


   ArrayList<PlayerView> playerViewList;
   Button[][] terrains;
   Button[] actions;
   public Map map;
   Religion[] religionArr;
   Player[] playerList;
   CardsAndTiles cardsAndTiles;
   CardsAndTilesController cardsAndTilesController;
   RoundController roundController;
   PlayerHandler playerHandler;
   Player currentPlayer;


   public GameController() throws IOException {

   }


   @Override
   public void initialize(URL url, ResourceBundle resourceBundle) {
      //SoundController.playGameMusic(30);

      actions = new Button[]{specialActions, terraform, upgradeShipping, upgradeStruct, sendPriest, powerActions, upgradeSpade, passRound};
      System.out.println("Initialize is called");
      int index = 0;
      terrains = new Button[ROW_NUMBER][COLUMN_NUMBER];
      for (int i = 0; i < ROW_NUMBER * COLUMN_NUMBER; i++) {
         int row = i / 13;
         int col = i % 13;
         if (row % 2 == 0)
            terrains[row][col] = (Button) mapPane.getChildren().get(index);
         else if (col == 12) {
            terrains[row][col] = null;
            index--;
         } else
            terrains[row][col] = (Button) mapPane.getChildren().get(index);
         index++;
      }

      createSpaces();
      //testText.setText(playerList[0].getNickName());
      Thread thread = new Thread(new Runnable() {

         @Override
         public void run() {
            Runnable updater = new Runnable() {

               @Override
               public void run() {

                  if (playerList != null) {
                     HBox factionsView = new HBox(5);

                     playerViewList = new ArrayList<>();
                     for (int i = 0; i < playerList.length; i++) {
                        if (playerList[i] != null) {
                           playerViewList.add(new PlayerView(playerList[i]));

                        }
                     }
                     factionsView.getChildren().addAll(playerViewList);
                     //displayPlayerTurn(playerViewList);
                     factionsView.setPadding(new Insets(0, 0, 0, 20));
                     //factionsView.setStyle("fx-margin-bottom: 150;");
//                     VBox.setMargin(factionsView, new Insets(0,0,1500,0));
                     //factionsView.setMargin(,);

                     factionsView.setMaxWidth(200);
                     borderPane.setBottom(factionsView);
                     borderPane.setPadding(new Insets(0,0,50,0));
                     displayPlayerTurn(playerViewList);
                     showTown(terrains, map);
//                     ImageView imview = new ImageView();
//                     imview.setImage(new Image("file:src/Images/FactionImages/Image_AleisterCrowley.jpeg"));
//                     imview.setFitHeight(150);
//                     imview.setFitWidth(50);
//                     borderPane.setBottom(imview);

                  }

               }
            };

            while (true) {
               try {
                  Thread.sleep(700);
               } catch (InterruptedException ex) {
               }

               // UI update is run on the Application thread
               Platform.runLater(updater);
            }
         }

      });
      // don't let thread prevent JVM shutdown
      thread.setDaemon(true);
      thread.start();
      setButtonClickForInitialDwellings();

   }

   @FXML
   public void saveGameClicked() throws IOException {
      fm.saveGame(this, roundController,save);
      System.out.println("saved");
   }

   @FXML
   public void loadGameClicked() throws IOException
   {
         this.map = fm.loadGame(save, this).map;
         System.out.println(map.spaces[3][2].getType());
         this.religionArr = fm.loadGame(save, this).religionArr;
         this.playerList = fm.loadGame(save, this).playerList;
         System.out.println(playerList[0].getGoldNum());
         this.cardsAndTiles = fm.loadGame(save, this).cardsAndTiles;
         this.cardsAndTilesController = fm.loadGame(save, this).cardsAndTilesController;
         this.playerHandler = fm.loadGame(save, this).playerHandler;
         this.currentPlayer = fm.loadGame(save, this).currentPlayer;
         this.roundController = fm.loadGame(save, this).roundController;
    //     this.loadInitialMap();
   }
   @FXML
   public void skipTurnClicked() {
      disableButtonClicks();
      enableTerrains();
      enableActions();
      if (roundController.currentRound == 0) {
         System.out.println("Current player was: " + playerList[roundController.getCurrentPlayerId()].getNickName());
         System.out.println(roundController.getCurrentPlayerId());
         if ((roundController.getCurrentPlayerId() + 1) < playerList.length && playerList[roundController.getCurrentPlayerId() + 1] != null)
            roundController.setCurrentPlayerId(roundController.currentPlayerId + 1);
         else
            roundController.setCurrentPlayerId(0);
         currentPlayer = playerList[roundController.getCurrentPlayerId()];
         System.out.println("current dwelling: " + currentPlayer.getDwellingNum());
         if (currentPlayer.getBuildingNumber() < currentPlayer.getFaction().startingDwellingNum) {
            loadInitialMap();
            setButtonClickForInitialDwellings();
         }
         System.out.println("Current player is now: " + playerList[roundController.getCurrentPlayerId()].getNickName());
         System.out.println("--------------------------------------------------");
      } else {
         roundController.endTurn(playerList);
      }
   }

   @FXML
   public void passRoundClicked() {

      if(roundController.getCurrentRound() + 1 != roundController.getMAX_ROUND()) {
         cardsAndTilesController.showBonusCardsTable(cardsAndTiles, playerList[roundController.getCurrentPlayerId()], true);
      }
         int round1 = roundController.getCurrentRound();
         roundController.passRound(playerList);
         int round2 = roundController.getCurrentRound();
         cardsAndTiles.returnScoringTile(round1, round2, playerList, religionArr);
         //Reset advancement on religion for this round
         if (round2 != round1) {
            for (Religion religion : religionArr) {
               religion.resetRoundBasedPosition();
            }
         }

      System.out.println("Victory Point = " + engineerStrongholdAbility());
      if(roundController.isOver){
         this.scoreTableClicked();
         System.out.println("girdii");
      }

   }

   @FXML
   public void bonusCardsClicked() {
      cardsAndTilesController.showBonusCardsTable(cardsAndTiles, currentPlayer,false);
   }

   @FXML
   public void upgradeShippingClicked() {
      ActionController.showUpdateShippingDialogs(playerList[roundController.getCurrentPlayerId()], actions);
   }

   @FXML
   public void sendPriestClicked() {
      ReligionController religionController = new ReligionController();
      religionController.showChoices(playerList, religionArr, roundController.getCurrentPlayerId());

   }

   @FXML
   public void powerActionClicked() {
      showPowerActions(playerList[roundController.currentPlayerId]);


   }

   @FXML
   public void upgradeSpadeClicked() {
      ActionController.showUpdateSpadeDialogs(playerList[roundController.getCurrentPlayerId()], actions);
   }

   @FXML
   public void terraformClicked() {
      ActionController.terraform(playerList[roundController.currentPlayerId], terrains, map, actions, cardsAndTiles,religionArr);

   }

   @FXML
   public void upgradeStructureClicked() {

      ActionController.upgradeStructure(playerList[roundController.getCurrentPlayerId()] , terrains, map, actions, cardsAndTiles, religionArr);

   }
   @FXML
   public void religionsClicked() {
      ReligionController religionController = new ReligionController();
      religionController.showReligion(playerList, 0, religionArr, playerList.length, false);
   }

   @FXML
   public void scoreTableClicked() {
      ReligionController religionController = new ReligionController();
      //FOR RELIGION
      ArrayList<ArrayList<Integer>>[] scoresReligion = religionController.calculateReligionScores(religionArr, playerList);
      //FOR LONGEST PATH
      ArrayList<Integer>[] scoresLongestPath = map.calculatePathScores(playerList);
      showScoreTable(scoresReligion,scoresLongestPath);

      //Calculate victory points for the winner in last round
   }


   @FXML
   public void townTilesClicked() throws IOException {
      //todo
      cardsAndTilesController.showTownTilesTable(cardsAndTiles,playerList[roundController.currentPlayerId],religionArr,true);
   }

   @FXML
   public void scoringTilesClicked() {

      cardsAndTilesController.showScoringTilesTable(cardsAndTiles);
   }

   @FXML
   public void exchangeResourcesClicked() {
      showExchangeResources(playerList[roundController.currentPlayerId]);
   }



   @FXML
   public void favorTilesClicked() {

      cardsAndTilesController.showFavorTilesTable(cardsAndTiles, playerList[roundController.getCurrentPlayerId()], religionArr,false);

   }


   @FXML
   public void specialActionClicked() {
      ActionController.showSpeacialActions( playerList, religionArr, roundController.getCurrentPlayerId(),map,terrains,actions);
   }

   public void createSpaces() {
      System.out.println("Create is called");
      Space[][] spaces = new Space[ROW_NUMBER][COLUMN_NUMBER];
      for (int i = 0; i < ROW_NUMBER; i++) {
         for (int j = 0; j < COLUMN_NUMBER; j++) {
            Space space = new Space();
            if (terrains[i][j] == null)
               space.setType("Empty");
            else {
               switch (terrains[i][j].getId()) {
                  case "blackHexagon":
                     space.setType("Swamp");
                     break;
                  case "blueHexagon":
                     space.setType("Lakes");
                     break;
                  case "brownHexagon":
                     space.setType("Plains");
                     break;
                  case "greenHexagon":
                     space.setType("Forest");
                     break;
                  case "yellowHexagon":
                     space.setType("Desert");
                     break;
                  case "redHexagon":
                     space.setType("Wasteland");
                     break;
                  case "riverHexagon":
                     space.setType("River");
                     break;
                  case "whiteHexagon":
                     space.setType("Mountains");
                     break;
                  default:
                     space.setType("River");
                     break;
               }
            }
            spaces[i][j] = space;
            space.setX(i);
            space.setY(j);
         }
      }
      for (int i = 0; i < ROW_NUMBER; i++) {
         for (int j = 0; j < COLUMN_NUMBER; j++) {

            if (i == 0) {
               if (j == 2 || j == 6 || j == 10)
                  spaces[i][j].setBridgability(true);
            } else if (i == 1) {
               spaces[i][j].setBridgability(true);
            } else if (i == 2) {
               if (j == 2 || j == 4 || j == 6 || j == 8 || j == 10)
                  spaces[i][j].setBridgability(true);
            } else if (i == 3) {
               if (j == 0 || j == 2 || j == 5 || j == 6 || j == 8 || j == 10 || j == 11)
                  spaces[i][j].setBridgability(true);
            } else if (i == 4) {
               if (j == 4 || j == 7 || j == 10)
                  spaces[i][j].setBridgability(true);
            } else if (i == 5) {
               if (j == 0 || j == 1 || j == 4 || j == 5)
                  spaces[i][j].setBridgability(true);
            } else if (i == 6) {
               if (j == 3 || j == 5 || j == 7 || j == 9)
                  spaces[i][j].setBridgability(true);
            } else if (i == 7) {
               if (j == 0 || j == 1 || j == 6 || j == 7 || j == 9)
                  spaces[i][j].setBridgability(true);
            } else if (i == 8) {
               if (j == 5 || j == 8)
                  spaces[i][j].setBridgability(true);
            }

         }
      }

      map = new Map(spaces);
   }

   public void loadPlayers(ArrayList<Faction> factionList, ArrayList<String> playerNames, int totalPlayerNumber) {

      Player[] playerList = new Player[totalPlayerNumber];
      for (int i = 0; i < totalPlayerNumber; i++) {
         playerList[i] = new Player(factionList.get(i), playerNames.get(i), i);
      }
      this.playerList = playerList;
      playerHandler = new PlayerHandler();
      roundController = new RoundController(playerList);


   }

   public void loadCardsAndTiles() {
      cardsAndTiles = new CardsAndTiles(playerList.length, playerList);
      cardsAndTilesController = new CardsAndTilesController();
      cardsAndTiles.returnScoringTile(0, 1, playerList,religionArr);
   }

   public void loadReligion(int totalPlayerNumber) {
      religionArr = new Religion[4];
      int[] player_initial_islam = new int[totalPlayerNumber];
      int[] player_initial_chirst = new int[totalPlayerNumber];
      int[] player_initial_jew = new int[totalPlayerNumber];
      int[] player_initial_hindu = new int[totalPlayerNumber];
      for (int j = 0; j < totalPlayerNumber; j++) {
         player_initial_islam[j] = playerList[j].getInitialIslam();
         player_initial_chirst[j] = playerList[j].getInitialChristianity();
         player_initial_jew[j] = playerList[j].getInitialJudaism();
         player_initial_hindu[j] = playerList[j].getInitialHinduism();
      }
      religionArr[0] = new Religion(totalPlayerNumber, player_initial_islam);
      religionArr[1] = new Religion(totalPlayerNumber, player_initial_chirst);
      religionArr[2] = new Religion(totalPlayerNumber, player_initial_jew);
      religionArr[3] = new Religion(totalPlayerNumber, player_initial_hindu);
   }

   public void loadInitialMap() {

      disableActions();
      disableAllTerrains();
      for (int i = 0; i < 9; i++)
         for (int j = 0; j < 13; j++) {
            if (map.spaces[i][j].getType().equals(playerList[roundController.getCurrentPlayerId()].getFaction().TERRAIN_TILE)) {
               if (terrains[i][j] != null && !map.spaces[i][j].isOccupied())
                  terrains[i][j].setDisable(false);
            }
         }
   }

   /**
    * TODO
    * TAŞINACAK
    */
   public  void setButtonClickForInitialDwellings() {
      for (int i = 0; i < ROW_NUMBER; i++) {
         for (int j = 0; j < COLUMN_NUMBER; j++) {
            final int row = i;
            final int col = j;
            if (terrains[i][j] != null) {
               terrains[i][j].setOnMouseClicked(new EventHandler<MouseEvent>() {
                  @Override
                  public void handle(MouseEvent event) {
                     skipTurn.setDisable(false);
                     map.buildDwelling(map.spaces[row][col], map.spaces[row][col].getType(), true);
                     playerHandler.buildInitialDwelling(playerList[roundController.getCurrentPlayerId()]);
                     map.spaces[row][col].setPlayer(playerList[roundController.getCurrentPlayerId()]);
                     TerrainController.buildDwelling(terrains[row][col], map.spaces[row][col].getType());
                     map.spaces[row][col].setStructure("Dwelling");
                     for (int i = 0; i < ROW_NUMBER; i++) {
                        for (int j = 0; j < COLUMN_NUMBER; j++) {
                           if (terrains[i][j] != null)
                              terrains[i][j].setDisable(true);
                        }
                     }
                  }
               });
            }
         }
      }
   }


   /**
    * TODO
    * TAŞINACAK
    */
   public void disableButtonClicks() {
      for (int i = 0; i < ROW_NUMBER; i++) {
         for (int j = 0; j < COLUMN_NUMBER; j++) {
            if (terrains[i][j] != null)
               terrains[i][j].setOnMouseClicked(null);
         }
      }
   }

   /**
    * TODO
    * TAŞINACAK
    */
   public void enableTerrains() {
      for (int i = 0; i < ROW_NUMBER; i++) {
         for (int j = 0; j < COLUMN_NUMBER; j++) {
            if (terrains[i][j] != null && map.spaces[i][j].getType() != "River")
               terrains[i][j].setDisable(false);
         }
      }
   }

   /**
    * TODO
    * TAŞINACAK
    */
   public void disableAllTerrains() {
      for (int i = 0; i < ROW_NUMBER; i++) {
         for (int j = 0; j < COLUMN_NUMBER; j++) {
            if (terrains[i][j] != null)
               terrains[i][j].setDisable(true);
         }
      }
   }

   /**
    * TODO
    * TAŞINACAK
    */
   public void disableActions() {
      terraform.setDisable(true);
      upgradeShipping.setDisable(true);
      upgradeStruct.setDisable(true);
      sendPriest.setDisable(true);
      powerActions.setDisable(true);
      specialActions.setDisable(true);
      upgradeSpade.setDisable(true);
      passRound.setDisable(true);
   }

   /**
    * TODO
    * TAŞINACAK
    */
   public void enableActions() {
      terraform.setDisable(false);
      upgradeShipping.setDisable(false);
      upgradeStruct.setDisable(false);
      sendPriest.setDisable(false);
      powerActions.setDisable(false);
      specialActions.setDisable(false);
      upgradeSpade.setDisable(false);
      passRound.setDisable(false);
      skipTurn.setDisable(false);
   }

   /**
    * TODO
    * TAŞINACAK
    */
   public void displayPlayerTurn(ArrayList<PlayerView> playerViewList) {

      for (PlayerView playerView : playerViewList) {
         playerView.setStyle("");
      }

      switch (playerList[roundController.getCurrentPlayerId()].getFaction().TERRAIN_TILE) {
         case "Wasteland":
            playerViewList.get(roundController.getCurrentPlayerId()).setStyle("-fx-effect: dropshadow( gaussian , rgba(224, 15, 0, 1) , 10,0.5,0,1 );");
            break;
         case "Forest":
            playerViewList.get(roundController.getCurrentPlayerId()).setStyle("-fx-effect: dropshadow( gaussian , rgba(23, 150, 26, 1) , 10,0.5,0,1 );");
            break;
         case "Lakes":
            playerViewList.get(roundController.getCurrentPlayerId()).setStyle("-fx-effect: dropshadow( gaussian , rgba(0, 189, 214, 1) , 10,0.5,0,1 );");
            break;
         case "Desert":
            playerViewList.get(roundController.getCurrentPlayerId()).setStyle("-fx-effect: dropshadow( gaussian , rgba(214, 175, 0, 1) , 10,0.5,0,1 );");
            break;
         case "Mountains":
            playerViewList.get(roundController.getCurrentPlayerId()).setStyle("-fx-effect: dropshadow( gaussian , rgba(191, 191, 191, 1), 10,0.5,0,1 );");
            break;
         case "Swamp":
            playerViewList.get(roundController.getCurrentPlayerId()).setStyle("-fx-effect: dropshadow( gaussian , rgba(22, 20, 8, 1), 10,0.5,0,1 );");
            break;
         case "Plains":
            playerViewList.get(roundController.getCurrentPlayerId()).setStyle("-fx-effect: dropshadow( gaussian , rgba(152, 93, 27, 1), 10,0.5,0,1 );");
            break;
      }

   }

   /**
    * TODO
    * TAŞINACAK HERHALDE BU DA
    *
    * @param
    */
   public void showPowerActions(Player currentPlayer) {

      BorderPane border = new BorderPane();
      BackgroundImage bg = new BackgroundImage(new Image("religion_bg.png"), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
      border.setBackground(new Background(bg));
      GridPane gridPane = new GridPane();
      gridPane.setHgap(10);
      gridPane.setVgap(10);
      Button select = new Button("Select");
      select.setMaxHeight(100);
      select.setMinWidth(100);
      BorderPane border_bottom = new BorderPane();
      border.setBottom(border_bottom);
      border_bottom.setCenter(select);

      for (int i = 0; i < 6; i++) {
         ImageView power_middle = new ImageView("arrow.png");
         ImageView power_image = new ImageView("power.png");
         ImageView bridge = new ImageView("bridge.png");
         ImageView priest = new ImageView("priest.png");
         ImageView worker =  new ImageView("worker.png");
         ImageView gold = new ImageView("gold.png");
         ImageView spade =  new ImageView("spade.png");
         Label label1 = new Label("\n3");
         label1.setTextFill(Color.WHITE);
         Label label2 = new Label("\n2");
         label2.setTextFill(Color.WHITE);
         label1.setFont(new Font("Stencil", 40));
         label2.setFont(new Font("Stencil", 40));
         label1.setOpacity(0.6);
         label2.setOpacity(0.6);
         power_middle.setFitHeight(150);
         power_middle.setFitWidth(150);
         priest.setFitWidth(150);
         priest.setFitHeight(150);
         worker.setFitWidth(150);
         worker.setFitHeight(150);
         gold.setFitWidth(150);
         gold.setFitHeight(150);
         spade.setFitWidth(150);
         spade.setFitHeight(150);
         power_image.setFitWidth(150);
         power_image.setFitHeight(150);
         bridge.setFitHeight(150);
         bridge.setFitWidth(150);
         HBox option;
         if(i == 0){
              label2.setText("\n1");
              option = new HBox(power_image, label1, power_middle, bridge,label2 );
         }else if (i == 1) {
               label2.setText("\n1");
               option = new HBox(power_image, label1, power_middle, priest,label2 );
         }else if (i == 2) {
            label1.setText("\n4");
            option = new HBox(power_image, label1 , power_middle, worker, label2 );
         }else if (i == 3) {
            label1.setText("\n4");
            label2.setText("\n7");
            option = new HBox(power_image, label1, power_middle,gold , label2);
         }else if (i == 4) {
             label1.setText("\n4");
             label2.setText("\n1");
             option = new HBox(power_image, label1, power_middle,spade, label2);
         }else {
            label1.setText("\n6");
            option = new HBox(power_image, label1, power_middle, spade,label2 );
         }
         GridPane tempPane = new GridPane();
         option.setMaxWidth(tempPane.getWidth() / 3);
         option.setMaxHeight(tempPane.getHeight() / 3);
         tempPane.add(option,0,0);
         tempPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
               DropShadow borderGlow = new DropShadow();
               borderGlow.setColor(Color.ORANGE);
               borderGlow.setOffsetX(0f);
               borderGlow.setOffsetY(0f);
               borderGlow.setWidth(50);
               borderGlow.setHeight(50);
               tempPane.setEffect(borderGlow);
            }
         });
         int finalI = i;
         tempPane.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
               if (selection != finalI)
                  tempPane.setEffect(null);

            }
         });

         tempPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
               setSelection(finalI);
               for (int i = 0; i < 6; i++) {
                  if (i != getSelection())
                     gridPane.getChildren().get(i).setEffect(null);
               }
            }
         });
         gridPane.add(tempPane, i % 2, i / 2);
      }

      border.setCenter(gridPane);
      final Stage dialog = new Stage();
      dialog.initModality(Modality.APPLICATION_MODAL);
      Scene dialogScene = new Scene(border, 1100, 600);
      dialog.setScene(dialogScene);
      dialog.setTitle("Power Action");
      dialog.setResizable(false);

      select.setOnMouseClicked(new EventHandler<MouseEvent>() {

         @Override
         public void handle(MouseEvent event) {
            playerList[roundController.currentPlayerId].setBowlThreePower(12);
            playerList[roundController.currentPlayerId].setWorkerNum(1000);
            playerList[roundController.currentPlayerId].setGoldNum(1000);
            int chosen = getSelection();
            System.out.println("Selection: " + chosen);
            setSelection(chosen);
            dialog.close();
            if (getSelection() == 0) {
               boolean checkBridgability = false;
               for(int i = 0; i < ROW_NUMBER; i++) {
                  for (int j = 0; j < COLUMN_NUMBER; j++) {
                     if (map.spaces[i][j].getType().equals(playerList[roundController.currentPlayerId].getFaction().TERRAIN_TILE) && map.spaces[i][j].getBridgability() && !map.spaces[i][j].getBridgeConnection() && map.spaces[i][j].isOccupied()) {
                        System.out.println("lala");
                        checkBridgability = true;
                     }
                  }
               }
               if(checkBridgability) {
                  if (playerHandler.usePowerAction(0, currentPlayer)) {
                     System.out.println("Girdi");
                     disableAllTerrains();
                     TerrainController.buildBridge(playerList[roundController.currentPlayerId].getFaction().TERRAIN_TILE, terrains, map, mapPane, actions);
                     System.out.println("Köprü kuruldu");
                  }
               }
            } else if (getSelection() == 4) {
               if (playerHandler.usePowerAction(4, currentPlayer)) {
                  disableActions();
                  terraform.setDisable(false);
               }
            } else if (getSelection() == 5) {
               if (playerHandler.usePowerAction(5, currentPlayer)) {
                  disableActions();
                  terraform.setDisable(false);
                  if (currentPlayer.getFreeSpade() > 0) {
                     disableActions();
                     terraform.setDisable(false);
                  }
               }
            } else {
               if (playerHandler.usePowerAction(getSelection(), currentPlayer)) {
                  System.out.println("Köprü kuruldu");
               }
            }
         }
      });

      dialog.show();
   }
   private void showExchangeResources(Player currentPlayer) {
      BorderPane border = new BorderPane();
      BackgroundImage bg = new BackgroundImage(new Image("religion_bg.png"), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
      border.setBackground(new Background(bg));
      GridPane gridPane = new GridPane();
      gridPane.setHgap(10);
      gridPane.setVgap(10);
      Button select = new Button("Select");
      select.setMaxHeight(100);
      select.setMinWidth(100);
      BorderPane border_bottom = new BorderPane();
      border.setBottom(border_bottom);
      border_bottom.setCenter(select);

      for (int i = 0; i < 6; i++) {
         ImageView power_middle = new ImageView("arrow.png");
         ImageView power_image = new ImageView("power.png");
         ImageView priest = new ImageView("priest.png");
         ImageView worker =  new ImageView("worker.png");
         ImageView gold = new ImageView("gold.png");
         Label label1 = new Label("\n3");
         label1.setTextFill(Color.WHITE);
         Label label2 = new Label("\n2");
         label2.setTextFill(Color.WHITE);
         label1.setFont(new Font("Stencil", 40));
         label2.setFont(new Font("Stencil", 40));
         label1.setOpacity(0.6);
         label2.setOpacity(0.6);
         power_middle.setFitHeight(150);
         power_middle.setFitWidth(150);
         priest.setFitWidth(150);
         priest.setFitHeight(150);
         worker.setFitWidth(150);
         worker.setFitHeight(150);
         gold.setFitWidth(140);
         gold.setFitHeight(140);
         power_image.setFitWidth(150);
         power_image.setFitHeight(150);
         HBox option;
         if(i == 0){
            label1.setText("\n5");
            label2.setText("\n1");
            option = new HBox(power_image, label1, power_middle, priest,label2 );
         }else if (i == 1) {
            label1.setText("\n1");
            label2.setText("\n1");
            option = new HBox(priest, label1, power_middle, worker,label2 );
         }else if (i == 2) {
            label1.setText("\n3");
            label2.setText("\n1");
            option = new HBox(power_image, label1 , power_middle, worker, label2 );
         }else if (i == 3) {
            label1.setText("\n1");
            label2.setText("\n1");
            option = new HBox(worker, label1, power_middle, gold , label2);
         }else if (i == 4) {
            label1.setText("\n1");
            label2.setText("\n1");
            option = new HBox(power_image, label1, power_middle,gold, label2);
         }else {
            label1.setText("\nBOWL 2");
            label2.setText("\nBOWL 3");

            option = new HBox(power_image, label1, power_middle,label2 );
         }
         GridPane tempPane = new GridPane();
         tempPane.add(option,0,0);

         tempPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
               DropShadow borderGlow = new DropShadow();
               borderGlow.setColor(Color.ORANGE);
               borderGlow.setOffsetX(0f);
               borderGlow.setOffsetY(0f);
               borderGlow.setWidth(50);
               borderGlow.setHeight(50);
               tempPane.setEffect(borderGlow);
            }
         });
         int finalI = i;
         tempPane.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
               if (selection != finalI)
                  tempPane.setEffect(null);

            }
         });

         tempPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
               setSelection(finalI);
               for (int i = 0; i < 6; i++) {
                  if (i != getSelection())
                     gridPane.getChildren().get(i).setEffect(null);
               }
            }
         });
         gridPane.add(tempPane, i % 2, i / 2);
      }

      border.setCenter(gridPane);
      final Stage dialog = new Stage();
      dialog.initModality(Modality.APPLICATION_MODAL);
      Scene dialogScene = new Scene(border, 1100, 600);
      dialog.setScene(dialogScene);
      dialog.setTitle("Power Action");
      dialog.setResizable(false);

      select.setOnMouseClicked(new EventHandler<MouseEvent>() {

         @Override
         public void handle(MouseEvent event) {
            playerList[roundController.currentPlayerId].setBowlThreePower(12); //TODO
            int chosen = getSelection();
            System.out.println("Selection: " + chosen);
            setSelection(chosen);
            playerHandler.exchangeResources(playerList[roundController.getCurrentPlayerId()], chosen);
            dialog.close();
         }
      });
      dialog.showAndWait();
   }
   public int getSelection() {
      return selection;
   }

   private void setSelection(int selection) {
      this.selection = selection;
   }


   public CardsAndTiles getCardsAndTiles() {

      return cardsAndTiles;
   }

   public void showTown(Button[][] terrains, Map map)

   {
      for (int i = 0; i < 9; i++) {
         for (int j = 0; j < 13; j++) {
            if( terrains[i][j] != null && map.spaces[i][j].isMarked())
               terrains[i][j].getStyleClass().add("townShadow");
         }
      }

   }

   public int engineerStrongholdAbility() {
      int counter = 0;
      for (int i = 0; i < ROW_NUMBER; i++) {
         for (int j = 0; j < COLUMN_NUMBER; j++) {
            if (map.spaces[i][j] != null) {
               if (map.spaces[i][j].getType().equals("Mountains") && map.spaces[i][j].getBridgeConnection()) {
                  ArrayList<Space> bridgables = map.bridgeables(map.spaces[i][j]);
                  for (int k = 0; k < bridgables.size(); k++)
                     if (bridgables.get(k).getBridgeConnection() && bridgables.get(k).getBridgeType().equals("Mountains") && bridgables.get(k).isOccupied()) {
                        counter++;
                     }
               }
            }
         }
      }
      return 3*(counter/2);
   }

   public void showScoreTable(ArrayList<ArrayList<Integer>>[] religionScores,ArrayList<Integer>[] pathScores) {
      Pane emptyPane = new Pane();
      final Stage dialog = new Stage();
      dialog.initModality(Modality.APPLICATION_MODAL);
      Scene dialogScene = new Scene(emptyPane, 1100, 600);
      dialog.setScene(dialogScene);
      dialog.setTitle("Score Table");
      dialog.setResizable(false);
      emptyPane.setBackground(new Background( new BackgroundImage( new Image("score_table_background.jpg"), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
              BackgroundSize.DEFAULT)));

      dialog.show();

      if(roundController.isOver())
      {
         System.out.println("girdi");
         dialog.setOnCloseRequest(e->Platform.exit());
      }
   }

   public Map getMap() {
      return map;
   }

   public ArrayList<PlayerView> getPlayerViewList() {
      return playerViewList;
   }

   public Player[] getPlayerList() {
      return playerList;
   }

   public Religion[] getReligionArr() {
      return religionArr;
   }

   public PlayerHandler getPlayerHandler() {
      return playerHandler;
   }

   public Player getCurrentPlayer() {
      return currentPlayer;
   }

   public RoundController getRoundController() {return roundController;}

   public void setMap(Map map) {
      this.map = map;
   }

   public void setPlayerList(Player[] playerList) {
      this.playerList = playerList;
   }

   public void setCardsAndTiles(CardsAndTiles cat) {
      this.cardsAndTiles = cat;
   }

   public void setReligionArr(Religion[] religions) {
      this.religionArr = religions;
   }

   public void setPlayerHandler(PlayerHandler ph) {
      this.playerHandler = ph;
   }

   public void setCurrentPlayer(Player p1) {
      this.currentPlayer = p1;
   }

   public void setRoundController(RoundController r1){this.roundController = r1;}


}