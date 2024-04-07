package org.ly.lysudoku.io;

import org.ly.lysudoku.Grid;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Static methods to load and store Sudokus from and to
 * files or the clipboard.
 * <p>
 * The support for formats is minimal and quick&dirty.
 * Only plain text formats are supported when reading:
 * <ul>
 * <li>A single line of 81 characters (all characters not in the
 * '1' - '9' range is considered as an empty cell).
 * <li>9 lines of 9 characters.
 * <li>Other multi-lines formats, with more than one character per cell,
 * or more than one line per row, or even with a few characters between
 * blocks might be supported, but there is no warranty. If a given format
 * works, and is not one of the first two above, you should consider you are lucky.
 * </ul>
 * <p>
 * When writing, the following format is used:
 * <ul>
 * <li>9 lines of 9 characters
 * <li>empty cells are represented by a '.'
 * </ul>
 */
public class SudokuIO {

    private static final int RES_OK = 2;
    private static final int RES_WARN = 1;
    private static final int RES_ERROR = 0;

    private static final String ERROR_MSG = "Unreadable Sudoku format";
    private static final String WARNING_MSG = "Warning: the Sudoku format was not recognized.\nThe Sudoku may not have been read correctly";

    private static int loadFromReader(Grid grid, Reader reader) throws IOException {
        List<String> lines = new ArrayList<String>();
        LineNumberReader lineReader = new LineNumberReader(reader);
        String line = lineReader.readLine();
        while (line != null) {
            lines.add(line);
            line = lineReader.readLine();
        }
        String[] arrLines = new String[lines.size()];
        lines.toArray(arrLines);

        int result = loadFromLines(grid, arrLines);
        if (result == RES_ERROR) {
            for (int i = 0; i < arrLines.length; i++)
                arrLines[i] = arrLines[i].trim();
            return loadFromSingleLine(grid, lines.get(0));
        }
        return result;
    }


    private static int TryParseSudoku(Grid grid, String singleLine) {
        String[] singleLineFormat = new String[] {
                "(^|[^01-9])(?<key>[01-9]{81})($|[^01-9])",
                "(^|[^.1-9])(?<key>[.1-9]{81})($|[^.1-9])",
                "(^|[^*1-9])(?<key>[*1-9]{81})($|[^*1-9])",
                "(^|[^_1-9])(?<key>[_1-9]{81})($|[^_1-9])" };

        for (String pattern : singleLineFormat)
        {
            Matcher result = java.util.regex.Pattern.compile(pattern).matcher(singleLine);
            if (result.find())
            {
                String line = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    line = result.group("key");
                }
                for (int y = 0; y < 9; y++)
                {
                    for (int x = 0; x < 9; x++)
                    {
                        char ch = line.charAt(x + y * 9);
                        if (ch >= '1' && ch <= '9')
                            grid.setCellValue(x, y, ch - '0');
                    }
                }
                grid.fixGivens();	// fix #99
                return RES_OK;
            }
        }
        return RES_ERROR;
    }

    private static int TryParseSukaku(Grid grid, String singleLine) {
        String[] sukakuLineFormat = new String[] {
                "(^|[^01-9])(?<key>[01-9]{729})($|[^01-9])",
                "(^|[^.1-9])(?<key>[.1-9]{729})($|[^.1-9])",
                "(^|[^*1-9])(?<key>[*1-9]{729})($|[^*1-9])",
                "(^|[^_1-9])(?<key>[_1-9]{729})($|[^_1-9])" };

        for (String pattern : sukakuLineFormat)
        {
            Matcher result = java.util.regex.Pattern.compile(pattern).matcher(singleLine);
            if (result.find())
            {
                String line = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    line = result.group("key");
                }
                for (int y = 0; y < 9; y++)
                {
                    for (int x = 0; x < 9; x++)
                    {
                        HashSet<Integer> possible = new HashSet<Integer>();
                        for (int i = 0; i < 9; i++)
                        {
                            char ch = line.charAt(i + x * 9 + y * 81);
                            if (ch >= '1' && ch <= '9')
                                possible.add(ch - '0');
                        }

                        int cellIndex = Grid.getCell(x, y).getIndex();
                        for (int i = 1; i <= 9; i++)
                        {
                            if (!possible.contains(i))
                                grid.removeCellPotentialValue(cellIndex, i);
                        }
                    }
                }

                // fixup naked singles
                grid.adjustPencilmarks();
                grid.fixGivens();	// fix #99
                grid.setSukaku();
                return RES_OK;
            }
        }
        return RES_ERROR;
    }

    private static int loadFromLines(Grid grid, String[] lines) {
        String singleLine = String.join("", lines);
        if (TryParseSudoku(grid, singleLine) == RES_OK)
        {
            return RES_OK;
        }

        if (TryParseSukaku(grid, singleLine) == RES_OK)
        {
            return RES_OK;
        }

        singleLine = singleLine.replace("#", ".").replaceAll("([^._01-9])", "");
        if (TryParseSudoku(grid, singleLine) == RES_OK)
        {
            return RES_OK;
        }

        //Last resort try first 81 parts of candidates separated by space
        String allLines = String.join(" ", lines);
        String newLines = allLines.replace(".", "0").replace("/", "0").replace("*", "0").replace("-", "").replace("+", "").replace("^", "").replace("%", "").replace("&", "").replace("(", "").replace(")", "").replace("@", "").replace("$", "").replaceAll("[a-zA-Z]", "").replaceAll("[^1-9]", " ").trim();
        String[] parts = newLines.split("\\s+");
        int y = 0, x = 0;
        for (String part : parts) {
            int cellIndex = Grid.getCell(x, y).getIndex();
            HashSet<Integer> possible = new HashSet<Integer>();
            for (char ch : part.toCharArray()) {
                if (ch >= '1' && ch <= '9')
                    possible.add(ch - '0');
            }
            for (int i = 1; i <= 9; i++) {
                if (!possible.contains(i))
                    grid.removeCellPotentialValue(cellIndex, i);
            }

            x++;
            if (x == 9) {
                y++;
                x = 0;
                if (y == 9)
                    break; //ignore if more than 81 found
            }
        }

        // fixup naked singles
        grid.adjustPencilmarks();
        grid.fixGivens();	// fix #99
        return parts.length == 81 ? RES_OK : RES_WARN;
    }

    private static int loadFromSingleLine(Grid grid, String line) {
        boolean isStandard = (line.length() == 81);
        // Detect Sudoku Susser format (Although the SS cannot cut/past to itself)
        if (line.endsWith("\t"))
            line = line.substring(0, line.length() - 1);
        boolean hasAlphaLabel = false;
        for (int i = 0; i < line.length() - 81; i++) {
            if (Character.isLetter(line.charAt(i)))
                hasAlphaLabel = true;
        }
        for (int i = line.length() - 81; i < line.length(); i++) {
            if (i >= 0 && Character.isLetter(line.charAt(i)))
                hasAlphaLabel = false;
        }
        if (hasAlphaLabel && line.length() > 81)
            line = line.substring(line.length() - 81);
        else if (line.trim().length() >= 81)
            line = line.trim();

        if (line.length() >= 81) {
            int rowGap = (line.length() - 81) / 8;
            int srcIndex = 0;
            for (int y = 0; y < 9; y++) {
                for (int x = 0; x < 9; x++) {
                    char ch = line.charAt(srcIndex++);
                    int value = 0;
                    if (ch >= '1' && ch <= '9')
                        value = ch - '0';
                    else if (ch != '.' && ch != '0')
                        isStandard = false;
                    grid.setCellValue(x, y, value);
                }
                srcIndex += rowGap;
            }
            grid.fixGivens();	// fix #99
            return (isStandard ? RES_OK : RES_WARN);
        }
        return RES_ERROR;
    }

    private static void saveToWriter(Grid grid, Writer writer, boolean line) throws IOException {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                int value = grid.getCellValue(x, y);
                int ch = '.';
                if (value > 0)
                    ch = '0' + value;
                writer.write(ch);
            }
            if (!line)
                writer.write("\r\n");
        }
    }

    private static void saveToWriterPencilmarks(Grid grid, Writer writer, boolean line) throws IOException {
        if (line)
            writer.write(grid.toStringPencilmarks());

        else
            writer.write(grid.toStringMultilinePencilmarks());
    }

    //@SudokuMonster: The process to copy Variant region grid SudokuFrame ---> SudokuExplainer ----> SudokuIO ----> Grid
    private static void saveToWriterVariant(Grid grid, Writer writer, boolean line) throws IOException {
        if (line)
            writer.write(grid.toStringVariantGrid());

        else
            writer.write(grid.toStringMultilinePencilmarksVariant());
    }

        /**
         * Test whether a Sudoku can be loaded from the current
         * content of the clipboard.
         * @return whether a Sudoku can be loaded from the current
         * content of the clipboard
         */

        /*
    public static boolean isClipboardLoadable() {
        Grid grid = new Grid();
        return (loadFromClipboard(grid) == null);
    }

    public static ErrorMessage loadFromClipboard(Grid grid) {
        Transferable content =
                Toolkit.getDefaultToolkit().getSystemClipboard().getContents(grid);
        if (content == null)
            return new ErrorMessage("The clipboard is empty");
        Reader reader = null;
        try {
            DataFlavor flavor = new DataFlavor(String.class, "Plain text");
            reader = flavor.getReaderForText(content);
            int result = loadFromReader(grid, reader);
            if (result == RES_OK) // success
                return null;
            if (result == RES_WARN) // warning
                return new ErrorMessage(WARNING_MSG, false, (Object[])(new String[0]));
            else // error
                return new ErrorMessage(ERROR_MSG, true, (Object[])(new String[0]));
        } catch (IOException ex) {
            return new ErrorMessage("Error while copying:\n{0}", ex);
        } catch (UnsupportedFlavorException ex) {
            return new ErrorMessage("Unsupported data type");
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch(Exception ex) {}
        }
    }

    public static void saveToClipboard(Grid grid, boolean line) {
        StringWriter writer = new StringWriter();
        try {
            saveToWriter(grid, writer, line);
            StringSelection data = new StringSelection(writer.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void savePencilmarksToClipboard(Grid grid, boolean line) {
        StringWriter writer = new StringWriter();
        try {
            saveToWriterPencilmarks(grid, writer, line);
            StringSelection data = new StringSelection(writer.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void saveVariantToClipboard(Grid grid, boolean line) {
        StringWriter writer = new StringWriter();
        try {
            saveToWriterVariant(grid, writer, line);
            StringSelection data = new StringSelection(writer.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static ErrorMessage loadFromFile(Grid grid, File file) {
        Reader reader = null;
        try {
            FileReader freader = new FileReader(file);
            reader = new BufferedReader(freader);
            int result = loadFromReader(grid, reader);
            if (result == RES_OK)
                return null;
            if (result == RES_WARN) // warning
                return new ErrorMessage(WARNING_MSG, false, (Object[])(new String[0]));
            else // error
                return new ErrorMessage(ERROR_MSG, true, (Object[])(new String[0]));
        } catch (FileNotFoundException ex) {
            return new ErrorMessage("File not found: {0}", file);
        } catch (IOException ex) {
            return new ErrorMessage("Error while reading file {0}:\n{1}", file, ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static ErrorMessage saveToFile(Grid grid, File file) {
        Writer writer = null;
        try {
            FileWriter fwriter = new FileWriter(file);
            writer = new BufferedWriter(fwriter);
            boolean line = false;
            saveToWriter(grid, writer, line);
            return null;
        } catch (IOException ex) {
            return new ErrorMessage("Error while writing file {0}:\n{1}", file, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }*/

}
