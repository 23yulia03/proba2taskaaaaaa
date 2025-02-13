package com.example.proba2taskaaaaaa;

import javafx.scene.paint.Color;

public class ShapeFactory {
    public Shape createShape(String shapeType, Color color, double... params) {
        switch (shapeType.toLowerCase()) {
            case "line":
                return new Line(color, params[0]);
            case "square":
                return new Square(color, params[0]);
            case "triangle":
                return new Triangle(color, params[0], params[1]);
            case "circle":
                return new Circle(color, params[0]);
            case "angle":
                return new Angle(color, params[0]);
            case "pentagon":
                return new Pentagon(color, params[0]);
            default:
                throw new IllegalArgumentException("Неверный тип фигуры: " + shapeType);
        }
    }
}