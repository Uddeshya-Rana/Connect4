package com.internshala.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {
	private static final int COL =7;
	private static final int ROW =6;
	private static final int circleDiameter=80;
    private static final String Color1="#CA1D1D";  //reddish
	private static final String Color2="#E9EF34";  //light yellow

	private static String Player1="Player One";
	private static String Player2="Player Two";
	private boolean isPlayerOneTurn=true;
private boolean isAllowedToInsert=true;//flag to avoid same disk added multiple times
	private Disc[][] insertedDiscsArray= new Disc[ROW][COL];
	@FXML
	public GridPane rootGridPane;
	@FXML
	public Pane insertedDiscPane;
	@FXML
	public Label playerNameLabel;
	@FXML
	public TextField PlayerOneName;
	@FXML
	public TextField PlayerTwoName;
	@FXML
	public Button setNamesButton;
	@FXML
	public VBox Vbox;
	public void createPlayground()
	{
		Vbox.setPadding(new Insets(10,10,10,10));
		Shape rectangleWithHoles=createDiscGrid();
		rootGridPane.add(rectangleWithHoles,0,1);
		List<Rectangle> rectangleList=createClickableColumns();

		for (Rectangle rectangle:rectangleList)
		{
			rootGridPane.add(rectangle,0,1);
		}


	}
    private Shape createDiscGrid()
    {
	    Shape rectangleWithHoles=new Rectangle((COL+1) *circleDiameter, (ROW+1) *circleDiameter);

	    for(int row=0;row<ROW;row++)
	    {
		    for(int col=0;col<COL;col++)
		    {
			    Circle circle=new Circle();
			    circle.setRadius(circleDiameter/2);
			    circle.setCenterY(circleDiameter/2);
			    circle.setCenterX(circleDiameter/2);
			    circle.setSmooth(true);

			    circle.setTranslateX(col*(circleDiameter+5)+circleDiameter/4);
			    circle.setTranslateY(row*(circleDiameter+5)+circleDiameter/4);
			    rectangleWithHoles=Shape.subtract(rectangleWithHoles,circle);
		    }
	    }
	    rectangleWithHoles.setFill(Color.WHITE);
	    return rectangleWithHoles;
    }

    private List<Rectangle> createClickableColumns()
    {
	    List<Rectangle> rList=new ArrayList<>();
    	for (int col=0;col<COL;col++)
	    {
		    Rectangle rectangle=new Rectangle(circleDiameter,(ROW+1) *circleDiameter);
		    rectangle.setFill(Color.TRANSPARENT);
		    rectangle.setTranslateX(col*(circleDiameter+5)+circleDiameter/4);
		    rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
		    rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));
		    final int column=col;
		    rectangle.setOnMouseClicked(event -> {
		    	if(isAllowedToInsert)
			    {
			    	isAllowedToInsert=false;
				    insertDisc(new Disc(isPlayerOneTurn),column);
			    }
		    });
		    rList.add(rectangle);
	    }


    	return rList;

    }

	private void insertDisc(Disc disc,int column)
	{
		int row=ROW-1;
		while(row>=0)  //finding position in the column to fit the disc
		{
			if(getDiscIfPresent(row,column)==null) //if pos is empty
				break;
			row--;
		}
		 if(row<0) //if it is full no more discs can be inserted in the column
		 	return;
		 int currentRow=row;
		 insertedDiscsArray[row][column] = disc; //for structural changes: for developers
		 insertedDiscPane.getChildren().add(disc);
		 disc.setTranslateX(column * (circleDiameter + 5) + circleDiameter / 4);
		 TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.4), disc);//disc falling
		 translateTransition.setToY(row * (circleDiameter + 5) + circleDiameter / 4);                   //animation

		translateTransition.setOnFinished(event -> {  //to change the player's turn

			isAllowedToInsert=true;//finally, when the next disk is dropped allow next player to insert disc
			if(gameEnd(currentRow,column))
			{
                gameOver();
			}
			isPlayerOneTurn=!isPlayerOneTurn;
			playerNameLabel.setText(isPlayerOneTurn?Player1:Player2);
		});
		translateTransition.play();  //plays the animation

	}

	private void gameOver() {
		String winner=isPlayerOneTurn?Player1:Player2;
		Alert alert=new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect 4");
		alert.setHeaderText("The winner is "+winner);
		alert.setContentText("Want to play again?");

		ButtonType yes=new ButtonType("Yes");
		ButtonType no=new ButtonType("No, Exit");
		alert.getButtonTypes().setAll(yes,no);

		//Prevents IllegalStateException: showAndWait is not allowed during animation or layout processing
		Platform.runLater(()->{
			Optional<ButtonType> btnClicked =alert.showAndWait();//optional container class

			if(btnClicked.get()==yes)
			{
				//Reset the game
				resetGame();
			}
			else{
				//exit the game
				Platform.exit(); //close the application
				System.exit(0); //also shuts down the threads of this app

			}
		});

	}

	public void resetGame() {
		//Visual reset
		insertedDiscPane.getChildren().clear(); //removes all the inserted discs from pane
		//Structural reset
		for(int row=0;row<insertedDiscsArray.length;row++)
		{
			for(int col=0;col<insertedDiscsArray.length;col++)
				insertedDiscsArray[row][col]=null;
		}
		isPlayerOneTurn=true; //let player start the game

		createPlayground(); //prepare a fresh playground
	}

	private boolean gameEnd(int row, int column) {
		//Vertical points: eg. last disc inserted at row=2,col=3
		//check each element present in the column for winning combination

		List<Point2D> verticalPoints=IntStream.rangeClosed(row-3,row+3)   //range of row values=0,1,2,3,4,5
				                   .mapToObj(r->new Point2D(r,column))//0,3  1,3  2,3  3,3  4,3  5,3-->Point2D
				                   .collect(Collectors.toList());
		List<Point2D> horizontalPoints=IntStream.rangeClosed(column-3,column+3)
				.mapToObj(col->new Point2D(row,col))
				.collect(Collectors.toList());

		Point2D startPoint1=new Point2D(row-3,column+3);  //for diagonal 1
		List<Point2D> diagonal1Points=IntStream.rangeClosed(0,6)
				.mapToObj(i->startPoint1.add(i,-i))   //changing the startpoint value to insert in the list
				.collect(Collectors.toList());

		Point2D startPoint2=new Point2D(row-3,column-3);  //for diagonal 1
		List<Point2D> diagonal2Points=IntStream.rangeClosed(0,6)
				.mapToObj(i->startPoint2.add(i,i))
				.collect(Collectors.toList());
		boolean hasEnded=checkCombinations(verticalPoints)||checkCombinations(horizontalPoints)
				||checkCombinations(diagonal1Points)||checkCombinations(diagonal2Points);
		return hasEnded;
	}

	private boolean checkCombinations(List<Point2D> points) {

		int chain=0; //to count number of discs forming  a chain
		for (Point2D point:points)
		{
			int Ri=(int)point.getX(); //row index
			int Ci= (int) point.getY(); //column index
			Disc disc=getDiscIfPresent(Ri,Ci);
			if(disc!=null &&disc.isPlayerOneMove==isPlayerOneTurn)
			{ //checking if the last inserted disc belongs to the current player
			  chain++;
			  if(chain==4)
			  	return true;
			}
			else
				chain=0;
		}
		return false;
	}

	private Disc getDiscIfPresent(int row,int column)
	{
		//to prevent ArrayIndexOutofBoundException
		if(row>=ROW||column>=COL||row<0||column<0)
			return null;
		return insertedDiscsArray[row][column];
	}
	private static class Disc extends Circle
    {
		private final boolean isPlayerOneMove;

		public Disc(boolean isPlayerOneMove)
		{
			this.isPlayerOneMove=isPlayerOneMove;
			setRadius(circleDiameter/2);
			setFill(isPlayerOneMove?Color.valueOf(Color1):Color.valueOf(Color2));
			setCenterX(circleDiameter/2);
			setCenterY(circleDiameter/2);

		}
	}

	public void initialize(URL location, ResourceBundle resources) {


		final BooleanProperty firstTime=new SimpleBooleanProperty(true);
		setNamesButton.setOnMouseClicked(event -> {
			if(PlayerOneName.getText()==null||PlayerOneName.getText().trim().isEmpty())
				Player1="Player One";
			else
				Player1=PlayerOneName.getText();

			if(PlayerTwoName.getText()==null||PlayerTwoName.getText().trim().isEmpty())
				Player2="Player Two";
			else
				Player2=PlayerTwoName.getText();

			playerNameLabel.setText(isPlayerOneTurn?Player1:Player2);
		});
		//removes focus from text field
		PlayerOneName.focusedProperty().addListener((observable, oldValue, newValue)->{
			if(newValue && firstTime.get())
			{
				Vbox.requestFocus();
				firstTime.setValue(false);
			}
		});
	}


}
