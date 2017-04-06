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

	public WebFrame(String url) {
		webView = new WebView();
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

	public WebFrame(File f) {
		webView = new WebView();
		webEngine = webView.getEngine();
		if (f.getName().endsWith(".png") || f.getName().endsWith(".jpg") || f.getName().endsWith(".bmp")) {
			webEngine.loadContent("<html><img style=\"width: 100%;height: auto;\"src=\"file:///" + f.getAbsolutePath() + "\"> </img></html>");
		} else if (f.getName().endsWith(".mp4") || f.getName().endsWith(".avi")) {
			webEngine.loadContent("<html><video style=\"width: 100%;height: 95%;\" src=\"file:///" + f.getAbsolutePath() + "\" controls> </video></html>");
		}
		System.out.println("<html><img src=\"file://" + f.getAbsolutePath() + "\" </img></html>");
		webView.setOnMouseClicked(event -> {
			if (Main.cursor_mode == Main.MODE_ERASE) {
				((AnchorPane) webView.getParent()).getChildren().remove(webView);
				Main.currentTafel.getWebFrames().remove(this);
				Main.currentTafel.addToHistory();
			}
		});
	}
	
	public void setBounds(double x, double y, double width, double height) {
		webView.setPrefHeight(height);
		webView.setPrefWidth(width);
		webView.setTranslateX(x);
		webView.setTranslateY(y);
	}

	public static WebFrame fromJSON(JSONObject obj) {
		WebFrame webFrame=new WebFrame(obj.getString("url"));
		webFrame.setBounds(obj.getDouble("x"), obj.getDouble("y"), obj.getDouble("width"), obj.getDouble("height"));
		return webFrame;
	}

	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("x", webView.getTranslateX());
		obj.put("y", webView.getTranslateY());
		obj.put("width", webView.getPrefWidth());
		obj.put("height", webView.getPrefHeight());
		obj.put("url", webEngine.getDocument()==null? url: webEngine.getDocument().getDocumentURI());
		return obj;
	}
}
