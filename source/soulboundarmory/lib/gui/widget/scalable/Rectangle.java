package soulboundarmory.lib.gui.widget.scalable;

public class Rectangle {
    public final Point start = new Point();
    public final Point end = new Point();

    public int width() {
        return this.end.x - this.start.x;
    }

    public int height() {
        return this.end.y - this.start.y;
    }
}
