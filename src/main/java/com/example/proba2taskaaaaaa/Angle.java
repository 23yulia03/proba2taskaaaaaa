package com.example.proba2taskaaaaaa;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Angle extends Shape {
    private double side;

    public Angle(Color color, double side) {
        super(color);
        this.side = side;
    }

    @Override
    public double area() {
        return 0.5 * side * side; // Площадь прямого угла как половина квадрата
    }

    @Override
    public void draw(GraphicsContext gr) {
        gr.setStroke(color);  // Используем stroke, а не fill, чтобы не закрашивать угол
        gr.setLineWidth(2);    // Устанавливаем толщину линии
        gr.strokeLine(x, y, x + side, y);           // Линия от точки (x, y) вправо
        gr.strokeLine(x, y, x, y - side);           // Линия вверх от точки (x, y)
    }

    @Override
    public Shape clone() {
        return new Angle(color, side);
    }
}