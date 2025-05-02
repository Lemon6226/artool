package models;

import java.util.ArrayList;

//pro uložení tvarů vytovřených z čar
public class LineCanvas {
    private ArrayList<LineGroup> shapes;

    //udělá prázdny seznam tvarů
    public LineCanvas() {
        this.shapes = new ArrayList<>();
    }

    //přidá nový tvar do plátna (nečekaně)
    public void addShape(LineGroup shape) { this.shapes.add(shape);}

    //vymaže všechny tvary z plátna
    public void clearShapes() {
        this.shapes.clear();
    }


    //vrátí seznam všehc celých (ne tečkovaných..) čár ve tvarech
    public ArrayList<Line> getLines() {
        ArrayList<Line> lines = new ArrayList<>();

        for (LineGroup shape : this.shapes) {
            lines.addAll(shape.getLines());
        }

        return lines;
    }

    //vrátí seznam všehc tečkovaných čár ve tvarech
    public ArrayList<Line> getDottedLines() {
        ArrayList<Line> lines = new ArrayList<>();

        for (LineGroup shape : this.shapes) {
            lines.addAll(shape.getDottedLines());
        }

        return lines;
    }

    //najde tvar který obsahuje čáru
    public LineGroup findShapeByLine(Line line) {
        for (LineGroup shape : this.shapes) {
            if (shape.getLines().contains(line) || shape.getDottedLines().contains(line)) {
                return shape;
            }
        }
        return null;
    }


    //odstraní tvar z plátna
    public void removeShape(LineGroup shape) {
        this.shapes.remove(shape);
    }

    //najde tvar podle čáry a odstraní
    public void removeShapeFromLine(Line line) {
        removeShape(findShapeByLine(line));
    }

}