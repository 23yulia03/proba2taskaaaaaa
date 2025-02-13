package com.example.proba2taskaaaaaa;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Shape {
    protected Color color;
    protected double x, y;
    protected double width; // Добавляем поле width

    public Shape(Color color) {
        this.color = color;
    }

    public abstract double area();
    public abstract void draw(GraphicsContext gr);

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public double getWidth() {
        return width; // Возвращаем ширину
    }

    public void setWidth(double width) {
        this.width = width; // Устанавливаем ширину
    }

    public abstract Shape clone();
}