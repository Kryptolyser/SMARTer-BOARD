package com.gelb.smarterboard;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.gelb.tools.Polygon2;
import com.gelb.tools.ShapeRecognizer;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.control.TextField;
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

	int currentPencilLineWidth = 3, currentEraserLineWidth = 20;
	Color currentLineColor = Color.BLACK;
	public static int cursor_mode = 0;
	public static final int MODE_DRAW=0, MODE_ERASE=1, MODE_SMARTFRAME=2;
	File currentFile;
	private WebFrame webFrameToInsert;

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
	@FXML
	TextField urlTextField;

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

	static Tafel currentTafel;

	private Stage primaryStage;
	private AnchorPane layout;

	public static long folderId = new Random().nextLong();

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
		if(cursor_mode==MODE_SMARTFRAME){
			Rectangle2D.Double rect=ShapeRecognizer.getRectangle(linePoints);
			undo();
			if(rect!=null){
				webFrameToInsert.setBounds(rect.x, rect.y, rect.width, rect.height);
				addWebFrame(webFrameToInsert);
				drawMode();
			}

		}
		else if (polygon.isSelected()){
			Polygon2 polygon=ShapeRecognizer.getPolygon(list);
			if(polygon!=null && cursor_mode == MODE_DRAW) {
				Tafel old=currentTafel;
				undo();
				graphicsContext.strokePolyline(polygon.x, polygon.y, polygon.getVertexCount());
				old.addToHistory();
				currentTafel.addToHistory();
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
	public void changeAdvanced(){
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
		if(cursor_mode == MODE_DRAW)
		{
			eraserMode();
		}
		else
		{
			drawMode();
		}
		graphicsContext.setLineCap(StrokeLineCap.ROUND);
	}
	
	
	public void eraserMode() {
		setCursor();
		graphicsContext.setLineWidth(currentEraserLineWidth);
		cursor_mode = MODE_ERASE;
		graphicsContext.setStroke(Color.WHITE);
		showColor.setVisible(false);
		mode.setImage(new Image(getClass().getResource("erase.png").toExternalForm()));
	}
	
	public void drawMode() {
		drawing.setCursor(Cursor.CROSSHAIR);
		cursor_mode = MODE_DRAW;
		graphicsContext.setLineWidth(currentPencilLineWidth);
		graphicsContext.setStroke(currentLineColor);
		showColor.setVisible(true);
		mode.setImage(new Image(getClass().getResource("write.png").toExternalForm()));
	}
	
	public void webframeMode() {
		cursor_mode=MODE_SMARTFRAME;
		drawing.setCursor(Cursor.CROSSHAIR);
		graphicsContext.setLineWidth(5);
		graphicsContext.setStroke(Color.BLACK);
		mode.setImage(new Image(getClass().getResource("rect.png").toExternalForm()));
		System.out.println(urlTextField.getText());
		showColor.setVisible(false);
	}
	
	public void setCursor(){
		Circle cursor = new Circle(currentEraserLineWidth / 2);
		cursor.setFill(Color.TRANSPARENT);
		cursor.setStroke(Color.BLACK);
		cursor.getStrokeDashArray().addAll(5d);
		WritableImage wi = new WritableImage(currentEraserLineWidth, currentEraserLineWidth);
		SnapshotParameters parameters = new SnapshotParameters();
		parameters.setFill(Color.TRANSPARENT);
		cursor.snapshot(parameters, wi);
		drawing.setCursor(new ImageCursor(wi,currentEraserLineWidth / 2,currentEraserLineWidth / 2));
	}

	@FXML
	public void changeColor(ActionEvent event)
	{
		try
		{
			Button clickedBtn  = (Button) event.getSource();
			currentLineColor = hex2Rgb(clickedBtn.getId());
			showColor.setStyle("-fx-background-radius: 40; -fx-background-color: "+clickedBtn.getId().toString()+";");
			if (cursor_mode != MODE_DRAW)
				changeMode();
			graphicsContext.setStroke(currentLineColor);
			colorPicker.setValue(hex2Rgb(clickedBtn.getId()));
		}catch (Exception ex) {ex.printStackTrace();}
	}

	@FXML
	public void changeCustomColor(ActionEvent event)
	{
		String color = "#" + Integer.toHexString(colorPicker.getValue().hashCode()).substring(0, 6).toUpperCase();
		currentLineColor = hex2Rgb(color);
		showColor.setStyle("-fx-background-radius: 40; -fx-background-color: "+color+";");
		if (cursor_mode != MODE_DRAW)
			changeMode();
		graphicsContext.setStroke(currentLineColor);
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
		currentPencilLineWidth = (int)slider.getValue();
		graphicsContext.setLineWidth(currentPencilLineWidth);
		if (cursor_mode==MODE_DRAW)
			graphicsContext.setLineWidth(currentPencilLineWidth);
	}

	@FXML
	public void setEraserWidth(MouseEvent event)
	{
		Slider slider = (Slider) event.getSource();
		currentEraserLineWidth = (int)slider.getValue();
		if (cursor_mode==MODE_ERASE)
		{
			graphicsContext.setLineWidth(currentEraserLineWidth);
			setCursor();
		}
	}

	@Override
	public void start(Stage primaryStage) {
		//delete history folders here


		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("SMARTer BOARD");

		initLayout();
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { }
		
		launch(args);
	}

	//post-init
	@FXML
	public void initialize() {
		drawing=(new Canvas(Screen.getPrimary().getBounds().getWidth(),
				Screen.getPrimary().getBounds().getHeight() - 20));
		setTafel(new Tafel(drawing));
		currentTafel.addToHistory();
		drawing.setCursor(Cursor.CROSSHAIR);
		colorPicker.setValue(Color.BLACK);
		changeAdvanced();
	}

	public void addWebFrame(WebFrame frame){
		canvasAnchor.getChildren().add(frame.webView);
		currentTafel.getWebFrames().add(frame);
		currentTafel.addToHistory();
	}

	public void setTafel(Tafel t){
		Canvas c=t.getCanvas();
		canvasAnchor.getChildren().clear();
		drawing = c;
		canvasAnchor.getChildren().add(drawing);
		drawing.setOnMouseDragged(event->{onMouseDragged(event);});
		drawing.setOnMouseReleased(event->{onMouseReleased(event);});
		graphicsContext = drawing.getGraphicsContext2D();
		graphicsContext.setLineCap(StrokeLineCap.ROUND);
		switch (cursor_mode) {
		case MODE_DRAW:
			drawMode();
			break;
		case MODE_ERASE:
			eraserMode();
			break;
		case MODE_SMARTFRAME:
			webframeMode();
			break;
		}
		drawMode();
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
		currentFile=null;
		clearCanvas();
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
	public void addWebFrame(){
		webFrameToInsert=new WebFrame(urlTextField.getText());
		webframeMode();
	}
	
	@FXML
	public void addFileFrame(){
		JFileChooser fileChooser=new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Videos (.mp4, .avi)", "mp4", "avi");
		fileChooser.setFileFilter(filter);
		filter = new FileNameExtensionFilter("Bilder (.png, .jpg, .bmp)", "png", "bmp", "jpg");
		fileChooser.setFileFilter(filter);
		filter = new FileNameExtensionFilter("Alle unterstützen Medien(.png, .jpg, .bmp, .mp4, .avi)", "png", "bmp", "jpg", "avi", "mp4");
		fileChooser.setFileFilter(filter);
		((Stage)drawing.getScene().getWindow()).setIconified(true);
		fileChooser.showOpenDialog(null);
		((Stage)drawing.getScene().getWindow()).setIconified(false);
		if(fileChooser.getSelectedFile()!=null) {
			webFrameToInsert=new WebFrame(fileChooser.getSelectedFile());
			webframeMode();
		}
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
		setTafel(new Tafel(new Canvas(Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight() - 20)));
		currentTafel.addToHistory();
		currentTafel.clearRedo();
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
