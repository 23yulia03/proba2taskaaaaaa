package com.example.proba2taskaaaaaa;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Line extends Shape {
    private double length;

    public Line(Color color, double length) {
        super(color);
        this.length = length;
    }

    @Override
    public double area() {
        return 0; // Линия не имеет площади
    }

    @Override
    public void draw(GraphicsContext gr) {
        gr.setStroke(color);
        gr.strokeLine(x, y, x + length, y);
    }

    @Override
    public Shape clone() {
        return new Line(color, length);
    }
}