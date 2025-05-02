package models;

import java.util.ArrayList;

//tvar složený s více čár........
public class LineGroup {
    private ArrayList<Line> lines;
    private ArrayList<Line> dottedLines;


    //vytvoří prázdný tvar
    public LineGroup() {
        this.lines = new ArrayList<>();
        this.dottedLines = new ArrayList<>();
    }

    //přidá plnou čáru do tvaru
    public void addLine(Line line) {
        this.lines.add(line);
    }

    //přidá tečkovanou čáru do tvaru
    public void addDottedLine(Line line) {
        this.dottedLines.add(line);
    }


    //odstraní všechný čáry v tvaru
    public void clearLines() {
        this.lines.clear();
        this.dottedLines.clear();
    }

    //vráti seznam plných čár
    public ArrayList<Line> getLines() {
        return lines;
    }

    //vrátí seznam tečkovaných čár
    public ArrayList<Line> getDottedLines() {
        return dottedLines;
    }

    //odstraní konkrétní plnou čáru
    public void removeLine(Line line) {
        this.lines.remove(line);
    }

    //odstraní konkrétní tečkovanou čáru
    public void removeDottedLine(Line line) {
        this.dottedLines.remove(line);
    }

    //posune všechny zadané čáry podle dx a dy
    public void moveLines(int dx, int dy){
        for(Line line : this.lines){
            line.move(dx, dy);
        }
        for(Line line : this.dottedLines){
            line.move(dx, dy);
        }
    }

}
