package com.example.proba2taskaaaaaa;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;

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
    @FXML
    private Label modeLabel; // Label для отображения текущего режима

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

    private boolean isSelectionMode = false; // Флаг для режима выделения

    // Инициализация ListView
    public void initialize() {
        shapeListView.setItems(FXCollections.observableArrayList(
                "Линия", "Квадрат", "Треугольник", "Круг", "Угол", "Пятиугольник"
        ));
        modeLabel.setText("Режим: Рисование"); // Начальный режим
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

    // Переключение режима
    public void toggleSelectionMode(ActionEvent actionEvent) {
        isSelectionMode = !isSelectionMode; // Переключаем режим
        if (isSelectionMode) {
            modeLabel.setText("Режим: Выделение");
        } else {
            modeLabel.setText("Режим: Рисование");
        }
    }

    // Обработчик для нажатия мыши
    @FXML
    private void onMousePressed(MouseEvent event) {
        if (isSelectionMode && event.isSecondaryButtonDown()) { // Выделение области правой кнопкой мыши
            startX = event.getX();
            startY = event.getY();
            endX = startX;
            endY = startY;
            isDrawing = true;
        } else if (!isSelectionMode && event.isPrimaryButtonDown()) { // Рисование фигур левой кнопкой
            isDrawing = true;
            onMouseDragged(event);
        } else if (event.isPrimaryButtonDown() && !selectedShapes.isEmpty()) { // Перетаскивание фигур левой кнопкой
            offsetX = event.getX();
            offsetY = event.getY();
            isDrawing = true;
        }
    }

    // Обработчик для отпускания мыши
    @FXML
    private void onMouseReleased(MouseEvent event) {
        if (isSelectionMode && event.isSecondaryButtonDown()) { // Завершаем выделение области
            isDrawing = false;
            selectShapesInArea(); // Выбираем фигуры в выделенной области
        } else {
            isDrawing = false;
            currentShape = null;
        }
    }

    // Обработчик для движения мыши при зажатой кнопке
    @FXML
    private void onMouseDragged(MouseEvent event) {
        if (isDrawing) {
            if (isSelectionMode && event.isSecondaryButtonDown()) { // Продолжаем выделение области
                endX = event.getX();
                endY = event.getY();
                redrawCanvas(); // Перерисовываем холст
                drawSelectionRectangle(); // Рисуем прямоугольник выделения
            } else if (!isSelectionMode && event.isPrimaryButtonDown()) { // Рисование новой фигуры
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
            } else if (event.isPrimaryButtonDown() && !selectedShapes.isEmpty()) { // Перетаскивание выбранных фигур
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
                drawSelectedShapesOutline(); // Обводим выбранные фигуры
            }
        }
    }

    private Shape createShapeByName(String shapeName, Color color, double size) {
        switch (shapeName) {
            case "Квадрат":
                return new Square(color, size);
            case "Круг":
                return new Circle(color, size);
            case "Треугольник":
                return new Triangle(color, size, size);
            case "Линия":
                return new Line(color, size);
            case "Угол":
                return new Angle(color, size);
            case "Пятиугольник":
                return new Pentagon(color, size);
            default:
                return null;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        drawSelectedShapesOutline(); // Рисуем контуры выбранных фигур
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

    // Перерисовываем холст с учетом удаленных фигур
    private void redrawCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Shape shape : shapes) {
            shape.draw(gc);
        }
    }

    public void onUndo(ActionEvent actionEvent) {
        if (!undoStack.isEmpty()) {
            Shape lastShape = undoStack.pop();
            shapes.remove(lastShape);
            shapeQueue.remove(lastShape);
            redrawCanvas();
        }
    }
}