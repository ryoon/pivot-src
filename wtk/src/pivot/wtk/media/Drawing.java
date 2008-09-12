/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.media;

import java.awt.Graphics2D;
import pivot.wtk.Bounds;
import pivot.wtk.media.drawing.Group;

/**
 * <p>Image representing a vector drawing.</p>
 *
 * @author gbrown
 */
public class Drawing extends Image {
    private Group shapes = new Group();

    public int getWidth() {
        Bounds bounds = shapes.getBounds();
        return bounds.width + bounds.x;
    }

    public int getHeight() {
        Bounds bounds = shapes.getBounds();
        return bounds.height + bounds.y;
    }

    public void paint(Graphics2D graphics) {
        graphics.clipRect(0, 0, getWidth(), getHeight());
        shapes.paint(graphics);
    }

    public Group getShapes() {
        return shapes;
    }
}
