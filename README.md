**Popis**

Aplikace zaznamenává pozice kliknutí myší na kreslícím plátně a podle vybraného nástroje vykresluje čáry nebo geometrické tvary, které počítá z těchto bodů. Uživatel přepíná nástroje pomocí tlačítek v horní liště (hotbar). :D


**Tlačítka + funkce**

Line – po dvou kliknutích nakreslí přímou čáru.

Rectangle – na základě dvou rohů vykreslí obdélník.

Square – podobně jako obdélník, ale přizpůsobí rozměry tak, aby vznikl čtverec.

Circle – uživatel vybere střed a bod na kružnici, aplikace dopočítá a vykreslí kruh složený z mnoha čar.

Polygon – uživatel přidává body postupnými kliky, mezi nimi se kreslí dočasné čáry -> polygon se uzavře po stisknutí Enter.

Edit – umožňuje upravovat existující body tvarů pravým klikem, přesouvat body nebo celé tvary vlevo myši.

Eraser – najde nejbližší čáru k pozici kliknutí a odstraní ji.

Fill – vyplní barevně oblast na základě zvoleného bodu kliknutí.

Color – otevře dialog pro výběr barvy použitelný pro kreslení i vyplňování.

Thickness – umožňuje nastavit tloušťku čar přes spinner nahoře.

Clear (Delete klávesa) – vymaže všechno na plátně i ve vnitřních seznamech.

*Přidržení Shift upravuje kreslení čar a tvarů tak, aby byly vodorovné, svislé nebo pod úhlem 45°.*

*Ctrl modifikuje čáry na čárkované.*

*Kreslení tlustších čar není explicitní, ale tloušťka lze nastavovat přes spinner.*


**Důležité metody**

clearEditState() – vyčistí všechny přechodné režimy kreslení a úprav.

createAdapters() – nastaví zpracování vstupů z myši (klik, tažení) i kláves (Control, Shift, Delete, Enter).

adjustForShift(Point) – upraví bod pro kreslení pod specifické úhly, když je Shift aktivní.

searchClosestPoint(int x, int y) – vyhledá bod blízko k zadané pozici, pro upravování.

searchClosestLineNearPoint(Point, int) – najde nejbližší čáru k danému bodu s tolerancí.

drawRectangle(Point p1, Point p2), drawSquare(Point p1, Point p2), drawCircleAsPolyline(Point center, Point edge), drawPolygon() – metody pro kreslení jednotlivých geometrických tvarů.

updateCanvasAndRepaint() – obnoví plátno překreslením všech aktuálních čar a tvarů.

getConnectedShapeLines(Line startLine) a getShapePoints(Set<Line>) – umožňují pracovat se sadami čar jako s celými tvary při úpravách a přesunech.


**Důležité třídy**

TrivialLineRasterizer – vykresluje plné čáry na raster podle koncových bodů a tloušťky.

DottedLineRasterizer – vykresluje čárkované (tečkované) čáry na základě vzoru tečka-mezera.

LineGroup – uchovává skupinu čar (plné i tečkované), umožňuje přidávat, mazat a posouvat čáry jako celek.

LineCanvas – spravuje všechny tvary složené z čar, poskytuje seznamy čar a operace pro správu tvarů.

BasicFiller – vyplňuje uzavřené oblasti barvou algoritmem zásobníkového zaplňování pixelů.


