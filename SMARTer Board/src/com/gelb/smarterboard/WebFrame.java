package com.gelb.smarterboard;

import java.io.File;

import org.json.JSONObject;

import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class WebFrame {
	WebView webView;
	WebEngine webEngine;
	String url;
	String html;
	File resource;

	public WebFrame(double x, double y, double width, double height, String url) {
		webView = new WebView();
		webView.setPrefHeight(height);
		webView.setPrefWidth(width);
		webView.setTranslateX(x);
		webView.setTranslateY(y);
		webEngine = webView.getEngine();
		webEngine.load(url);
		this.url = url;
		webView.setOnMouseClicked(event -> {
			if (Main.cursor_mode == Main.MODE_ERASE) {
				((AnchorPane) webView.getParent()).getChildren().remove(webView);
				Main.currentTafel.getWebFrames().remove(this);
				Main.currentTafel.addToHistory();
			}
		});
	}

	public WebFrame(double x, double y, double width, double height, File f) {
		webView = new WebView();
		webView.setPrefHeight(height);
		webView.setPrefWidth(width);
		webView.setTranslateX(x);
		webView.setTranslateY(y);
		webEngine = webView.getEngine();
		if (f.getName().endsWith(".png") || f.getName().endsWith(".jpg") || f.getName().endsWith(".bmp"))
			webEngine.loadContent("<html><img src=\"" + f.getAbsolutePath() + "\"></html>");
		else if (f.getName().endsWith(".mp4") || f.getName().endsWith(".avi"))
			;
		webView.setOnMouseClicked(event -> {
			if (Main.cursor_mode == Main.MODE_ERASE) {
				((AnchorPane) webView.getParent()).getChildren().remove(webView);
				Main.currentTafel.getWebFrames().remove(this);
				Main.currentTafel.addToHistory();
			}
		});
	}

	public static WebFrame fromJSON(JSONObject obj) {
		return new WebFrame(obj.getDouble("x"), obj.getDouble("y"), obj.getDouble("width"), obj.getDouble("height"),
				obj.getString("url"));
	}

	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("x", webView.getTranslateX());
		obj.put("y", webView.getTranslateY());
		obj.put("width", webView.getPrefWidth());
		obj.put("height", webView.getPrefHeight());
		obj.put("url", url);
		return obj;
	}
}
