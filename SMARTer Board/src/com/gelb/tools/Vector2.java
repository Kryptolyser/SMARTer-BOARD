package com.gelb.tools;

public class Vector2 {
	double x,y;
	public Vector2() {
		x=0;
		y=0;
	}

	public Vector2(double x, double y) {
		this.x=x;
		this.y=y;
	}

	public double length(){
		return Math.sqrt(x*x+y*y);
	}

	public Vector2 multiply(double d){
		return new Vector2(x*d, y*d);
	}

	public Vector2 divide(double d){
		return new Vector2(x/d, y/d);
	}

	public Vector2 toUnitVector(){
		return this.divide(length());
	}

	public double dot(Vector2 vec){
		return (this.x*vec.x)+(this.y*vec.y);
	}

	public double angle(Vector2 vec){
		return Math.toDegrees(Math.acos(dot(vec)/(length()*vec.length())));
	}
}
