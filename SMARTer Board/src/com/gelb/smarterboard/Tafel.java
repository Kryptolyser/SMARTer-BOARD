package com.gelb.smarterboard;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Tafel {

	private BufferedImage image;
	private Color backgroundColor;

	private Tafel(){

	}

	public Tafel(int width, int height) {
		image=new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
	}

	public void save(File folder) {
		if(!folder.exists()) {
			folder.mkdirs();
		}
		try {
			ImageIO.write(image, "PNG", new File(folder, "image.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Tafel load(File folder) {
		try{
			ImageIO.read(new File(folder, "image.png"));
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Failed to load TafelObject: "+folder.getAbsolutePath());
		}
		return null;
	}

}
