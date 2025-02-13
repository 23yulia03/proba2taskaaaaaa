package com.example.proba2taskaaaaaa;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Triangle extends Shape {
    private double base, height;

    public Triangle(Color color, double base, double height) {
        super(color);
        this.base = base;
        this.height = height;
    }

    @Override
    public double area() {
        return 0.5 * base * height;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        // Рисуем треугольник с вершиной вверх
        gc.fillPolygon(
                new double[]{x - base / 2, x, x + base / 2},  // X координаты вершин
                new double[]{y + height, y, y + height},      // Y координаты вершин
                3
        );
    }

    @Override
    public Shape clone() {
        return new Triangle(color, base, height);
    }

    // Добавляем метод getBase()
    public double getBase() {
        return base;
    }

    // Добавляем метод getHeight()
    public double getHeight() {
        return height;
    }
}