package borseth.owen.openlands;

import android.graphics.Bitmap;

public class GraphicObject {
    private Bitmap _bitmap;
    private Coordinates _coordinates;
 
    public GraphicObject(Bitmap bitmap) {
        _bitmap = bitmap;
        _coordinates = new Coordinates();
    }
 
    public Bitmap getGraphic() {
        return _bitmap;
    }
 
    public Coordinates getCoordinates() {
        return _coordinates;
    }
 
    /**
     * Contains the coordinates of the graphic.
     */
    public class Coordinates {
        private int _x = 100;
        private int _y = 0;
 
        public int getX() {
            return _x + _bitmap.getWidth() / 2;
        }
 
        public void setX(int value) {
            _x = value - _bitmap.getWidth() / 2;
        }
 
        public int getY() {
            return _y + _bitmap.getHeight() / 2;
        }
 
        public void setY(int value) {
            _y = value - _bitmap.getHeight() / 2;
        }
 
        public String toString() {
            return "Coordinates: (" + _x + "/" + _y + ")";
        }
    }
}