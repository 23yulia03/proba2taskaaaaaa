package com.example.proba2taskaaaaaa;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Pentagon extends Shape {
    private double side;

    public Pentagon(Color color, double side) {
        super(color);
        this.side = side;
    }

    @Override
    public double area() {
        double apothem = side / (2 * Math.tan(Math.PI / 5));
        return 5 * side * apothem / 2;
    }

    @Override
    public void draw(GraphicsContext gr) {
        double angle = Math.toRadians(72);
        double[] xPoints = new double[5];
        double[] yPoints = new double[5];
        for (int i = 0; i < 5; i++) {
            xPoints[i] = x + side * Math.cos(angle * i);
            yPoints[i] = y + side * Math.sin(angle * i);
        }
        gr.setFill(color);
        gr.fillPolygon(xPoints, yPoints, 5);
    }

    @Override
    public Shape clone() {
        return new Pentagon(color, side);
    }
}