package dataShared;

import java.awt.Color;

public class Util_ColorAdapter {
    public float r;
    public float g;
    public float b;
    public float a;
    public Color getColor() {
    	return new Color(r, g, b, a);
    }
    @Override
    public String toString() {
    	return "ColorFloat - r: " + r + ", g: " + g + ", b: " + b + ", a: " + a;
    }
}
