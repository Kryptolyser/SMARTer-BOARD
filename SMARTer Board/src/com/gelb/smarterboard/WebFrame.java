package com.gelb.smarterboard;

import org.json.JSONObject;

import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class WebFrame {
	WebView webView;
	WebEngine webEngine;
	String url;

	public WebFrame(double x, double y, double width, double height, String url){
		webView=new WebView();
		webView.setPrefHeight(height);
		webView.setPrefWidth(width);
		webView.setTranslateX(x);
		webView.setTranslateY(y);
		webEngine=webView.getEngine();
		webEngine.load(url);
		this.url=url;
		webView.setOnMouseClicked(event->{if(!Main.writing)((AnchorPane) webView.getParent()).getChildren().remove(webView);});
	}

	public static WebFrame fromJSON(JSONObject obj){
		return new WebFrame(obj.getDouble("x"), obj.getDouble("y"), obj.getDouble("width"), obj.getDouble("height"), obj.getString("url"));
	}

	public JSONObject toJSON(){
		JSONObject obj=new JSONObject();
		obj.put("x", webView.getTranslateX());
		obj.put("y", webView.getTranslateY());
		obj.put("width", webView.getPrefWidth());
		obj.put("height", webView.getPrefHeight());
		obj.put("url", url);
		return obj;
	}
}
