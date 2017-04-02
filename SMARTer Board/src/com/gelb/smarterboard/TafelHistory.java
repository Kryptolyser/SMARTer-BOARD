package com.gelb.smarterboard;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

public class TafelHistory {

	private int mHistorySize;
	private String mHistoryDir;
	private LinkedList<File> stack = new LinkedList<>();

	public TafelHistory(String historyDirectory, int size){
		mHistoryDir = historyDirectory;
		mHistorySize = size;
	}

	private int historyCount = 0;

	public void addToHistory(Tafel t){
		int width = (int) t.getCanvas().getWidth();
		int height = (int) t.getCanvas().getHeight();
		BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		WritableImage writableImage = new WritableImage(width, height);
		t.getCanvas().snapshot(null, writableImage);
		SwingFXUtils.fromFXImage(writableImage, bi);
		try {
			File savingFile = new File(mHistoryDir + "/" + historyCount + ".png");
			if(!savingFile.exists()){
				savingFile.mkdirs();
				savingFile.createNewFile();
			}

			ImageIO.write(bi, "PNG", savingFile);

			stack.add(savingFile);
			if(stack.size() > mHistorySize){
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
}
