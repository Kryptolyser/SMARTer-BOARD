package com.gelb.smarterboard;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import org.json.JSONFileAPI;
import org.json.JSONObject;

import com.gelb.tools.ZipAPI;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;

public class Tafel {

	private BufferedImage image;
	private Color backgroundColor;

	private Canvas mCanvas;

	private Tafel() {

	}

	public Tafel(Canvas c, Color backColor) {
		mCanvas = c;
		image = new BufferedImage((int)c.getWidth(), (int)c.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
		backgroundColor = backColor;
	}

	public void saveToFolder(File folder) {
		if (!folder.exists()) {
			folder.mkdirs();
		}
		try {
			ImageIO.write(image, "PNG", new File(folder, "image.png"));
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("background_color", JSONFileAPI.toJSON(backgroundColor));
			JSONFileAPI.save(jsonObject, new File(folder, "structure.json"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save(File file) {
		File temp=new File(file.getParentFile(), file.getName()+"_temp");
		saveToFolder(temp);
		ZipAPI.zipFolder(temp, file);
		ZipAPI.deleteFolder(temp);
	}

	public static Tafel loadFromFolder(File folder) {
		try {
			Tafel obj = new Tafel();
			obj.image = ImageIO.read(new File(folder, "image.png"));
			JSONObject json = JSONFileAPI.load(new File(folder, "structure.json"));
			obj.backgroundColor = JSONFileAPI.getColor(json.getJSONObject("background_color"));
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Failed to load TafelObject: " + folder.getAbsolutePath());
			return null;
		}

	}
	public static Tafel load(File file) {
		File temp=new File(file.getParentFile(), file.getName()+"_temp");
		ZipAPI.unzipFolder(file, temp);
		Tafel tafel=loadFromFolder(temp);
		ZipAPI.deleteFolder(temp);
		return tafel;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public BufferedImage getImage() {
		return image;
	}

	public Canvas getCanvas() {
		return mCanvas;
	}

	//TafelHistory
	private int mHistorySize = 10;
	private String mHistoryDir;
	private LinkedList<File> mHistoryFiles = new LinkedList<>();

	public void setHistorySize(int size){
		mHistorySize = size;
	}

	private int historyCount = 0;

	public void addToHistory(){
		int width = (int) mCanvas.getWidth();
		int height = (int) mCanvas.getHeight();
		BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		WritableImage writableImage = new WritableImage(width, height);
		mCanvas.snapshot(null, writableImage);
		SwingFXUtils.fromFXImage(writableImage, bi);
		try {
			File savingFile = new File(mHistoryDir + "/" + historyCount + ".png");
			if(!savingFile.exists()){
				savingFile.mkdirs();
				savingFile.createNewFile();
			}

			ImageIO.write(bi, "PNG", savingFile);

			mHistoryFiles.add(savingFile);
			if(mHistoryFiles.size() > mHistorySize){
				mHistoryFiles.get(0).delete();
				mHistoryFiles.pollFirst();
			}
			historyCount++;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void redo(){

	}

	public void undo(){

	}

}
