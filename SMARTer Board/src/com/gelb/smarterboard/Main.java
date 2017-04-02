package com.gelb.smarterboard;

import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import com.gelb.tools.ShapeRecognizer;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class Main extends Application {

	int LINE_WIDTH = 3;
	Color LINE_COLOR = Color.BLUE;
	Color SHAPE_COLOR = Color.GREEN;
	boolean writing = true;

	Canvas drawing;

	@FXML
	AnchorPane canvasAnchor;
	@FXML
	AnchorPane advancedPane;
	@FXML
	ImageView arrow;
	@FXML
	ImageView mode;
	@FXML
	Button showColor;

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

	}

	@FXML
	public void onMouseReleased(MouseEvent e){
		started = false;
		saveToStack();

		onLine(linePoints);
		linePoints.clear();
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
			File savingFile = new File(Paths.get(".").toAbsolutePath().normalize().toString() + "/" + fileName + ".history/" + historyCount + ".png");
			if(!savingFile.exists()){
				savingFile.mkdirs();
				savingFile.createNewFile();
			}

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

	//======LAYOUT START======

	@FXML
	public void changeAdvanced(MouseEvent e){
		if(advancedPane.getWidth() == 0)
		{
			advancedPane.setPrefWidth(250);
			arrow.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		}
		else
		{
			advancedPane.setPrefWidth(0);
			arrow.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
		}
	}

	@FXML
	public void changeMode(MouseEvent e){
		if(writing)
		{
			writing = false;
			showColor.setVisible(false);
			mode.setImage(new Image(getClass().getResource("erase.png").toExternalForm()));
		}
		else
		{
			writing = true;
			showColor.setVisible(true);
			mode.setImage(new Image(getClass().getResource("write.png").toExternalForm()));
		}
	}

	@FXML
	public void changeColor(ActionEvent event)
	{
		try
		{
			Button clickedBtn  = (Button) event.getSource();
			LINE_COLOR = hex2Rgb(clickedBtn.getId());
			showColor.setStyle("-fx-background-radius: 40; -fx-background-color: "+clickedBtn.getId().toString()+";");
		}catch (Exception ex) {ex.printStackTrace();}
	}

	//======LAYOUT END======

	public void undo(){

	}

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
	@FXML
	public void initialize(){
        drawing=new Canvas(Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight()-20);
        drawing.setOnMouseDragged(event->{onMouseDragged(event);});
        drawing.setOnMousePressed(event->{onMousePressed(event);});
        drawing.setOnMouseReleased(event->{onMouseReleased(event);});
        canvasAnchor.getChildren().add(drawing);
		graphicsContext = drawing.getGraphicsContext2D();
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
