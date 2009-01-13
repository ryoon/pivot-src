/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.wtk.skin.text;

import java.awt.Graphics2D;

import pivot.wtk.Dimensions;
import pivot.wtk.text.TextNode;
import pivot.wtk.text.TextNodeListener;

/**
 * Text node view.
 *
 * @author gbrown
 */
public class TextNodeView extends NodeView implements TextNodeListener {
    public int getPreferredHeight(int width) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getPreferredWidth(int height) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Dimensions getPreferredSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public void paint(Graphics2D graphics) {
        // TODO Auto-generated method stub

    }

    public NodeView breakAt(int x) {
        // TODO
        return null;
    }

    public void charactersInserted(TextNode textNode, int index, int count) {
        // TODO Auto-generated method stub

    }

    public void charactersRemoved(TextNode textNode, int index,
        String characters) {
        // TODO Auto-generated method stub
    }
}
