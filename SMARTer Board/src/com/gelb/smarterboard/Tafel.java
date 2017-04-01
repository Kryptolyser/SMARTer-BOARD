package com.gelb.smarterboard;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.json.JSONFileAPI;
import org.json.JSONObject;

import netscape.javascript.JSObject;

public class Tafel {

	private BufferedImage image;
	private Color backgroundColor;

	private Tafel() {

	}

	public Tafel(int width, int height, Color backColor) {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
		backgroundColor = backColor;
	}

	public void save(File folder) {
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

	public static Tafel load(File folder) {
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

	public Color getBackgroundColor() {
		return backgroundColor;
	}

}
