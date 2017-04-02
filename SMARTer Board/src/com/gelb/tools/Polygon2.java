package com.gelb.tools;

public class Polygon2 {
	public double[] x;
	public double[] y;
	int vertexCount;

	public Polygon2(double[] x2, double[] y2, int i) {
		x=new double[0];
		y=new double[0];
		vertexCount=0;
	}

	public Polygon2(double[] x, double[] y) {
		this.x=x;
		this.y=y;
		vertexCount=Math.min(x.length, y.length);
	}

	public int getVertexCount() {
		return vertexCount;
	}
}
