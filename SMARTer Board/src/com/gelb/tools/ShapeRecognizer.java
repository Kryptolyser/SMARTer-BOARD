package com.gelb.tools;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class ShapeRecognizer {

	public static Rectangle2D.Double getRectangle(List<Point2D.Double> points){
		double smallestX = Double.POSITIVE_INFINITY, smallestY = Double.POSITIVE_INFINITY, biggestX = 0, biggestY = 0;
		for(Point2D.Double point:points) {
			if(point.getX()>biggestX)
				biggestX=point.getX();
			else if(point.getX()<smallestX)
				smallestX=point.getX();
			if(point.getY()>biggestY)
				biggestY=point.getY();
			else if(point.getY()<smallestY)
				smallestY=point.getY();
		}
		Rectangle2D.Double outerRect=new Rectangle2D.Double(smallestX, smallestY, biggestX-smallestX, biggestY-smallestY);
		double diff=Math.sqrt(outerRect.width*outerRect.width+outerRect.height+outerRect.height)/10;
		Rectangle2D.Double innerRect=new Rectangle2D.Double(smallestX+diff, smallestY+diff, biggestX-smallestX-2*diff, biggestY-smallestY-2*diff);
		int pointsInside = 0;
		for(Point2D.Double point:points) {
			if(outerRect.contains(point)&&!innerRect.contains(point))
				pointsInside++;
		}
		if((double)pointsInside/points.size()>=0.3)
			return outerRect;
		else
			return null;
	}
}
