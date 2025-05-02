package rasterizers;

import models.Line;
import rasters.Raster;

import java.awt.*;
import java.util.ArrayList;

public class TrivialLineRasterizer implements Rasterizer {
    private Raster raster;

    public TrivialLineRasterizer(Raster raster) {
        this.raster = raster;
    }

    @Override
    public void rasterize(Line line) {
        //získání souřadnich krajních bodů čáry
        int x1 = line.getPoint1().getX();
        int y1 = line.getPoint1().getY();
        int x2 = line.getPoint2().getX();
        int y2 = line.getPoint2().getY();

        //polovina tlouštky.... k hledání
        int halfThickness = line.getThickness() / 2;
        int unevenThickness = line.getThickness() % 2;


        //když svislá čára...
        if (x1 == x2) {
            //uspořádání aby šel směr od menšího k většímu
            if (y1 > y2) {
                int temp = y1;
                y1 = y2;
                y2 = temp;
            }

            //pro každý bod v okolí (počíta se k tomu i tlouštka)
            for (int y = y1; y <= y2; y++) {
                for (int tx=-halfThickness; tx<halfThickness+unevenThickness; tx++) {
                    for (int ty = -halfThickness; ty < halfThickness + unevenThickness; ty++) {
                        if (x1+tx >= 0 && x1+tx < raster.getWidth() && y + ty >= 0 && y + ty < raster.getHeight()) {
                            raster.setPixel(x1 + tx, y + ty, line.getColor().getRGB());
                        }
                    }
                }
            }
            return;
        }

        //výpočet posunu a směru
        float k = (float) (y2 - y1) / (x2 - x1);
        float q = y1 - (k * x1);

        //podle sklonu zjištuje kam má kreslit (x a y)
        if (Math.abs(k) < 1) {
            //směr zleva doprava
            if (x1 > x2) {
                int temp = x1;
                x1 = x2;
                x2 = temp;
            }
            for (int x = x1; x <= x2; x++) {
                int y = Math.round(k * x + q);
                for (int tx=-halfThickness; tx<halfThickness+unevenThickness; tx++) {
                    for (int ty = -halfThickness; ty < halfThickness + unevenThickness; ty++) {
                        if (x+tx >= 0 && x+tx < raster.getWidth() && y + ty >= 0 && y + ty < raster.getHeight()) {
                            raster.setPixel(x + tx, y + ty, line.getColor().getRGB());
                        }
                    }
                }
            }
        } else {
            //směr zdola nahoru
            if (y1 > y2) {
                int temp = y1;
                y1 = y2;
                y2 = temp;
            }
            for (int y = y1; y <= y2; y++) {
                int x = Math.round((y - q) / k);
                for (int tx=-halfThickness; tx<halfThickness+unevenThickness; tx++) {
                    for (int ty = -halfThickness; ty < halfThickness + unevenThickness; ty++) {
                        if (x + tx >= 0 && x + tx < raster.getWidth() && y + ty >= 0 && y + ty < raster.getHeight()) {
                            raster.setPixel(x + tx, y + ty, line.getColor().getRGB());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void rasterizeArray(ArrayList<Line> lines) {
        for (Line line : lines) {
            rasterize(line);
        }
    }
}