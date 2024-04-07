package org.ly.lysudoku.generator;

/**
 * Enumeration of Sudoku grid's symmetries
 */
public enum Symmetry {

    Vertical {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(8 - x, y)};
        }

        @Override
        public String getDescription() {
            return "Mirror symmetry around the vertical axis";
        }
    },
    Horizontal {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(x, 8 - y)};
        }

        @Override
        public String getDescription() {
            return "Mirror symmetry around the horizontal axis";
        }
    },
    Diagonal {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(8 - y, 8 - x)};
        }

        @Override
        public String getDescription() {
            return "Mirror symmetry around the raising diagonal";
        }
    },
    AntiDiagonal {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(y, x)};
        }

        @Override
        public String toString() {
            return "Anti-diagonal";
        }

        @Override
        public String getDescription() {
            return "Mirror symmetry around the falling diagonal";
        }
    },
    BiDiagonal {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(y, x),
                    new Point(8 - y, 8 - x),
                    new Point(8 - x, 8 - y)};
        }

        @Override
        public String toString() {
            return "Bi-diagonal";
        }

        @Override
        public String getDescription() {
            return "Mirror symmetries around both diagonals";
        }
    },
    Orthogonal {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(8 - x, y),
                    new Point(x, 8 - y),
                    new Point(8 - x, 8 - y)};
        }

        @Override
        public String getDescription() {
            return "Mirror symmetries around the horizontal and vertical axes";
        }
    },
    Rotational180 {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(8 - x, 8 - y)};
        }

        @Override
        public String toString() {
            return "180� rotational";
        }

        @Override
        public String getDescription() {
            return "Symmetric under a 180� rotation (central symmetry)";
        }
    },
    Rotational90 {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(8 - x, 8 - y),
                    new Point(y, 8 - x),
                    new Point(8 - y, x)};
        }

        @Override
        public String toString() {
            return "90� rotational";
        }

        @Override
        public String getDescription() {
            return "Symmetric under a 90� rotation";
        }
    },
    None {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y)};
        }

        @Override
        public String getDescription() {
            return "No symmetry";
        }
    },
    Full {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(8 - x, y),
                    new Point(x, 8 - y),
                    new Point(8 - x, 8 - y),
                    new Point(y, x),
                    new Point(8 - y, x),
                    new Point(y, 8 - x),
                    new Point(8 - y, 8 - x)};
        }

        @Override
        public String getDescription() {
            return "All symmetries (around the 8 axes and under a 90� rotation)";
        }
    },
    Full32 {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    //q1
                    new Point(x % 4, y % 4),
                    new Point(4 - x % 4, y % 4),
                    new Point(x % 4, 4 - y % 4),
                    new Point(4 - x % 4, 4 - y % 4),
                    new Point(y % 4, x % 4),
                    new Point(4 - y % 4, x % 4),
                    new Point(y % 4, 4 - x % 4),
                    new Point(4 - y % 4, 4 - x % 4),
                    //q2
                    new Point(x % 4 + 4, y % 4),
                    new Point(4 - x % 4 + 4, y % 4),
                    new Point(x % 4 + 4, 4 - y % 4),
                    new Point(4 - x % 4 + 4, 4 - y % 4),
                    new Point(y % 4 + 4, x % 4),
                    new Point(4 - y % 4 + 4, x % 4),
                    new Point(y % 4 + 4, 4 - x % 4),
                    new Point(4 - y % 4 + 4, 4 - x % 4),
                    //q3
                    new Point(x % 4, y % 4 + 4),
                    new Point(4 - x % 4, y % 4 + 4),
                    new Point(x % 4, 4 - y % 4 + 4),
                    new Point(4 - x % 4, 4 - y % 4 + 4),
                    new Point(y % 4, x % 4 + 4),
                    new Point(4 - y % 4, x % 4 + 4),
                    new Point(y % 4, 4 - x % 4 + 4),
                    new Point(4 - y % 4, 4 - x % 4 + 4),
                    //q4
                    new Point(x % 4 + 4, y % 4 + 4),
                    new Point(4 - x % 4 + 4, y % 4 + 4),
                    new Point(x % 4 + 4, 4 - y % 4 + 4),
                    new Point(4 - x % 4 + 4, 4 - y % 4 + 4),
                    new Point(y % 4 + 4, x % 4 + 4),
                    new Point(4 - y % 4 + 4, x % 4 + 4),
                    new Point(y % 4 + 4, 4 - x % 4 + 4),
                    new Point(4 - y % 4 + 4, 4 - x % 4 + 4)};
        }

        @Override
        public String getDescription() {
            return "Extended full symmetry (32-fold symmetry)";
        }
    };

    public abstract Point[] getPoints(int x, int y);

    public abstract String getDescription();

}
