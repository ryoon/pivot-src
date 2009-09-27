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
package org.apache.pivot.demos.scripting;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class ScriptingDemo implements Application {
    public static class MyButtonPressListener implements ButtonPressListener {
        @Override
        public void buttonPressed(Button button) {
            System.out.println("[Java] A button was clicked.");
        }
    }

    private Window window = null;

    private String foo;
    private List<?> listData;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        wtkxSerializer.put("bar", "12345");

        window = (Window)wtkxSerializer.readObject(this, "scripting_demo.wtkx");
        foo = (String)wtkxSerializer.get("foo");
        listData = (List<?>)wtkxSerializer.get("listData");

        System.out.println("foo = " + (foo == null ? null : "\"" + foo + "\""));
        System.out.println("listData.getLength() = " + listData.getLength());

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ScriptingDemo.class, args);
    }
}
