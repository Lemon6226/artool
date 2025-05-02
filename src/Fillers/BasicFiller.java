package Fillers;
import models.Point;
import rasters.Raster;

import java.awt.Color;
import java.util.Stack;

public class BasicFiller implements Filler {

    private Raster raster;

    //raster do kterého budeme kreslit..........
    public BasicFiller(Raster raster) {
        this.raster = raster;
    }

    @Override
    public void fill(Point start, Color fillColor) {
        // získá původní barvy pixel
        int targetColor = raster.getPixel(start.getX(), start.getY());
        int replacementColor = fillColor.getRGB();

        // jestli je stará barva stejná jako nová barva tak nic nemění (zbytečné)
        if (targetColor == replacementColor) return;

        Stack<Point> stack = new Stack<>();
        stack.push(start);

        // vyplňuje do všech 4 stran
        while (!stack.isEmpty()) {
            Point p = stack.pop();
            int x = p.getX();
            int y = p.getY();

            // kontroluje hranici plátna
            if (x < 0 || x >= raster.getWidth() || y < 0 || y >= raster.getHeight()) {
                continue;
            }

            // jestli barva pixelů je jiná než původní tka se toto přeskočí
            int currentColor = raster.getPixel(x, y);
            if (currentColor != targetColor) {
                continue;
            }

            // nastaví novou barvu pixelu :D
            raster.setPixel(x, y, replacementColor);

            // přidá sousední body
            stack.push(new Point(x + 1, y));
            stack.push(new Point(x - 1, y));
            stack.push(new Point(x, y + 1));
            stack.push(new Point(x, y - 1));
        }
    }

    // starý kód na výplň, už nepoužívám
    private void recursiveFill(int x, int y, Color fillColor, int baseColor) {
        if (x < 0 || x >= raster.getWidth() || y < 0 || y >= raster.getHeight()) {
            return;
        }

        int currentColor = raster.getPixel(x, y);
        if (currentColor != baseColor) {
            return;
        }

        raster.setPixel(x, y, fillColor.getRGB());

        recursiveFill(x + 1, y, fillColor, baseColor);//doprava
        recursiveFill(x - 1, y, fillColor, baseColor);//doleva
        recursiveFill(x, y + 1, fillColor, baseColor);//dolů
        recursiveFill(x, y - 1, fillColor, baseColor);//nahoru
    }
}