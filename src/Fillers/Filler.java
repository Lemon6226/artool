package Fillers;

import models.Point;
import java.awt.*;

public interface Filler {

    public void fill(
            Point click,
            Color fillColor
    );
}
