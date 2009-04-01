/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.tutorials;

import java.net.URL;

import pivot.wtk.ApplicationContext;
import pivot.wtk.media.Image;

public class CustomTableRow {
    private boolean a = false;
    private Image b = null;
    private String c = null;

    public boolean getA() {
        return a;
    }

    public void setA(boolean a) {
        this.a = a;
    }

    public Image getB() {
        return b;
    }

    public void setB(Image b) {
        this.b = b;
    }

    public void setB(URL bURL) {
        Image b = (Image)ApplicationContext.getResourceCache().get(bURL);

        if (b == null) {
            b = Image.load(bURL);
            ApplicationContext.getResourceCache().put(bURL, b);
        }

        setB(b);
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }
}
