package models;

import java.awt.*;

public class Line {
    private Point point1;
    private Point point2;
    private Color color;
    private boolean isDotted;
    private int thickness;

    public Line(Point point1, Point point2, Color color, boolean isDotted, int thickness) {
        this.point1 = point1;
        this.point2 = point2;
        this.color = color;
        this.isDotted = isDotted;
        this.thickness = thickness;
    }

    public Point getPoint1() {
        return point1;
    }

    public Point getPoint2() {
        return point2;
    }

    public Color getColor() {
        return color;
    }

    public boolean isDotted() {
        return isDotted;
    }

    public int getThickness() {return thickness;}

    public void setPoint1(Point point1) {
        this.point1 = point1;
    }

    public void setPoint2(Point point2) {
        this.point2 = point2;
    }

    public void alignLine(int alignmentType) {
        if (alignmentType == 1) {
            point2.setY(point1.getY());
        } else if (alignmentType == 2) {
            point2.setX(point1.getX());
        } else if (alignmentType == 3) {
            int dx = point2.getX() - point1.getX();
            int dy= point2.getY() - point1.getY();
            int length = Math.max(Math.abs(dx), Math.abs(dy));
            point2.setX(point1.getX() + length *(dx < 0 ? -1 : 1));
            point2.setY(point1.getY() + length * (dy < 0 ? -1 : 1));
        }
    }

    public void move(int dx, int dy){
        int x1 = point1.getX() + dx;
        int y1 = point1.getY() + dy;
        int x2 = point2.getX() + dx;
        int y2 = point2.getY() + dy;

        point1 = new Point(x1, y1);
        point2 = new Point(x2, y2);
    }
}