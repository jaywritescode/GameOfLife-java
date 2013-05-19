package gameoflife;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.regex.*;

/**
 * Conway's Game of Life
 *
 * @author jay
 * @version 0.2
 */
public class GameOfLife {

    int rows, columns;
    Cell topleft, bottomright;

    Rules rules;

    int iterations = 0;

    /**
     * A cell in the game of life grid.
     */
    class Cell {
        boolean live;
        Cell n, ne, e, se, s, sw, w, nw;

        final int x, y;                         // these vars will help us when it comes to drawing this

        private Cell(boolean live, int x, int y) {
            this.live = live;
            this.x = x;
            this.y = y;
        }

        public int countLiveNeighbors() {
            int count = 0;
            for(Cell neighbor : new Cell[] {n, ne, e, se, s, sw, w, nw}) {
                if(neighbor != null && neighbor.live) {
                    ++count;
                }
            }
            return count;
        }

        @Override public String toString() {
            return String.format("Cell [%s,%s]: %", x, y, live ? "live" : "dead");
        }
    }

    /**
     * Game of life rules.
     */
    class Rules {
        boolean[] born = new boolean[9], survives = new boolean[9];
                                    // In rule r, a cell with k live neighbors is born iff r.born[k].
                                    // Likewise, a cell with k live neighbors survives iff r.survives[k]

        /**
         * Constructor.
         *
         * @param rule the rule
         */
        public Rules(String rule) {
            Matcher m = Pattern.compile("^(B(\\d*)/S(\\d*))|((\\d*)/(\\d*))$", Pattern.CASE_INSENSITIVE).matcher(rule);
            if(!m.matches()) {
                throw new IllegalArgumentException("Invalid rule.");
            }

            if(m.group(2) != null && m.group(3) != null) {
                for(char ch : m.group(2).toCharArray()) {
                    born[Character.digit(ch, 10)] = true;
                }
                for(char ch : m.group(3).toCharArray()) {
                    survives[Character.digit(ch, 10)] = true;
                }
            }
            else if(m.group(5) != null && m.group(6) != null) {
                for(char ch : m.group(5).toCharArray()) {
                    survives[Character.digit(ch, 10)] = true;
                }
                for(char ch : m.group(6).toCharArray()) {
                    born[Character.digit(ch, 10)] = true;
                }
            }
        }

        /**
         * Applies the set of rules to the given cell.
         *
         * @param cell the cell
         * @return true if the cell should be alive as per the rules, otherwise false
         */
        public boolean apply(Cell cell) {
            if(cell.live) {
                return survives[cell.countLiveNeighbors()];
            }
            else {
                return born[cell.countLiveNeighbors()];
            }
        }

        /**
         * Gets the minimum number of neighbor cells to turn a dead cell live.
         *
         * @return the minimum number of neighbor cells to turn a dead cell live
         */
        public int getBornMinimum() {
            for(int i = 0; i < born.length; ++i) {
                if(born[i]) {
                    return i;
                }
            }
            return -1;
        }

        @Override public String toString() {
            StringBuilder bornSB = new StringBuilder("B"),
                          survivesSB = new StringBuilder("S");
            for(int i = 0; i < 9; ++i) {
                if(born[i]) {
                    bornSB.append(i);
                }
                if(survives[i]) {
                    survivesSB.append(i);
                }
            }
            return bornSB.append("/").append(survivesSB).toString();
        }
    }

    /**
     * Constructor.
     *
     * @param seed the initial pattern
     * @param ruleString the rule
     */
    public GameOfLife(boolean[][] seed, String ruleString) {
        Cell curr = null, last = topleft;
        rows = seed.length;
        columns = seed[0].length;

        for(int row = 0; row < seed.length; ++row) {
            for(int col = 0; col < seed[0].length; ++col) {
                curr = new Cell(seed[row][col], col - (columns / 2), row - (rows / 2));

                if(topleft == null) {
                    topleft = curr;
                }
                else if(row == 0) {
                    curr.w = last;
                    curr.w.e = curr;
                }
                else if(col == 0) {
                    curr.n = last;
                    curr.n.s = curr;
                    curr.ne = curr.n.e;
                    if(curr.ne != null) {
                        curr.ne.sw = curr;
                    }
                }
                else {
                    curr.w = last;
                    curr.w.e = curr;
                    curr.nw = curr.w.n;
                    curr.nw.se = curr;
                    curr.n = curr.nw.e;
                    curr.n.s = curr;
                    curr.ne = curr.n.e;
                    if(curr.ne != null) {
                        curr.ne.sw = curr;
                    }
                }

                last = curr;
            }
            bottomright = last;
            while(last.w != null) {
                last = last.w;
            }
        }
        this.rules = new Rules(ruleString);
    }

    /**
     * Constructor with Conway's original B3/S23 rule.
     *
     * @param seed the initial pattern
     */
    public GameOfLife(boolean[][] seed) {
        this(seed, "B3/S23");
    }

    /**
     * Adds a row of off cells to the top of the grid.
     */
    private void addTopRow() {
        Cell curr, i = topleft;
        while(i != null) {
            i.n = (curr = new Cell(false, i.x, i.y - 1));
            curr.s = i;
            curr.sw = curr.s.w;
            if(curr.sw != null) {
                curr.sw.ne = curr;
            }
            curr.w = curr.s.nw;
            if(curr.w != null) {
                curr.w.e = curr;
            }
            curr.nw = curr.n = curr.ne = curr.e = null;
            curr.se = curr.s.e;
            if(curr.se != null) {
                curr.se.nw = curr;
            }

            i = i.e;
        }
        ++rows;
        topleft = topleft.n;
    }

    /**
     * Adds a row of cells to the bottom of the grid.
     */
    private void addBottomRow() {
        Cell curr, i = bottomright;
        while(i != null) {
            i.s = (curr = new Cell(false, i.x, i.y + 1));
            curr.n = i;
            curr.ne = curr.n.e;
            if(curr.ne != null) {
                curr.ne.sw = curr;
            }
            curr.e = curr.n.se;
            if(curr.e != null) {
                curr.e.w = curr;
            }
            curr.se = curr.s = curr.sw = curr.w = null;
            curr.nw = curr.n.w;
            if(curr.nw != null) {
                curr.nw.se = curr;
            }

            i = i.w;
        }
        ++rows;
        bottomright = bottomright.s;
    }

    /**
     * Adds a column of cells to the left of the grid.
     */
    private void addLeftColumn() {
        Cell curr, j = topleft;
        while(j != null) {
            j.w = (curr = new Cell(false, j.x - 1, j.y));
            curr.e = j;
            curr.se = curr.e.s;
            if(curr.se != null) {
                curr.se.nw = curr;
            }
            curr.s = curr.sw = curr.w = curr.nw = null;
            curr.n = curr.e.nw;
            if(curr.n != null) {
                curr.n.s = curr;
            }
            curr.ne = curr.e.n;
            if(curr.ne != null) {
                curr.ne.sw = curr;
            }

            j = j.s;
        }
        ++columns;
        topleft = topleft.w;
    }

    /**
     * Adds a column of cells to the right of the grid.
     */
    private void addRightColumn() {
        Cell curr, j = bottomright;
        while(j != null) {
            j.e = (curr = new Cell(false, j.x + 1, j.y));
            curr.w = j;
            curr.nw = curr.w.n;
            if(curr.nw != null) {
                curr.nw.se = curr;
            }
            curr.n = curr.ne = curr.e = curr.se = null;
            curr.s = curr.w.se;
            if(curr.s != null) {
                curr.s.n = curr;
            }
            curr.sw = curr.w.s;
            if(curr.sw != null) {
                curr.sw.ne = curr;
            }

            j = j.n;
        }
        ++columns;
        bottomright = bottomright.e;
    }

    /**
     * Adds dead cells around the edge of the grid.
     */
    private void expandGrid() {
        Cell curr;

        int minimumCellsToExpand = rules.getBornMinimum();
        if(minimumCellsToExpand > 3) {
            return;
        }

        if(columns > minimumCellsToExpand) {
            for(curr = topleft; curr != null; curr = curr.e) {
                if(curr.live) {
                    if(minimumCellsToExpand == 1) {
                        addTopRow();
                        break;
                    }
                    else if(minimumCellsToExpand == 2 &&
                            ((curr.w != null && curr.w.live) || (curr.e != null && curr.e.live))) {
                        addTopRow();
                        break;
                    }
                    else if(curr.w != null && curr.w.live && curr.e != null && curr.e.live) {
                        addTopRow();
                        break;
                    }
                }
            }
            for(curr = bottomright; curr != null; curr = curr.w) {
                if(curr.live) {
                    if(minimumCellsToExpand == 1) {
                        addBottomRow();
                        break;
                    }
                    else if(minimumCellsToExpand == 2 &&
                            ((curr.e != null && curr.e.live) || (curr.w != null && curr.w.live))) {
                        addBottomRow();
                        break;
                    }
                    else if(curr.e != null && curr.e.live && curr.w != null && curr.w.live) {
                        addBottomRow();
                        break;
                    }
                }
            }
        }

        if(rows > minimumCellsToExpand) {
            for(curr = topleft; curr != null; curr = curr.s) {
                if(curr.live) {
                    if(minimumCellsToExpand == 1) {
                        addLeftColumn();
                        break;
                    }
                    else if(minimumCellsToExpand == 2 &&
                            ((curr.s != null && curr.s.live) || (curr.n != null && curr.n.live))) {
                        addLeftColumn();
                        break;
                    }
                    else if(curr.s != null && curr.s.live && curr.n != null && curr.n.live) {
                        addLeftColumn();
                        break;
                    }
                }
            }
            for(curr = bottomright; curr != null; curr = curr.n) {
                if(curr.live) {
                    if(minimumCellsToExpand == 1) {
                        addRightColumn();
                        break;
                    }
                    else if(minimumCellsToExpand == 2 &&
                            ((curr.n != null && curr.n.live) || (curr.s != null && curr.s.live))) {
                        addRightColumn();
                        break;
                    }
                    else if(curr.s != null && curr.s.live && curr.n != null && curr.n.live) {
                        addRightColumn();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Generates the next iteration of the game of life.
     */
    public void next() {
        class UpdateCell {
            Cell cellRef;
            boolean updateValue;

            UpdateCell(Cell cellRef, boolean updateValue) {
                this.cellRef = cellRef;
                this.updateValue = updateValue;
            }
        }

        expandGrid();

        Cell current = topleft, rowHeader = topleft;
        Queue<UpdateCell> buffer = new ArrayDeque<UpdateCell>();
        UpdateCell u;

        while(rowHeader != null) {
            while(current != null) {
                buffer.add(new UpdateCell(current, rules.apply(current)));
                if(buffer.size() >= columns + 2) {
                    u = buffer.remove();
                    u.cellRef.live = u.updateValue;
                }
                current = current.e;
            }
            rowHeader = rowHeader.s;
            current = rowHeader;
        }
        while(!buffer.isEmpty()) {
            u = buffer.remove();
            u.cellRef.live = u.updateValue;
        }

        ++iterations;
    }
}
