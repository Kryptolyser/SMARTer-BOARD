package com.gelb.smarterboard;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import com.gelb.tools.ShapeRecognizer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class Main extends Application {

	int LINE_WIDTH_PENCIL = 5, LINE_WIDTH_ERASER = 20;
	Color LINE_COLOR = Color.BLACK;
	Color SHAPE_COLOR = Color.GREEN;
	boolean writing = true;
	String save = "";

	Canvas drawing;

	//FXML variables
	@FXML
	AnchorPane canvasAnchor;
	@FXML
	AnchorPane advancedPane;
	@FXML
	AnchorPane contentOfAdvanced;
	@FXML
	ImageView arrow;
	@FXML
	ImageView mode;
	@FXML
	Button showColor;

	//Un-Redo variables
	@FXML
	ImageView undoBasicStrip;
	@FXML
	ImageView redoBasicStrip;
	@FXML
	MenuItem undoMenu;
	@FXML
	MenuItem redoMenu;

	GraphicsContext graphicsContext;

	Point2D.Double previousPoint = new Point2D.Double();
	boolean started = false;

	private ArrayList<Point2D.Double> linePoints = new ArrayList<>();

	Tafel currentTafel;

	private Stage primaryStage;
	private AnchorPane layout;

	@FXML
	public void onMouseDragged(MouseEvent e){
		if(started)
			graphicsContext.strokeLine(previousPoint.getX(), previousPoint.getY(), e.getX(), e.getY());
		else
			started = true;

		previousPoint = new Point2D.Double(e.getX(), e.getY());

		linePoints.add(new Point2D.Double(e.getX(), e.getY()));
	}

	@FXML
	public void onMouseReleased(MouseEvent e){
		started = false;
		currentTafel.addToHistory();

		onLine(linePoints);
		linePoints.clear();
		toggleUndoRedoButtons();
	}

	public void onLine(ArrayList<Point2D.Double> list){
		Rectangle2D.Double rect=ShapeRecognizer.getRectangle(list);
		//TODO repair ShapeRecognition
		//if(rect!=null) {
		//	graphicsContext.setFill(Color.RED);
		//	graphicsContext.fillRect(rect.x, rect.y, rect.width, rect.height);
		//	graphicsContext.setFill(LINE_COLOR);
		//}
	}

	public void toggleUndoRedoButtons(){
		if(currentTafel.hasUndo()){
			undoBasicStrip.setOpacity(1d);
			undoBasicStrip.setDisable(false);
			undoMenu.setDisable(false);
		}
		else{
			undoBasicStrip.setOpacity(0.1d);
			undoBasicStrip.setDisable(true);
			undoMenu.setDisable(true);
		}

		if(currentTafel.hasRedo()){
			redoBasicStrip.setOpacity(1d);
			redoBasicStrip.setDisable(false);
			redoMenu.setDisable(false);
		}
		else{
			redoBasicStrip.setOpacity(0.1d);
			redoBasicStrip.setDisable(true);
			redoMenu.setDisable(true);
		}
	}

	public void undo(){
		try{
			currentTafel = currentTafel.getUndo();
			setCanvas(currentTafel.getCanvas());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		toggleUndoRedoButtons();
	}

	public void redo(){
		try{
			currentTafel = currentTafel.getRedo();
			setCanvas(currentTafel.getCanvas());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		toggleUndoRedoButtons();
	}

	//======LAYOUT START======

	@FXML
	public void changeAdvanced(MouseEvent e){
		if(advancedPane.getWidth() == 0)
		{
			advancedPane.setPrefWidth(250);
			contentOfAdvanced.setVisible(true);
			arrow.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		}
		else
		{
			advancedPane.setPrefWidth(0);
			contentOfAdvanced.setVisible(false);
			arrow.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
		}
	}

	@FXML
	public void changeMode(MouseEvent e){
		if(writing)
		{
			setCursor();
			graphicsContext.setLineWidth(LINE_WIDTH_ERASER);
			writing = false;
			graphicsContext.setStroke(Color.WHITE);
			showColor.setVisible(false);
			mode.setImage(new Image(getClass().getResource("erase.png").toExternalForm()));
		}
		else
		{
			drawing.setCursor(Cursor.CROSSHAIR);
			writing = true;
			graphicsContext.setLineWidth(LINE_WIDTH_PENCIL);
			graphicsContext.setStroke(LINE_COLOR);
			showColor.setVisible(true);
			mode.setImage(new Image(getClass().getResource("write.png").toExternalForm()));
		}
		graphicsContext.setFill(SHAPE_COLOR);
	}

	public void setCursor(){
		Circle cursor = new Circle(LINE_WIDTH_ERASER / 2);
		cursor.setFill(Color.TRANSPARENT);
		cursor.setStroke(Color.BLACK);
		cursor.getStrokeDashArray().addAll(5d);
		WritableImage wi = new WritableImage(LINE_WIDTH_ERASER, LINE_WIDTH_ERASER);
		SnapshotParameters parameters = new SnapshotParameters();
		parameters.setFill(Color.TRANSPARENT);
		cursor.snapshot(parameters, wi);
		drawing.setCursor(new ImageCursor(wi,LINE_WIDTH_ERASER / 2,LINE_WIDTH_ERASER / 2));
	}

	@FXML
	public void changeColor(ActionEvent event)
	{
		try
		{
			Button clickedBtn  = (Button) event.getSource();
			LINE_COLOR = hex2Rgb(clickedBtn.getId());
			showColor.setStyle("-fx-background-radius: 40; -fx-background-color: "+clickedBtn.getId().toString()+";");
			if (!writing)
				changeMode(null);
		}catch (Exception ex) {ex.printStackTrace();}
	}

	@FXML
	public void changeFullscreen(ActionEvent event)
	{
		try {
			Stage c = (Stage) drawing.getScene().getWindow();
			c.setFullScreen(!c.isFullScreen());
		}catch (Exception ex) {ex.printStackTrace();}
	}

	@FXML
	public void close(ActionEvent event)
	{
		System.exit(0);
	}

	//======LAYOUT END======

	//Set widths
	@FXML
	public void setPencilWidth(MouseEvent event)
	{
		Slider slider = (Slider) event.getSource();
		LINE_WIDTH_PENCIL = (int)slider.getValue();
		graphicsContext.setLineWidth(LINE_WIDTH_PENCIL);
		if (writing)
			graphicsContext.setLineWidth(LINE_WIDTH_PENCIL);
	}

	@FXML
	public void setEraserWidth(MouseEvent event)
	{
		Slider slider = (Slider) event.getSource();
		LINE_WIDTH_ERASER = (int)slider.getValue();
		if (!writing)
		{
			graphicsContext.setLineWidth(LINE_WIDTH_ERASER);
			setCursor();
		}
	}

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
	@FXML
	public void initialize() {
		setCanvas(new Canvas(Screen.getPrimary().getBounds().getWidth(),
				Screen.getPrimary().getBounds().getHeight() - 20));
		currentTafel = new Tafel(drawing, java.awt.Color.WHITE);
		currentTafel.addToHistory();
		drawing.setCursor(Cursor.CROSSHAIR);
	}

	public void setCanvas(Canvas c){
		canvasAnchor.getChildren().clear();
		drawing = c;
		canvasAnchor.getChildren().add(drawing);
		drawing.setOnMouseDragged(event->{onMouseDragged(event);});
		drawing.setOnMouseReleased(event->{onMouseReleased(event);});
		graphicsContext = drawing.getGraphicsContext2D();
		graphicsContext.setLineWidth(LINE_WIDTH_PENCIL);
	}


	public static Color hex2Rgb(String colorStr) {
		java.awt.Color c = java.awt.Color.decode(colorStr.replace(" ", ""));
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		int a = c.getAlpha();
		double opacity = a / 255.0 ;
		return Color.rgb(r, g, b, opacity);
	}


	/**
	 * Initializes the root layout.
	 */
	private void initLayout() {
		try {
			// Load layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("BasicLayout.fxml"));
			layout = (AnchorPane) loader.load();

			// Show the scene containing the root layout.
			Scene scene = new Scene(layout);
			primaryStage.setScene(scene);
			primaryStage.setFullScreen(true);
			primaryStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
