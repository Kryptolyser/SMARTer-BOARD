package com.gelb.smarterboard;

import java.awt.Checkbox;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.gelb.tools.Polygon2;
import com.gelb.tools.ShapeRecognizer;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;


public class Main extends Application {

	int LINE_WIDTH_PENCIL = 5, LINE_WIDTH_ERASER = 20;
	Color LINE_COLOR = Color.BLACK;
	Color SHAPE_COLOR = Color.GREEN;
	static boolean writing = true;
	File currentFile;

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
	@FXML
	ColorPicker colorPicker;
	@FXML
	CheckBox polygon;

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
		if (polygon.isSelected()){
			Polygon2 polygon=ShapeRecognizer.getPolygon(list);
			if(polygon!=null && writing) {
				graphicsContext.setStroke(Color.RED);
				graphicsContext.strokePolyline(polygon.x, polygon.y, polygon.getVertexCount());
				graphicsContext.setStroke(LINE_COLOR);
			}
		}
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
			setTafel(currentTafel.getUndo());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		toggleUndoRedoButtons();
	}

	public void redo(){
		try{
			setTafel( currentTafel.getRedo());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		toggleUndoRedoButtons();
	}

	//======LAYOUT START======

	private boolean advancedPaneState = true;
	@FXML
	public void changeAdvanced(MouseEvent e){
		TranslateTransition tt = new TranslateTransition(Duration.millis(500), advancedPane);
		RotateTransition rt = new RotateTransition(Duration.millis(500), arrow);
		if(advancedPaneState)
		{
			tt.setByX(-250f);
			advancedPaneState = false;
			rt.setByAngle(180);
		}
		else
		{
			tt.setByX(250f);
			advancedPaneState = true;
			rt.setByAngle(-180);
		}
	     tt.setAutoReverse(true);
	     tt.play();
	     rt.play();
	}

	@FXML
	public void changeMode(){
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
		graphicsContext.setLineCap(StrokeLineCap.ROUND);
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
				changeMode();
			graphicsContext.setStroke(LINE_COLOR);
			colorPicker.setValue(hex2Rgb(clickedBtn.getId()));
		}catch (Exception ex) {ex.printStackTrace();}
	}

	@FXML
	public void changeCustomColor(ActionEvent event)
	{
		String color = "#" + Integer.toHexString(colorPicker.getValue().hashCode()).substring(0, 6).toUpperCase();
		LINE_COLOR = hex2Rgb(color);
		showColor.setStyle("-fx-background-radius: 40; -fx-background-color: "+color+";");
		if (!writing)
			changeMode();
		graphicsContext.setStroke(LINE_COLOR);
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
		drawing=(new Canvas(Screen.getPrimary().getBounds().getWidth(),
				Screen.getPrimary().getBounds().getHeight() - 20));
		setTafel(new Tafel(drawing, java.awt.Color.WHITE));
		currentTafel.addToHistory();
		drawing.setCursor(Cursor.CROSSHAIR);
		colorPicker.setValue(Color.BLACK);
	}

	public void setTafel(Tafel t){
		Canvas c=t.getCanvas();
		canvasAnchor.getChildren().clear();
		drawing = c;
		canvasAnchor.getChildren().add(drawing);
		drawing.setOnMouseDragged(event->{onMouseDragged(event);});
		drawing.setOnMouseReleased(event->{onMouseReleased(event);});
		graphicsContext = drawing.getGraphicsContext2D();
		graphicsContext.setLineWidth(LINE_WIDTH_PENCIL);
		graphicsContext.setLineCap(StrokeLineCap.ROUND);
		currentTafel=t;
		for(WebFrame frame:t.getWebFrames()){
			canvasAnchor.getChildren().add(frame.webView);
		}

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


	@FXML
	public void fileNew(){
		setTafel(new Tafel(new Canvas(), java.awt.Color.WHITE));
	}

	@FXML
	public void fileOpen(){
		JFileChooser fileChooser=new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("SMARTer BOARD 2017 File (.sb)", "sb", "SB-File");
		fileChooser.setFileFilter(filter);
		((Stage)drawing.getScene().getWindow()).setIconified(true);
		fileChooser.showOpenDialog(null);
		((Stage)drawing.getScene().getWindow()).setIconified(false);
		if(fileChooser.getSelectedFile()!=null) {
			currentFile=fileChooser.getSelectedFile();
			setTafel(Tafel.load(currentFile));
		}
	}

	@FXML
	public void fileOpenRecent(){

	}

	@FXML
	public void fileSave(){
		if(currentFile!=null) {
			currentTafel.save(currentFile);
		} else {
			fileSaveAs();
		}
	}

	@FXML
	public void fileSaveAs(){
		JFileChooser fileChooser=new JFileChooser();
		((Stage)drawing.getScene().getWindow()).setIconified(true);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("SMARTer BOARD 2017 File (.sb)", "sb", "SB-File");
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.showSaveDialog(null);
		((Stage)drawing.getScene().getWindow()).setIconified(false);
		if(fileChooser.getSelectedFile()!=null) {
			currentFile=fileChooser.getSelectedFile();
			if(!currentFile.getName().endsWith(".sb"))
				currentFile=new File(currentFile.getAbsolutePath()+".sb");
			fileSave();
		}
	}

	@FXML
	public void clearCanvas(){
		graphicsContext.clearRect(0,0,drawing.getWidth(), drawing.getHeight());
		currentTafel.addToHistory();
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
