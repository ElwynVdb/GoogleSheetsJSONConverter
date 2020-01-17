package converter;

public class SpreadSheet {

    public Feed feed;

    public SpreadSheet() {
        feed = new Feed();
    }

    public static class Feed {
        public Entry[] entry;
    }

    public static class Entry {
        public Cell gs$cell;
    }

    public static class Cell {
        public int row;
        public int col;
        public String $t;
    }
}
