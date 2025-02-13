package com.example.proba2taskaaaaaa;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.*;

public class HelloController {
    @FXML
    private Canvas canvas;
    @FXML
    private ListView<String> shapeListView;
    @FXML
    private TextField sizeInput;
    @FXML
    private ColorPicker colorPicker;

    private ShapeFactory shapeFactory = new ShapeFactory();
    private ArrayList<Shape> shapes = new ArrayList<>();
    private Stack<Shape> undoStack = new Stack<>();
    private Queue<Shape> shapeQueue = new LinkedList<>();
    private Map<String, Integer> shapeCountMap = new HashMap<>();

    private boolean isDrawing = false;
    private Shape currentShape = null;

    // Для выделения и перетаскивания
    private double startX, startY; // Начальные координаты выделения
    private double endX, endY; // Конечные координаты выделения
    private ArrayList<Shape> selectedShapes = new ArrayList<>(); // Список выбранных фигур
    private double offsetX, offsetY; // Смещение для перетаскивания

    // Для Memento и CareTaker
    private CareTaker careTaker = new CareTaker();

    // Инициализация ListView
    public void initialize() {
        shapeListView.setItems(FXCollections.observableArrayList(
                "Линия", "Квадрат", "Треугольник", "Круг", "Угол", "Пятиугольник"
        ));
    }

    // Метод для очистки холста
    public void onClear() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        shapes.clear();
        undoStack.clear();
        shapeQueue.clear();
        shapeCountMap.clear();
        selectedShapes.clear();
    }

    // Создаёт фигуру на основе выбранного названия
    private Shape createShapeByName(String shapeName, Color color, double size) {
        switch (shapeName) {
            case "Линия":
                return shapeFactory.createShape("Line", color, size);
            case "Квадрат":
                return shapeFactory.createShape("Square", color, size);
            case "Треугольник":
                return shapeFactory.createShape("Triangle", color, size, size);
            case "Круг":
                return shapeFactory.createShape("Circle", color, size);
            case "Угол":
                return shapeFactory.createShape("Angle", color, size);
            case "Пятиугольник":
                return shapeFactory.createShape("Pentagon", color, size);
            default:
                return null;
        }
    }

    // Показывает уведомление об ошибке
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Обработчик для нажатия мыши
    @FXML
    private void onMousePressed(MouseEvent event) {
        if (event.isSecondaryButtonDown()) { // Выделение области при нажатии правой кнопки мыши
            startX = event.getX();
            startY = event.getY();
            endX = startX;
            endY = startY;
            isDrawing = true;
        } else if (event.isPrimaryButtonDown() && !selectedShapes.isEmpty()) { // Перетаскивание фигур
            offsetX = event.getX();
            offsetY = event.getY();
            isDrawing = true;
        } else { // Рисование фигур
            isDrawing = true;
            onMouseDragged(event);
        }
    }

    // Обработчик для отпускания мыши
    @FXML
    private void onMouseReleased(MouseEvent event) {
        if (event.isSecondaryButtonDown()) { // Завершаем выделение области
            isDrawing = false;
            selectShapesInArea();
        } else {
            isDrawing = false;
            currentShape = null;
        }
    }

    // Обработчик для движения мыши при зажатой клавише
    @FXML
    private void onMouseDragged(MouseEvent event) {
        if (isDrawing) {
            if (event.isSecondaryButtonDown()) { // Продолжаем выделение области
                endX = event.getX();
                endY = event.getY();
                redrawCanvas();
                drawSelectionRectangle();
            } else if (event.isPrimaryButtonDown() && !selectedShapes.isEmpty()) { // Перетаскивание фигур
                double newX = event.getX();
                double newY = event.getY();
                double deltaX = newX - offsetX;
                double deltaY = newY - offsetY;
                offsetX = newX;
                offsetY = newY;

                for (Shape shape : selectedShapes) {
                    shape.setPosition(shape.getX() + deltaX, shape.getY() + deltaY);
                }
                redrawCanvas();
                drawSelectedShapesOutline();
            } else { // Рисование фигур
                String shapeName = shapeListView.getSelectionModel().getSelectedItem();
                Color color = colorPicker.getValue();
                double size = Double.parseDouble(sizeInput.getText());
                GraphicsContext gc = canvas.getGraphicsContext2D();

                if (currentShape == null) {
                    currentShape = createShapeByName(shapeName, color, size);
                }

                if (currentShape != null) {
                    currentShape.setPosition(event.getX(), event.getY());
                    currentShape.draw(gc);
                    shapes.add(currentShape);
                    undoStack.push(currentShape);
                    shapeQueue.add(currentShape);
                    shapeCountMap.put(shapeName, shapeCountMap.getOrDefault(shapeName, 0) + 1);
                    currentShape = createShapeByName(shapeName, color, size);
                } else {
                    showAlert("Ошибка", "Неверное название фигуры.");
                }
            }
        }
    }

    // Метод для отмены последнего действия
    public void onUndo() {
        if (!undoStack.isEmpty()) {
            Shape lastShape = undoStack.pop();
            shapes.remove(lastShape);
            shapeQueue.remove(lastShape);
            redrawCanvas();
        }
    }

    // Перерисовываем холст с учетом удаленных фигур
    private void redrawCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Shape shape : shapes) {
            shape.draw(gc);
        }
    }

    // Метод для выделения фигур в области
    private void selectShapesInArea() {
        selectedShapes.clear();
        double minX = Math.min(startX, endX);
        double maxX = Math.max(startX, endX);
        double minY = Math.min(startY, endY);
        double maxY = Math.max(startY, endY);

        for (Shape shape : shapes) {
            if (shape.getX() >= minX && shape.getX() <= maxX && shape.getY() >= minY && shape.getY() <= maxY) {
                selectedShapes.add(shape);
            }
        }

        redrawCanvas();
        drawSelectedShapesOutline();
    }

    // Рисуем прямоугольник выделения
    private void drawSelectionRectangle() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);
        gc.strokeRect(Math.min(startX, endX), Math.min(startY, endY), Math.abs(endX - startX), Math.abs(endY - startY));
    }

    // Обводим выбранные фигуры
    private void drawSelectedShapesOutline() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);

        for (Shape shape : selectedShapes) {
            if (shape instanceof Square) {
                Square square = (Square) shape;
                gc.strokeRect(square.getX(), square.getY(), square.getSize(), square.getSize());
            } else if (shape instanceof Circle) {
                Circle circle = (Circle) shape;
                gc.strokeOval(circle.getX(), circle.getY(), circle.getRadius() * 2, circle.getRadius() * 2);
            } else if (shape instanceof Triangle) {
                Triangle triangle = (Triangle) shape;
                gc.strokePolygon(
                        new double[]{triangle.getX() - triangle.getBase() / 2, triangle.getX(), triangle.getX() + triangle.getBase() / 2},
                        new double[]{triangle.getY() + triangle.getHeight(), triangle.getY(), triangle.getY() + triangle.getHeight()},
                        3
                );
            }
            // Добавьте другие типы фигур по аналогии
        }
    }

    // Сохраняем состояние фигур
    public void saveState() {
        for (Shape shape : shapes) {
            careTaker.push(new Memento(shape));
        }
    }

    // Восстанавливаем состояние фигур
    public void restoreState() {
        if (careTaker.hasMementos()) {
            shapes.clear();
            while (careTaker.hasMementos()) {
                shapes.add(careTaker.poll().getState());
            }
            redrawCanvas();
        }
    }
}