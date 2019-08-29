package com.internshala.connectfour;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private Controller controller;
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader=new FXMLLoader(getClass().getResource("game.fxml"));
        GridPane rootGridPane=loader.load();

        controller=loader.getController();
        controller.createPlayground();

        MenuBar menuBar=createMenu();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
        Pane menuPane= (Pane) rootGridPane.getChildren().get(0);
        menuPane.getChildren().add(menuBar);
        Scene scene=new Scene(rootGridPane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Connect 4");

        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private MenuBar createMenu()
    {
        //file menu
        Menu fileMenu=new Menu("File");
        MenuItem newGame=new MenuItem("New Game");
        newGame.setOnAction(event -> controller.resetGame());

        MenuItem resetGame=new MenuItem("Reset Game");
        resetGame.setOnAction(event -> controller.resetGame());

        SeparatorMenuItem separatorMenuItem=new SeparatorMenuItem();
        MenuItem exitGame=new MenuItem("Exit");
        exitGame.setOnAction(event -> exitGame());
        fileMenu.getItems().addAll(newGame,resetGame,separatorMenuItem,exitGame);

        //help menu
        Menu helpMenu=new Menu("Help");
        MenuItem aboutGame=new MenuItem("About Connect4");
        aboutGame.setOnAction(event -> aboutConnect4Game());

        SeparatorMenuItem separator=new SeparatorMenuItem();
        MenuItem aboutMe=new MenuItem("About Me");
        aboutMe.setOnAction(event -> aboutMe());
        helpMenu.getItems().addAll(aboutGame,separator,aboutMe);
        MenuBar menuBar=new MenuBar();
        menuBar.getMenus().addAll(fileMenu,helpMenu);
        return  menuBar;
    }

    private void aboutMe() {
        Alert alert=new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About The developer");
        alert.setHeaderText("Uddeshya Rana");
        alert.setContentText("I love coding and playing games. Connect 4 is my first game developed using JavaFX."+
        " I will consider developing more interesting games in the future. Live and play ,but also make sure to eat healthy.");
        alert.show();
    }

    private void aboutConnect4Game() {
        Alert alert=new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Connect 4");
        alert.setHeaderText("How To Play?");
        alert.setContentText("Connect Four is a 2-player connection game in which "+
                "players first choose a color and then take turns dropping colored discs"+
                "from the top into a 7-column, six-row vertically suspended grid."+
                "The piece fall straight down, occupying the next available space within the column."+
                "The objective of the game is to be the first player to form a horizontal, vertical,"+
                " or diagonal line of 4 of one's own discs. Connect Four is a solved game." +
                "The first player can always win by playing the right moves.");
        alert.show();
    }

    private void exitGame() {
        Platform.exit(); //close the application
        System.exit(0); //also shuts down the threads of this app
    }


    public static void main(String[] args) {
        launch(args);
    }
}
