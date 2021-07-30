package converter;

public class SpreadSheet {

    private Feed feed;

    public SpreadSheet() {
        feed = new Feed();
    }

    public static class Feed {
        private Entry[] entry;

        public void setEntry(Entry[] entry) {
            this.entry = entry;
        }

        public Entry[] getEntry() {
            return entry;
        }
    }

    public static class Entry {
        private Cell gs$cell;

        public void setCell(Cell gs$cell) {
            this.gs$cell = gs$cell;
        }

        public Cell getCell() {
            return gs$cell;
        }
    }

    public static class Cell {
        private int row;
        private int col;
        private String $t;


        public void setContent(String $t) {
            this.$t = $t;
        }

        public void setColumn(int col) {
            this.col = col;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return col;
        }

        public String getContent() {
            return $t;
        }
    }

    public Feed getFeed() {
        return feed;
    }
}
