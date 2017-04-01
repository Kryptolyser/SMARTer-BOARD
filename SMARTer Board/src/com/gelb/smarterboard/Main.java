package com.gelb.smarterboard;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;


public class Main extends Application {

	int LINE_WIDTH = 3;
	Color LINE_COLOR = Color.BLUE;
	Color SHAPE_COLOR = Color.GREEN;

	@FXML
	Canvas drawing;

	GraphicsContext graphicsContext;

	Point2D.Double previousPoint = new Point2D.Double();
	boolean started = false;

	private ArrayList<Point2D.Double> linePoints = new ArrayList<>();;
	
	@FXML
	public void onMouseDragged(MouseEvent e){
		graphicsContext.setFill(SHAPE_COLOR);
		graphicsContext.setStroke(LINE_COLOR);
		graphicsContext.setLineWidth(LINE_WIDTH);

		if(started)
			graphicsContext.strokeLine(previousPoint.getX(), previousPoint.getY(), e.getX(), e.getY());
		else
			started = true;

		previousPoint = new Point2D.Double(e.getX(), e.getY());
		
		linePoints.add(new Point2D.Double(e.getX(), e.getY()));
	}
		
	@FXML
	public void onMousePressed(MouseEvent e){
		linePoints.clear();
	}

	@FXML
	public void onMouseReleased(MouseEvent e){
		started = false;
		saveToStack();
		
		onLine(linePoints);
	}
	
	public void onLine(ArrayList<Point2D.Double> list){
		System.out.println(list.size());
	}


	//////Stacking der BufferedImages
	private int historyCount = 0;
	private final int HISTORY_LENGTH = 10;
	private String fileName = "test";
	private LinkedList<File> stack = new LinkedList<>();

	public void saveToStack(){
		BufferedImage bi = new BufferedImage((int)drawing.getWidth(),(int) drawing.getHeight(),BufferedImage.TYPE_INT_ARGB);
		WritableImage writableImage = new WritableImage((int)drawing.getWidth(), (int) drawing.getHeight());
		drawing.snapshot(null, writableImage);
		SwingFXUtils.fromFXImage(writableImage, bi);
		try {
			File savingFile = new File("./" + fileName + ".history/" + historyCount + ".png");
			if(!savingFile.exists())
				savingFile.createNewFile();

			ImageIO.write(bi, "PNG", savingFile);
			
			stack.add(savingFile);
			if(stack.size() > HISTORY_LENGTH){
				stack.get(0).delete();
				stack.pollFirst();
			}
			historyCount++;	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void redo(){
		
	}
	@FXML
	public void changeAdvanced(MouseEvent e){
		if(advancedPane.getWidth() == 35)
		{
			advancedPane.setPrefWidth(250);
			arrow.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		}
		else
		{
			advancedPane.setPrefWidth(35);
			arrow.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
		}
	}


	public void undo(){

	}













    //NICHT ANSCHAUEN!!!!

    private Stage primaryStage;
    private AnchorPane layout;

    @Override
	public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("SMARTer BOARD");

        initLayout();
	}

	public static void main(String[] args) {
		launch(args);
	}

	//post-init
	public void initialize(){
		graphicsContext = drawing.getGraphicsContext2D();
	}

	/**
     * Initializes the root layout.
     */
    private void initLayout() {
        try {
            // Load layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("UserInterface.fxml"));
            layout = (AnchorPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(layout);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
