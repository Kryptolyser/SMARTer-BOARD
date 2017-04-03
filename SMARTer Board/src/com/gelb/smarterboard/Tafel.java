package com.gelb.smarterboard;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONFileAPI;
import org.json.JSONObject;

import com.gelb.tools.ZipAPI;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;

public class Tafel {

	private Color backgroundColor;

	List<WebFrame> webFrames;

	private Canvas mCanvas;


	public Tafel(Canvas c, Color backColor) {
		mCanvas = c;
		backgroundColor = backColor;
		webFrames=new ArrayList<WebFrame>();
	}

	public void saveToFolder(File folder) {
		if (!folder.exists()) {
			folder.mkdirs();
		}
		try {
			int width = (int) mCanvas.getWidth();
			int height = (int) mCanvas.getHeight();
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			WritableImage writableImage = new WritableImage(width, height);
			SnapshotParameters parameters = new SnapshotParameters();
			parameters.setFill(javafx.scene.paint.Color.TRANSPARENT);
			mCanvas.snapshot(parameters, writableImage);
			SwingFXUtils.fromFXImage(writableImage, bi);
			ImageIO.write(bi, "PNG", new File(folder, "image.png"));
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("background_color", JSONFileAPI.toJSON(backgroundColor));
			JSONArray array=new JSONArray();
			for(WebFrame frame: webFrames)
				array.put(frame.toJSON());
			jsonObject.put("web_frames", array);
			JSONFileAPI.save(jsonObject, new File(folder, "structure.json"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save(File file) {
		System.out.println("Saving Tafel: "+file.getAbsolutePath());
		File temp = new File(file.getParentFile(), file.getName() + "_temp");
		saveToFolder(temp);
		ZipAPI.zipFolder(temp, file);
		ZipAPI.deleteFolder(temp);
	}

	public static Tafel loadFromFolder(File folder) {
		try {
			BufferedImage img = ImageIO.read(new File(folder, "image.png"));
			Canvas c = new Canvas(img.getWidth(), img.getHeight());
			c.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(img, null), 0, 0);
			JSONObject json = JSONFileAPI.load(new File(folder, "structure.json"));
			Color backgroundColor = JSONFileAPI.getColor(json.getJSONObject("background_color"));
			Tafel ret=new Tafel(c, backgroundColor);
			JSONArray array=json.getJSONArray("web_frames");
			array.forEach(obj->{ret.webFrames.add(WebFrame.fromJSON((JSONObject) obj));});
			return ret;
			} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Failed to load TafelObject: " + folder.getAbsolutePath());
			return null;
		}

	}

	public static Tafel load(File file) {
		System.out.println("Loading Tafel: "+file.getAbsolutePath());
		File temp = new File(file.getParentFile(), file.getName() + "_temp");
		ZipAPI.unzipFolder(file, temp);
		Tafel tafel = loadFromFolder(temp);
		ZipAPI.deleteFolder(temp);
		return tafel;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Canvas getCanvas() {
		return mCanvas;
	}

	// TafelHistory
	private int mHistorySize = 10;
	private String mHistoryDir = Paths.get(".").toAbsolutePath().normalize().toString() + "/test.history";
	private LinkedList<File> mHistoryFiles = new LinkedList<>();

	public void setHistorySize(int size) {
		mHistorySize = size;
	}

	public static int historyCount = 0; //current Tafel

	public void addToHistory() {
		File savingFile = new File(mHistoryDir + "/" + historyCount + ".sb");
		save(savingFile);

		mHistoryFiles.add(savingFile);
		if (mHistoryFiles.size() > mHistorySize) {
			mHistoryFiles.get(0).delete();
			mHistoryFiles.pollFirst();
		}
		clearRedo();
		historyCount++;
		
	}

	public boolean hasRedo() {
		return new File(mHistoryDir + "/" + (historyCount) + ".sb").exists();
	}

	public boolean hasUndo() {
		return new File(mHistoryDir + "/" + (historyCount - 2) + ".sb").exists();
	}

	public Tafel getRedo() throws Exception {
		if (hasRedo()) {
			File savingFile = new File(mHistoryDir + "/" + (historyCount) + ".sb");
			historyCount++;
			return load(savingFile);
		}
		throw new Exception("History is not that long");
	}

	public Tafel getUndo() throws Exception{
		if (hasUndo()) {
			File savingFile = new File(mHistoryDir + "/" + (historyCount - 2) + ".sb");
			historyCount--;
			return load(savingFile);
		}
		throw new Exception("History is not that long");
	}
	
	public void clearRedo(){
		new File(mHistoryDir + "/" + (historyCount+1) + ".sb").delete();
	}

	public List<WebFrame> getWebFrames() {
		return webFrames;
	}
}
