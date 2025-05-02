import models.Line;
import models.LineCanvas;
import models.LineGroup;
import models.Point;
import rasterizers.DottedLineRasterizer;
import rasterizers.Rasterizer;
import rasterizers.TrivialLineRasterizer;
import rasters.Raster;
import rasters.RasterBufferedImage;
import Fillers.BasicFiller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class App {
    // nástroje pro kreslení a úpravy
    enum Tool {
        LINE, RECTANGLE, SQUARE, CIRCLE, POLYGON, EDIT, ERASER, FILL
    }

    private final JPanel panel;
    private final Raster raster;
    private MouseAdapter mouseAdapter;
    private KeyAdapter keyAdapter;
    private Point pressPoint;
    private Point dragStartPoint;
    private Rasterizer rasterizer;
    private Rasterizer dottedRasterizer;
    private LineCanvas canvas;
    private BasicFiller filler;
    private boolean ctrlMode = false;
    private boolean shiftMode = false;
    private boolean editingPoint = false;
    private boolean movingShape = false;
    private boolean reshapingShape = false;
    private Line selectedLine;
    private Line originalLine;
    private ArrayList<Point> polygonPoints;
    private Tool selectedTool = Tool.LINE;
    private JSpinner thicknessSpinner;
    Color currentColor = Color.BLUE;

    public static void main(String[] args) {
        // vytvoří okno
        SwingUtilities.invokeLater(() -> new App(800, 600).start());
    }

    // vyčistí plátno podle barvy
    public void clear(int color) {
        raster.setClearColor(color);
        raster.clear();
    }

    // překreslení plátna
    public void present(Graphics graphics) {
        raster.repaint(graphics);
    }

    // nachistá kreslící plochy a fillery

    public void start() {
        clear(0xaaaaaa);
        panel.repaint();
        polygonPoints = new ArrayList<>();
        filler = new BasicFiller(raster);
    }

    public App(int width, int height) {
        // vykonstroluje plochu a fillery
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setTitle("Drawing Application");
        frame.setResizable(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        raster = new RasterBufferedImage(width, height);
        panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                present(g);
            }
        };
        panel.setPreferredSize(new Dimension(width, height));

        rasterizer = new TrivialLineRasterizer(raster);
        dottedRasterizer = new DottedLineRasterizer(raster);
        // plátno které uloží všechny nakreslené tvary jako čáry
        canvas = new LineCanvas();

        polygonPoints = new ArrayList<>();

        // aby jsme mohli používat myš a klávesnici
        createAdapters();
        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);
        panel.addKeyListener(keyAdapter);
        panel.setFocusable(true);
        panel.requestFocusInWindow();

        // panel s tlačítkami
        JPanel buttonPanel = new JPanel();

        JLabel thicknessLabel = new JLabel("Thickness:");
        thicknessSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        buttonPanel.add(thicknessLabel);
        buttonPanel.add(thicknessSpinner);

        // tlačítko pro výběr barvy
        JButton colorButton = new JButton("Color");
        colorButton.setBackground(currentColor);
        colorButton.setForeground(Color.WHITE);

        colorButton.addActionListener(e -> {
            // po kliknutí se otevře okno s barvama
            Color chosenColor = JColorChooser.showDialog(null, "Choose Drawing Color", currentColor);
            if (chosenColor != null) {
                currentColor = chosenColor;
                colorButton.setBackground(currentColor);
            }
        });

        buttonPanel.add(colorButton);

        // vytvoření názvů a přidání s nástrojema pro tlačítka
        String[] toolNames = {"Line", "Rectangle", "Square", "Circle", "Polygon", "Edit", "Eraser", "Fill"};
        Tool[] tools = {Tool.LINE, Tool.RECTANGLE, Tool.SQUARE, Tool.CIRCLE, Tool.POLYGON, Tool.EDIT, Tool.ERASER, Tool.FILL};

        //vytvoření tlačítek nástrojů, jestli klikneme nastaví nástroj a vyčistí stav
        for (int i = 0; i < toolNames.length; i++) {
            JButton button = new JButton(toolNames[i]);
            Tool tool = tools[i];
            button.addActionListener(e -> {
                selectedTool = tool;
                polygonPoints.clear();
                clearEditState(); // Resetuje stav úpravy
                panel.requestFocusInWindow();
                panel.repaint();
            });
            buttonPanel.add(button);
        }

        // přidání panelu s tlačítky a kreslícího panelu do okna
        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    // vyčistí stav
    private void clearEditState() {
        editingPoint = false;
        movingShape = false;
        reshapingShape = false;
        selectedLine = null;
        originalLine = null;
        pressPoint = null;
        dragStartPoint = null;
    }

    // jestli klikneme s miší atd....
    private void createAdapters() {
        mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point mousePt = new Point(e.getX(), e.getY());

                // tlačítko úpravy
                if (selectedTool == Tool.EDIT) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        // pravý klik - > zařne hledat blízký bod
                        Point nearPoint = searchClosestPoint(e.getX(), e.getY());
                        if (nearPoint != null) {
                            //aktivuje úpravu bodu v tvaru
                            editingPoint = true;
                            pressPoint = nearPoint;
                            reshapingShape = true;
                            dragStartPoint = nearPoint;
                            selectedLine = findLineContainingPoint(nearPoint);
                            return;
                        }
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        // levý klik -> hledá blízkou linku pro přesun celého tvaru
                        selectedLine = searchClosestLineNearPoint(mousePt, 6);
                        if (selectedLine != null) {
                            movingShape = true;
                            dragStartPoint = mousePt;
                            // Uloží čáry pro vrácení nějaké změny
                            originalLine = new Line(
                                    selectedLine.getPoint1(), selectedLine.getPoint2(),
                                    selectedLine.getColor(), selectedLine.isDotted(),
                                    selectedLine.getThickness());
                            return;
                        }
                    }
                    clearEditState(); //pokud nejde upravit tak se resetuje stav
                    return;
                }

                // tlačítko gumy
                if (selectedTool == Tool.ERASER) {
                    //najde nejbližší čáru a odstraní jí
                    Line lineToErase = searchClosestLineNearPoint(mousePt, 6);
                    if (lineToErase != null) {
                        canvas.removeShapeFromLine(lineToErase);
                        updateCanvasAndRepaint();
                    }
                    return;
                }

                //tlačítko vyplnění
                if (selectedTool == Tool.FILL) {
                    //vyplní oblast podle vybrané barvy
                    filler.fill(mousePt, currentColor);

                    //překkreslí všechný čáry
                    rasterizer.rasterizeArray(canvas.getLines());
                    dottedRasterizer.rasterizeArray(canvas.getDottedLines());

                    panel.repaint();
                    return;
                }

                //pravý klik když needitujem -> snaží se najít blízký bod pro úpravu
                if (SwingUtilities.isRightMouseButton(e)) {
                    Point closePoint = searchClosestPoint(e.getX(), e.getY());
                    if (closePoint != null) {
                        editingPoint = true;
                        selectedLine = findLineContainingPoint(closePoint);
                        if (selectedLine != null) {
                            originalLine = new Line(
                                    selectedLine.getPoint1(), selectedLine.getPoint2(),
                                    selectedLine.getColor(), selectedLine.isDotted(),
                                    selectedLine.getThickness());
                        } else {
                            editingPoint = false;
                            originalLine = null;
                        }
                        pressPoint = closePoint;
                    }
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    // kliknutí -> začne kreslit nebo přidá bod do polygonu
                    pressPoint = new Point(e.getX(), e.getY());
                    if (selectedTool == Tool.POLYGON) {
                        // přidá bod a vykreslí polygon...
                        polygonPoints.add(pressPoint);
                        drawTempPolygon();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //uvolnění myši když editujem
                if (selectedTool == Tool.EDIT) {
                    if (editingPoint || movingShape || reshapingShape) {
                        clearEditState();
                        updateCanvasAndRepaint();
                    }
                    return;
                }

                // jestli kreslíme jiný tvar než polygon
                if (pressPoint != null && selectedTool != Tool.POLYGON) {
                    Point releasePoint = new Point(e.getX(), e.getY());

                    // udržení proporcí při stisknutí shift (pro některé tvary)
                    if (selectedTool == Tool.RECTANGLE || selectedTool == Tool.SQUARE || selectedTool == Tool.LINE) {
                        releasePoint = adjustForShift(releasePoint);
                    }

                    // pokud upravujeme bod ukončíme úpravu a aktualizujeme
                    if (editingPoint && selectedLine != null) {
                        editingPoint = false;
                        originalLine = null;
                        updateCanvasAndRepaint();
                        return;
                    }

                    // vytvoření tvaru podle vybraného nástroje
                    switch (selectedTool) {
                        case LINE -> {
                            int thickness = (int) thicknessSpinner.getValue();
                            Line newLine = new Line(pressPoint, releasePoint, currentColor, ctrlMode, thickness);
                            LineGroup shape = new LineGroup();
                            // pokud je stisknutý ctrl, čárů udělá tečkovanou
                            if (ctrlMode) {
                                shape.addDottedLine(newLine);
                            } else {
                                shape.addLine(newLine);
                            }
                            canvas.addShape(shape);
                        }
                        case RECTANGLE -> drawRectangle(pressPoint, releasePoint);
                        case SQUARE -> drawSquare(pressPoint, releasePoint);
                        case CIRCLE -> drawCircleAsPolyline(pressPoint, releasePoint);
                        case POLYGON -> {
                            // polygon se dokončí po stisknutí enter
                        }
                    }
                    updateCanvasAndRepaint();
                }
                // reset stavů pro upravů s myší
                editingPoint = false;
                originalLine = null;
                pressPoint = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point mousePt = new Point(e.getX(), e.getY());

                // spracování přesunu bodů/tvarů když editujem
                if (selectedTool == Tool.EDIT) {
                    if (reshapingShape && selectedLine != null && pressPoint != null) {
                        // změna velikosti tvaru
                        Set<Line> shapeLines = getConnectedShapeLines(selectedLine);
                        Set<Point> shapePoints = getShapePoints(shapeLines);
                        Point opposite = findOppositePoint(pressPoint, shapeLines);
                        if (opposite == null) {
                            if (selectedLine.getPoint1().equals(pressPoint)) {
                                selectedLine.setPoint1(mousePt);
                            } else if (selectedLine.getPoint2().equals(pressPoint)) {
                                selectedLine.setPoint2(mousePt);
                            }
                            updateCanvasAndRepaint();
                            return;
                        }

                        // yýpočet poměrů X a Y
                        double oldDx = pressPoint.getX() - opposite.getX();
                        double oldDy = pressPoint.getY() - opposite.getY();
                        double newDx = mousePt.getX() - opposite.getX();
                        double newDy = mousePt.getY() - opposite.getY();
                        if (oldDx == 0) oldDx = 1;
                        if (oldDy == 0) oldDy = 1;

                        double scaleX = newDx / oldDx;
                        double scaleY = newDy / oldDy;

                        // posun všech bodů podle výpočtu
                        for (Point p : shapePoints) {
                            int newX = (int) Math.round(opposite.getX() + (p.getX() - opposite.getX()) * scaleX);
                            int newY = (int) Math.round(opposite.getY() + (p.getY() - opposite.getY()) * scaleY);
                            p.setX(newX);
                            p.setY(newY);
                        }
                        updateCanvasAndRepaint();
                        return;
                    }
                    if (movingShape && selectedLine != null && dragStartPoint != null) {
                        // posun tvaru podle myši
                        int dx = e.getX() - dragStartPoint.getX();
                        int dy = e.getY() - dragStartPoint.getY();
                        LineGroup shape = canvas.findShapeByLine(selectedLine);
                        shape.moveLines(dx, dy);
                        dragStartPoint = mousePt;
                        updateCanvasAndRepaint();
                        return;
                    }
                    return;
                }

                // jestli upravujeme bod čáry(bez editoavání)
                if (editingPoint && selectedLine != null && pressPoint != null) {
                    if (selectedLine.getPoint1().equals(pressPoint)) {
                        selectedLine.setPoint1(mousePt);
                    } else if (selectedLine.getPoint2().equals(pressPoint)) {
                        selectedLine.setPoint2(mousePt);
                    }
                    updateCanvasAndRepaint();
                }
            }
        };

        keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                //kontroluje jestli jsme nestistkli speciální klávesnice
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) ctrlMode = true;
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) shiftMode = true;
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    //vymazání plátna
                    canvas.clearShapes();
                    polygonPoints.clear();
                    raster.clear();
                    panel.repaint();
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER && selectedTool == Tool.POLYGON) {
                    // dokončí polygon jestli klikneme enter
                    drawPolygon();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // kontroluje jestli jsme přestali držet control neo shift
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) ctrlMode = false;
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) shiftMode = false;
            }

        };
    }

    // nekreslení obdélníku ze dvou rohových bodů
    private void drawRectangle(Point p1, Point p2) {
        Point p3 = new Point(p2.getX(), p1.getY());
        Point p4 = new Point(p1.getX(), p2.getY());

        int thickness = (int) thicknessSpinner.getValue();
        LineGroup shape = new LineGroup();
        // přidá 4 čáry na obdélník
        shape.addLine(new Line(p1, p3, currentColor, false, thickness));
        shape.addLine(new Line(p3, p2, currentColor, false, thickness));
        shape.addLine(new Line(p2, p4, currentColor, false, thickness));
        shape.addLine(new Line(p4, p1, currentColor, false, thickness));
        canvas.addShape(shape);
    }

    // nakreslení čtverce
    private void drawSquare(Point p1, Point p2) {
        // strana čtverce je nejmenší délka x a y
        int size = Math.min(Math.abs(p2.getX() - p1.getX()), Math.abs(p2.getY() - p1.getY()));
        // určí směr od p1 k p2 (kladný a záporný)
        int dx = p2.getX() < p1.getX() ? -size : size;
        int dy = p2.getY() < p1.getY() ? -size : size;

        Point p2Corner = new Point(p1.getX() + dx, p1.getY());
        Point p3Corner = new Point(p1.getX() + dx, p1.getY() + dy);
        Point p4Corner = new Point(p1.getX(), p1.getY() + dy);

        int thickness = (int) thicknessSpinner.getValue();
        LineGroup shape = new LineGroup();
        //přidá 4 čáry na čtverec
        shape.addLine(new Line(p1, p2Corner, currentColor, false, thickness));
        shape.addLine(new Line(p2Corner, p3Corner, currentColor, false, thickness));
        shape.addLine(new Line(p3Corner, p4Corner, currentColor, false, thickness));
        shape.addLine(new Line(p4Corner, p1, currentColor, false, thickness));
        canvas.addShape(shape);
    }

    // nakreslení kruhu který je složený s hodně hodně čár
    private void drawCircleAsPolyline(Point center, Point edge) {
        int cx = center.getX();
        int cy = center.getY();
        int dx = edge.getX() - cx;
        int dy = edge.getY() - cy;
        int radius = (int) Math.round(Math.sqrt(dx * dx + dy * dy));
        if (radius <= 0) return;

        int segments = 100; // počet čár na kruh
        double angleStep = 2 * Math.PI / segments;

        int thickness = (int) thicknessSpinner.getValue();
        ArrayList<Point> circlePoints = new ArrayList<>();
        LineGroup shape = new LineGroup();

        // vypočítá body kruhu na úhlech
        for (int i = 0; i <= segments; i++) {
            double angle = i * angleStep;
            int x = (int) Math.round(cx + radius * Math.cos(angle));
            int y = (int) Math.round(cy + radius * Math.sin(angle));
            circlePoints.add(new Point(x, y));
        }

        //spojí body
        for (int i = 0; i < circlePoints.size() - 1; i++) {
            Point p1 = circlePoints.get(i);
            Point p2 = circlePoints.get(i + 1);
            shape.addLine(new Line(p1, p2, currentColor, false, thickness));
        }
        canvas.addShape(shape);
    }

    //dokončení polygon tím že spojí všechny body
    private void drawPolygon() {
        if (polygonPoints.size() < 3) return; // polygon musí mít aspoň 3 body
        int thickness = (int) thicknessSpinner.getValue();
        LineGroup shape = new LineGroup();
        // spojení všech bodů
        for (int i = 0; i < polygonPoints.size() - 1; i++) {
            Point p1 = polygonPoints.get(i);
            Point p2 = polygonPoints.get(i + 1);
            shape.addLine(new Line(p1, p2, currentColor, false, thickness));
        }
        Point first = polygonPoints.get(0);
        Point last = polygonPoints.get(polygonPoints.size() - 1);
        // uzavření polygonu čárou
        shape.addLine(new Line(last, first, currentColor, false, thickness));
        canvas.addShape(shape);

        polygonPoints.clear(); //vyčistí staré body
        updateCanvasAndRepaint();
    }

    // nakreslení "dočasného" polygonu když přidáváme body
    private void drawTempPolygon() {
        raster.clear();
        rasterizer.rasterizeArray(canvas.getLines());
        dottedRasterizer.rasterizeArray(canvas.getDottedLines());

        int thickness = (int) thicknessSpinner.getValue();
        // nakreslí body aktuálního polygonu
        for (int i = 0; i < polygonPoints.size() - 1; i++) {
            new TrivialLineRasterizer(raster).rasterize(
                    new Line(polygonPoints.get(i), polygonPoints.get(i + 1), currentColor, false, thickness));
        }
        panel.repaint();
    }

    // jestli držime shift, můžeme jenom kreslit vodorovnou a vertikální čáru
    private Point adjustForShift(Point currentPoint) {
        if (!shiftMode || pressPoint == null) return currentPoint;
        int dx = Math.abs(currentPoint.getX() - pressPoint.getX());
        int dy = Math.abs(currentPoint.getY() - pressPoint.getY());
        int tolerance = 8;
        if (dx > dy + tolerance) return new Point(currentPoint.getX(), pressPoint.getY());
        else if (dy > dx + tolerance) return new Point(pressPoint.getX(), currentPoint.getY());
        else {
            return new Point(pressPoint.getX() + (currentPoint.getX() - pressPoint.getX()),
                    pressPoint.getY() + (currentPoint.getY() - pressPoint.getY()));
        }
    }

    // vyhledá nejbližší bod od myši
    private Point searchClosestPoint(int mouseX, int mouseY) {
        int closeNumber = 10; // maximální vzdálenost pro hledání blízkého bodu
        ArrayList<Line> lines = canvas.getLines();
        Point closestPoint = null;
        double closestDistance = Double.MAX_VALUE;

        for (Line line : lines) {
            Point p1 = line.getPoint1();
            Point p2 = line.getPoint2();
            double distanceToP1 = euclideanDistance(mouseX, mouseY, p1.getX(), p1.getY());
            double distanceToP2 = euclideanDistance(mouseX, mouseY, p2.getX(), p2.getY());
            if (distanceToP1 < closeNumber && distanceToP1 < closestDistance) {
                closestDistance = distanceToP1;
                closestPoint = p1;
            }
            if (distanceToP2 < closeNumber && distanceToP2 < closestDistance) {
                closestDistance = distanceToP2;
                closestPoint = p2;
            }
        }
        return closestPoint;
    }

    // vyhledá nejbližší čáru blízko zadaného bodu
    private Line searchClosestLineNearPoint(Point mousePt, int toleranceDistance) {
        for (Line line : canvas.getLines()) {
            if (isPointNearLineSegment(line.getPoint1(), line.getPoint2(), mousePt, toleranceDistance)) {
                return line;
            }
        }
        return null;
    }

    // kontroluje jestli je blízko čáry
    private boolean isPointNearLineSegment(Point a, Point b, Point p, double tolerance) {
        double distAB = euclideanDistance(a.getX(), a.getY(), b.getX(), b.getY());
        if (distAB == 0) return euclideanDistance(p.getX(), p.getY(), a.getX(), a.getY()) <= tolerance;

        // projekce bodu p na čáru ab
        double t = ((p.getX() - a.getX()) * (b.getX() - a.getX()) + (p.getY() - a.getY()) * (b.getY() - a.getY())) / (distAB * distAB);
        if (t < 0) t = 0;
        else if (t > 1) t = 1;
        double projX = a.getX() + t * (b.getX() - a.getX());
        double projY = a.getY() + t * (b.getY() - a.getY());

        // vzdálenost od projekce k bodu p
        double distToSegment = euclideanDistance(projX, projY, p.getX(), p.getY());
        return distToSegment <= tolerance;
    }

    //výpočet Eukleidovské vzdálenosti mezi dvěma body
    private double euclideanDistance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // najde čáru která obsahuje bod jako jeden ze svých konečných bodů
    private Line findLineContainingPoint(Point point) {
        for (Line line : canvas.getLines()) {
            if (line.getPoint1().equals(point) || line.getPoint2().equals(point)) return line;
        }
        return null;
    }

    // vyčistí plátno
    private void updateCanvasAndRepaint() {
        raster.clear();
        rasterizer.rasterizeArray(canvas.getLines());
        dottedRasterizer.rasterizeArray(canvas.getDottedLines());
        panel.repaint();
    }

    // najde všechny čáry kde tvar obsahuje startLine (polygon..)
    private Set<Line> getConnectedShapeLines(Line startLine) {
        Set<Line> connected = new HashSet<>();
        connected.add(startLine);

        boolean added;
        do {
            added = false;
            for (Line line : canvas.getLines()) {
                if (connected.contains(line)) continue;
                for (Line connectedLine : new ArrayList<>(connected)) {
                    if (linesSharePoint(line, connectedLine)) {
                        connected.add(line);
                        added = true;
                        break;
                    }
                }
            }
        } while (added);

        return connected;
    }

    // kontroluje jestli čáry nesdílý body (aby se mohli přesunovat celý tvary.. ne jenom čáry z nich)
    private boolean linesSharePoint(Line l1, Line l2) {
        return l1.getPoint1().equals(l2.getPoint1()) ||
                l1.getPoint1().equals(l2.getPoint2()) ||
                l1.getPoint2().equals(l2.getPoint1()) ||
                l1.getPoint2().equals(l2.getPoint2());
    }

    // vrací set všech bodů ve tvaru
    private Set<Point> getShapePoints(Set<Line> lines) {
        Set<Point> points = new HashSet<>();
        for (Line line : lines) {
            points.add(line.getPoint1());
            points.add(line.getPoint2());
        }
        return points;
    }

    //najde bod který je nejvíc od bodu(pro kontrolu)
    private Point findOppositePoint(Point point, Set<Line> shapeLines) {
        Set<Point> points = getShapePoints(shapeLines);
        Point opposite = null;
        double maxDist = -1;
        for (Point p : points) {
            if (p.equals(point)) continue;
            double dist = euclideanDistance(point.getX(), point.getY(), p.getX(), p.getY());
            if (dist > maxDist) {
                maxDist = dist;
                opposite = p;
            }
        }
        return opposite;
    }
}
