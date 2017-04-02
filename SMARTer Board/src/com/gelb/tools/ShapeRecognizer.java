package com.gelb.tools;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.sun.javafx.geom.Vec2d;

import javafx.scene.shape.Polygon;

public class ShapeRecognizer {

	public static Rectangle2D.Double getRectangle(List<Point2D.Double> points) {
		double smallestX = Double.POSITIVE_INFINITY, smallestY = Double.POSITIVE_INFINITY, biggestX = 0, biggestY = 0;
		for (Point2D.Double point : points) {
			if (point.getX() > biggestX)
				biggestX = point.getX();
			else if (point.getX() < smallestX)
				smallestX = point.getX();
			if (point.getY() > biggestY)
				biggestY = point.getY();
			else if (point.getY() < smallestY)
				smallestY = point.getY();
		}
		Rectangle2D.Double outerRect = new Rectangle2D.Double(smallestX, smallestY, biggestX - smallestX,
				biggestY - smallestY);
		double diff = Math.sqrt(outerRect.width * outerRect.width + outerRect.height + outerRect.height) / 8;
		if (points.get(0).distance(points.get(points.size() - 1)) > diff)
			return null;
		else {
			Rectangle2D.Double innerRect = new Rectangle2D.Double(smallestX + diff, smallestY + diff,
					biggestX - smallestX - 2 * diff, biggestY - smallestY - 2 * diff);
			int pointsInside = 0;
			for (Point2D.Double point : points) {
				if (outerRect.contains(point) && !innerRect.contains(point))
					pointsInside++;
			}
			if ((double) pointsInside / points.size() >= 0.8)
				return outerRect;
			else
				return null;
		}
	}

	public static Polygon2 getPolygon(List<Point2D.Double> points){
		List<Point2D.Double> polygonPoints=getPolygonPoints(points);
		//groupPoints(points);
		polygonPoints.add(points.get(points.size()-1));
		double x[]=new double[polygonPoints.size()];
		double y[]=new double[polygonPoints.size()];
		for(int i=0;i<polygonPoints.size();i++){
			x[i]=polygonPoints.get(i).x;
			y[i]=polygonPoints.get(i).y;

			System.out.println(polygonPoints.get(i).x+" "+polygonPoints.get(i).y);
		}
		return new Polygon2(x,y);
	}
	public static List<Point2D.Double> getPolygonPoints(List<Point2D.Double> points){
		Vector2 compVector = null;
		List<Point2D.Double> polygonPoints=new ArrayList<Point2D.Double>();
		for(int i=1; i<points.size();i++){
			Vector2 vector=new Vector2(points.get(i).x-points.get(i-1).x, points.get(i).y-points.get(i-1).y);
			if(compVector==null||Math.abs(compVector.angle(vector))>30){
				compVector=vector;
				polygonPoints.add(points.get(i));
			}

		}
		if(points.get(0).distance(points.get(points.size()-1))<30)
			points.set(points.size()-1, points.get(0));
		return polygonPoints;

	}

	public static void groupPoints(List<Point2D.Double> points) {
		for(int i=1;i<points.size();i++){
			if(points.get(i).distance(points.get(i-1))<400){
				points.get(i-1).setLocation((points.get(i).x+points.get(i-1).x)/2, (points.get(i).y+points.get(i-1).y)/2);
				points.remove(i);
				System.out.println("removed point");
			}

		}
	}

}
