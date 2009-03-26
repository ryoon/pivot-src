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
package pivot.wtk.media.drawing;

import java.awt.Graphics2D;

/**
 * Shape representing a copy of another shape.
 *
 * @author gbrown
 */
public class Clone extends Shape {
    private Shape source = null;

    public Shape getSource() {
        return source;
    }

    public void setSource(Shape source) {
        this.source = source;
    }

    public int getWidth() {
        // TODO
        return 0;
    }

    public int getHeight() {
        // TODO
        return 0;
    }

    public void paint(Graphics2D graphics) {
        // TODO
    }

    @Override
    public boolean contains(int x, int y) {
        return (source == null) ? false : source.contains(x, y);
    }
}
