package com.example.proba2taskaaaaaa;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Circle extends Shape {
    private double radius;

    public Circle(Color color, double radius) {
        super(color);
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x, y, radius * 2, radius * 2);
    }

    @Override
    public Shape clone() {
        return new Circle(color, radius);
    }

    // Добавляем метод getRadius()
    public double getRadius() {
        return radius;
    }
}