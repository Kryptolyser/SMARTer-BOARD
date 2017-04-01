package org.json;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class JSONFileAPI {

	public static JSONObject load(File file) throws IOException{
		StringBuilder builder=new StringBuilder();
		FileInputStream is=new FileInputStream(file);
		BufferedReader reader=new BufferedReader(new InputStreamReader(is));
		String line;
		while((line=reader.readLine())!=null) {
			builder.append(line);
			builder.append('\n');
		}
		reader.close();
		JSONObject obj=new JSONObject(builder.toString());
		return obj;
	}

	public static void save(JSONObject obj, File file) throws IOException {
		BufferedWriter writer=new BufferedWriter(new FileWriter(file));
		writer.write(obj.toString());
		writer.close();
	}

	public static Color getColor(JSONObject obj){
		return new Color(obj.getInt("R"), obj.getInt("G"), obj.getInt("B"), obj.getInt("A"));
	}

	public static JSONObject toJSON(Color color) {
		JSONObject obj=new JSONObject();
		obj.put("R", color.getRed());
		obj.put("G", color.getBlue());
		obj.put("B", color.getGreen());
		obj.put("A", color.getAlpha());
		return obj;
	}

}
