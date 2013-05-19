package gameoflife;

import java.awt.Canvas;
import java.awt.Graphics;

@SuppressWarnings("serial")
public class GameOfLifeCanvas extends Canvas {

    final GameOfLife gameOfLife;

    final static int SIZE = 400;
    Graphics g;

    int magnification = 1;

    public GameOfLifeCanvas(GameOfLife gameOfLife) {
        this.gameOfLife = gameOfLife;
        setBackground(java.awt.Color.WHITE);
    }

    public void paint() {
        if(g == null) {
            g = getGraphics();
        }
        g.clearRect(0, 0, SIZE, SIZE);

        int x0 = (SIZE / 2 + gameOfLife.topleft.x * magnification),
            y0 = (SIZE / 2 + gameOfLife.topleft.y * magnification);

        GameOfLife.Cell rowHeader = gameOfLife.topleft;
        while(x0 < 0) {
            rowHeader = rowHeader.e;
            x0 += magnification;
        }
        GameOfLife.Cell current = rowHeader;
        while(y0 < 0) {
            current = current.s;
            y0 += magnification;
        }

        for(int delta_y = 0; rowHeader != null && y0 + delta_y < SIZE; delta_y += magnification) {
            for(int delta_x = 0; current != null && x0 + delta_x < SIZE; delta_x += magnification) {
                if(current.live) {
                    drawPixel(x0 + delta_x, y0 + delta_y, magnification);
                }
                current = current.e;
            }
            rowHeader = rowHeader.s;
            current = rowHeader;
        }
    }

    @SuppressWarnings("unused")
    private void drawPixel(int x, int y) {
        drawPixel(x, y, 1);
    }

    private void drawPixel(int x, int y, int m) {
        g.fillRect(x, y, m, m);
    }
}
