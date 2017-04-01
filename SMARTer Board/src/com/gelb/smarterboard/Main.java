package com.gelb.smarterboard;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

	double[] previousPoint = new double[2];
	boolean started = false;

	@FXML
	public void onMouseDragged(MouseEvent e){
		graphicsContext.setFill(SHAPE_COLOR);
		graphicsContext.setStroke(LINE_COLOR);
		graphicsContext.setLineWidth(LINE_WIDTH);
		
		if(started)
			graphicsContext.strokeLine(previousPoint[0], previousPoint[1], e.getX(), e.getY());
		else
			started = true;

		previousPoint[0] = e.getX();
		previousPoint[1] = e.getY();
	}

	@FXML
	public void onMouseReleased(MouseEvent e){
		started = false;
		save();
		//Hier müsste ein Temp-Save geschehen
	}
	
	public void save(){
		BufferedImage bi = new BufferedImage((int)drawing.getWidth(),(int) drawing.getHeight(),BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=(Graphics2D) bi.getGraphics();
		WritableImage writableImage = new WritableImage((int)drawing.getWidth(), (int) drawing.getHeight());
		drawing.snapshot(null, writableImage);
		SwingFXUtils.fromFXImage(writableImage, bi);
		try {
			ImageIO.write(bi, "PNG", new File("Bild.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
