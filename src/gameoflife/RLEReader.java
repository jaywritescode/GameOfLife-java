package gameoflife;

import java.io.*;
import java.util.regex.*;

public class RLEReader {

    private BufferedReader br;

    final static String xPattern = "x ?= ?([1-9]\\d*)",
                        yPattern = "y ?= ?([1-9]\\d*)",
                        rulePattern = "rule ?= ?((B[0-8]*/S[0-8]*)|([0-8]*/[0-8]*))",
                        cellRunPattern = "([1-9]\\d*)?([bo$])";

    public RLEReader(File file) throws FileNotFoundException {
        br = new BufferedReader(new FileReader(file));
    }

    public RLEReader(String rle) {
        br = new BufferedReader(new StringReader(rle));
    }

    private String readLine() throws IOException {
        return br.readLine();
    }

    private void close() throws IOException {
        br.close();
    }

    public static GameOfLife create(final File file) throws FileNotFoundException, IllegalArgumentException {
        return (new RLEReader(file)).load();
    }

    public static GameOfLife create(final String rle) {
        return (new RLEReader(rle)).load();
     }

    public GameOfLife load() {
        try {
            String header = getHeaderLine();

            Matcher headerMatcher = Pattern.compile(String.format("^%s, ?%s(, ?%s)?$",
                    xPattern, yPattern, rulePattern), Pattern.CASE_INSENSITIVE).matcher(header);
            if(!headerMatcher.matches()) {
                throw new IllegalArgumentException("Invalid header line format.");
            }

            int x = Integer.parseInt(headerMatcher.group(1)), y = Integer.parseInt(headerMatcher.group(2));
            if(x < 1 || y < 1) {
                throw new IllegalArgumentException("Grid height and width must be greater than zero.");
            }

            String rule;
            if(headerMatcher.group(4) != null) {
                rule = headerMatcher.group(4);
            }
            else {
                rule = "B3/S23";
            }

            boolean[][] grid = new boolean[y][x];

            String[] cellStrings = getCellString().split("\\$");
            Pattern p = Pattern.compile(cellRunPattern);
            Matcher cellRunMatcher;

            int row = 0;
            for(String cellString : cellStrings) {
                cellRunMatcher = p.matcher(cellString + "$");
                int col = 0;

                int runLength;
                String tag;
                while(cellRunMatcher.find()) {
                    if(cellRunMatcher.group(1) != null) {
                        runLength = Integer.parseInt(cellRunMatcher.group(1));
                    }
                    else {
                        runLength = 1;
                    }
                    tag = cellRunMatcher.group(2);

                    if(!tag.equals("$")) {
                        try {
                            for(int i = 0; i < runLength; ++i) {
                                grid[row][col++] = tag.equals("o");
                            }
                        }
                        catch(ArrayIndexOutOfBoundsException e) {
                            throw new IllegalArgumentException("Too many cells in row.");
                        }
                    }
                    else {
                        row += runLength;           // implicit break statement here
                    }
                }
            }
            return new GameOfLife(grid, rule);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Advances the RLEReader to the first non-comment line and returns it.
     *
     * @return the RLE header line
     * @throws IOException via {@link gameoflife.RLEReader#readLine}
     * @throws IllegalArgumentException if the reader gets to the end of the file
     */
    private String getHeaderLine() throws IOException, IllegalArgumentException {
        String line;
        try {
            while(true) {
                line = readLine();
                if(!line.startsWith("#")) {
                    return line;
                }
            }
        }
        catch(NullPointerException e) {
            throw new IllegalArgumentException("No non-comment data in run-length enconding.");
        }
    }

    private String getCellString() throws IOException, IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        String line;
        int i;

        while((line = readLine()) != null) {
            if((i = line.indexOf("!")) > -1) {
                sb.append(line.substring(0, i));
                break;
            }
            else {
                sb.append(line);
            }
        }

        if(sb.length() == 0) {
            throw new IllegalArgumentException("No cell data in run-length encoding.");
        }
        return sb.toString();
    }
}
