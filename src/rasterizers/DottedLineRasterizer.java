package rasterizers;

import models.Line;
import rasters.Raster;

import java.util.ArrayList;

public class DottedLineRasterizer implements Rasterizer {
    private Raster raster;
    private int dotLength = 1; //tečka
    private int gapLength = 5; //mezera

    //rasterizer do ktereho se bude kreslit
    public DottedLineRasterizer(Raster raster) {
        this.raster = raster;
    }

    @Override
    public void rasterize(Line line) {
        if (!line.isDotted()) {
            return; //pokud čára není tečkovaná tak jí nakreslí
        }

        //získání souřadnic koncových bodů čáry
        int x1 = line.getPoint1().getX();
        int y1 = line.getPoint1().getY();
        int x2 = line.getPoint2().getX();
        int y2 = line.getPoint2().getY();

        //výpočet posunu a směrnice
        float k = (float) (y2 - y1) / (x2 - x1);
        float q = y1 - (k * x1);

        //jestli je čára více vodorovná tak kreslí odleva doprava
        if (Math.abs(k) < 1) {
            if (x1 > x2) {
                int temp = x1;
                x1 = x2;
                x2 = temp;
            }

            int count = 0;
            for (int x = x1; x <= x2; x++) {
                int y = Math.round(k * x + q);
                //vykreslí body podle zadaných hodnot mezery a tečky (gap, dot)
                if (count % (dotLength + gapLength) < dotLength) {
                    raster.setPixel(x, y, line.getColor().getRGB());
                }
                count++;
            }
        } else {
            //jestli je čára více svyslá tak kreslí oshora dolů
            if (y1 > y2) {
                int temp = y1;
                y1 = y2;
                y2 = temp;
            }

            int count = 0;
            for (int y = y1; y <= y2; y++) {
                int x = Math.round((y - q) / k);
                if (count % (dotLength + gapLength) < dotLength) {
                    raster.setPixel(x, y, line.getColor().getRGB());
                }
                count++;
            }
        }
    }

    //rasterizace více čár
    @Override
    public void rasterizeArray(ArrayList<Line> lines) {
        for (Line line : lines) {
            rasterize(line);
        }
    }
}