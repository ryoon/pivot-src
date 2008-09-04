package pivot.wtk.media.drawing;

import java.awt.Graphics2D;

import pivot.wtk.Bounds;

public class Clone extends Shape {
    private Shape source = null;

    public Shape getSource() {
        return source;
    }

    public void setSource(Shape source) {
        this.source = source;
    }

    @Override
    public Bounds getUntransformedBounds() {
        return (source == null) ? new Bounds(0, 0, 0, 0) : source.getUntransformedBounds();
    }

    @Override
    public void fill(Graphics2D graphics) {
        if (source != null) {
            source.fill(graphics);
        }
    }

    @Override
    public void stroke(Graphics2D graphics) {
        if (source != null) {
            source.stroke(graphics);
        }
    }
}
