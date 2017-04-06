package com.gelb.smarterboard;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.json.JSONObject;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
			html="<html><img style=\"width: 100%;height: auto;\"src=\"file:///" + f.getAbsolutePath() + "\"> </img></html>";
		} else if (f.getName().endsWith(".mp4") || f.getName().endsWith(".avi")) {
			html="<html><video style=\"width: 100%;height: 95%;\" src=\"file:///" + f.getAbsolutePath() + "\" controls> </video></html>";
		} else {
			html="<html><a style=\"padding:20px;color:white;background-color: #5555EE; border: 2px solid blue;border-radius: 10px;font-size: 40px; font-family: Arial; display:block;text-align:center; text-decoration: none;\" href=\"file:///"+f.getAbsolutePath()+"\">"+f.getName()+"<a></html>";
			webEngine.locationProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observableValue, String oldLoc, String newLoc) {
					try {
						Desktop.getDesktop().open(resource);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
		webEngine.loadContent(html);
		resource=f;
		System.out.println(html);
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
		WebFrame webFrame;
		if(obj.has("url"))
			webFrame=new WebFrame(obj.getString("url"));
		else
			webFrame=new WebFrame(new File(obj.getString("resource")));
		webFrame.setBounds(obj.getDouble("x"), obj.getDouble("y"), obj.getDouble("width"), obj.getDouble("height"));
		return webFrame;
	}

	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("x", webView.getTranslateX());
		obj.put("y", webView.getTranslateY());
		obj.put("width", webView.getPrefWidth());
		obj.put("height", webView.getPrefHeight());
		if(url!=null)
			obj.put("url", webEngine.getDocument()==null? url: webEngine.getDocument().getDocumentURI());
		else
			obj.put("resource", resource.getAbsolutePath());
		return obj;
	}
}
