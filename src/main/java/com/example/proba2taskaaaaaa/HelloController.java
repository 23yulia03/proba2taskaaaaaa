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
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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
    private Stack<UndoAction> undoStack = new Stack<>(); // Стек для Undo действий

    private boolean isDrawing = false;
    private boolean isSelecting = false; // флаг для выделения
    private Shape currentShape = null;
    private Rectangle selectionRect = null; // прямоугольник выделения
    private Set<Shape> selectedShapes = new HashSet<>(); // выделенные фигуры

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
        if (isSelecting) {
            // Начинаем выделение
            selectionRect = new Rectangle(event.getX(), event.getY(), 0, 0);
        } else {
            isDrawing = true;
            onMouseDragged(event);
        }
    }

    // Обработчик для отпускания мыши
    @FXML
    private void onMouseReleased(MouseEvent event) {
        if (isSelecting) {
            // Завершаем выделение
            double x = selectionRect.getX();
            double y = selectionRect.getY();
            double width = selectionRect.getWidth();
            double height = selectionRect.getHeight();

            // Отмечаем фигуры, которые попадают в выделенную область
            selectedShapes.clear();
            for (Shape shape : shapes) {
                if (shape.x >= x && shape.x <= x + width && shape.y >= y && shape.y <= y + height) {
                    selectedShapes.add(shape);
                }
            }
        } else {
            isDrawing = false;
            currentShape = null;
        }
    }

    // Обработчик для движения мыши при зажатой клавише
    @FXML
    private void onMouseDragged(MouseEvent event) {
        if (isDrawing) {
            String shapeName = shapeListView.getSelectionModel().getSelectedItem(); // Получаем выбранное название фигуры
            Color color = colorPicker.getValue(); // Получаем цвет
            double size = Double.parseDouble(sizeInput.getText()); // Получаем размер фигуры
            GraphicsContext gc = canvas.getGraphicsContext2D();

            if (currentShape == null) {
                currentShape = createShapeByName(shapeName, color, size);
            }

            if (currentShape != null) {
                // Устанавливаем позицию фигуры на место курсора
                currentShape.setPosition(event.getX(), event.getY());
                currentShape.draw(gc);

                // Добавляем фигуру в список и стек для отмены
                shapes.add(currentShape);
                undoStack.push(new UndoAction(currentShape, UndoAction.Type.ADD)); // Сохраняем действие добавления фигуры

                // Создаем новую фигуру для следующего рисования
                currentShape = createShapeByName(shapeName, color, size);
            } else {
                showAlert("Ошибка", "Неверное название фигуры.");
            }
        } else if (isSelecting && selectionRect != null) {
            // Рисуем прямоугольник выделения
            double x = Math.min(event.getX(), selectionRect.getX());
            double y = Math.min(event.getY(), selectionRect.getY());
            double width = Math.abs(event.getX() - selectionRect.getX());
            double height = Math.abs(event.getY() - selectionRect.getY());

            selectionRect.setX(x);
            selectionRect.setY(y);
            selectionRect.setWidth(width);
            selectionRect.setHeight(height);

            // Перерисовываем холст с выделением
            redrawCanvas();
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x, y, width, height);
        }
    }

    // Метод для отмены последнего действия
    public void onUndo() {
        if (!undoStack.isEmpty()) {
            UndoAction lastAction = undoStack.pop();

            if (lastAction.getType() == UndoAction.Type.ADD) {
                // Если действие было добавлением фигуры, удаляем её
                shapes.remove(lastAction.getShape());
            } else if (lastAction.getType() == UndoAction.Type.COLOR_CHANGE) {
                // Если действие было изменением цвета, восстанавливаем старый цвет для всех фигур
                Map<Shape, Color> oldColors = lastAction.getOldColors();
                for (Map.Entry<Shape, Color> entry : oldColors.entrySet()) {
                    entry.getKey().setColor(entry.getValue());
                }
            }

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

    // Включение режима выделения
    @FXML
    private void onSelectMode() {
        isSelecting = true;
    }

    // Включение режима рисования
    @FXML
    private void onDrawMode() {
        isSelecting = false;
    }

    // Изменение цвета выбранных фигур
    @FXML
    private void onChangeColor() {
        Color newColor = colorPicker.getValue();

        // Создаем список для хранения старых цветов и фигур
        Map<Shape, Color> oldColors = new HashMap<>();

        // Для каждой выделенной фигуры сохраняем старый цвет и изменяем на новый
        for (Shape shape : selectedShapes) {
            oldColors.put(shape, shape.getColor()); // Сохраняем старый цвет
            shape.setColor(newColor); // Устанавливаем новый цвет
        }

        // Добавляем действие изменения цвета в стек отмены
        if (!oldColors.isEmpty()) {
            undoStack.push(new UndoAction(oldColors, UndoAction.Type.COLOR_CHANGE));
        }

        redrawCanvas(); // Обновляем холст с новым цветом
    }

    // Вспомогательный класс для действия Undo
    private static class UndoAction {
        enum Type {ADD, COLOR_CHANGE}

        private Shape shape; // Для действий с одной фигурой (например, добавление)
        private Map<Shape, Color> oldColors; // Для действий с несколькими фигурами (например, изменение цвета)
        private Type type;

        public UndoAction(Shape shape, Type type) {
            this.shape = shape;
            this.type = type;
            this.oldColors = null;
        }

        public UndoAction(Map<Shape, Color> oldColors, Type type) {
            this.shape = null;
            this.type = type;
            this.oldColors = oldColors;
        }

        public Shape getShape() {
            return shape;
        }

        public Map<Shape, Color> getOldColors() {
            return oldColors;
        }

        public Type getType() {
            return type;
        }
    }
}