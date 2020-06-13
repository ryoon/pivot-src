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
package org.apache.pivot.tests;

import java.io.IOException;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Console;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.util.TextAreaOutputStream;


public class TextAreaConsoleTest implements Application {
    @BXML private Window window;
    @BXML private PushButton logMessageButton;
    @BXML private TextArea consoleArea;
    private Console console;
    private int line = 1;

    private static final String[] PARAGRAPHS = {
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt "
      + "ut labore et dolore magna aliqua. Commodo odio aenean sed adipiscing diam donec adipiscing "
      + "tristique. Orci nulla pellentesque dignissim enim sit amet venenatis. Magna sit amet purus "
      + "gravida quis blandit. Tincidunt eget nullam non nisi est sit. Quis ipsum suspendisse ultrices "
      + "gravida dictum fusce ut placerat. Et pharetra pharetra massa massa ultricies mi. In fermentum "
      + "posuere urna nec tincidunt praesent. Ut etiam sit amet nisl purus in mollis nunc sed. Euismod "
      + "lacinia at quis risus sed vulputate odio ut enim. In nibh mauris cursus mattis molestie a "
      + "iaculis at erat. Dapibus ultrices in iaculis nunc sed. Viverra suspendisse potenti nullam "
      + "ac tortor vitae purus faucibus ornare. Scelerisque fermentum dui faucibus in ornare quam "
      + "viverra orci. Sagittis orci a scelerisque purus semper eget duis at tellus. Auctor eu augue "
      + "ut lectus. Mattis rhoncus urna neque viverra justo nec ultrices dui sapien. Pellentesque eu "
      + "tincidunt tortor aliquam nulla. Massa eget egestas purus viverra accumsan in nisl. Sed velit "
      + "dignissim sodales ut eu sem integer.",

        "Tortor consequat id porta nibh venenatis cras sed felis eget. Viverra adipiscing at in tellus "
      + "integer feugiat scelerisque. Hendrerit dolor magna eget est lorem ipsum dolor. Viverra tellus "
      + "in hac habitasse platea dictumst vestibulum. Eget nulla facilisi etiam dignissim diam. At in "
      + "tellus integer feugiat scelerisque varius morbi. Et egestas quis ipsum suspendisse ultrices "
      + "gravida dictum fusce. Faucibus pulvinar elementum integer enim neque. A diam sollicitudin "
      + "tempor id. Metus aliquam eleifend mi in nulla posuere sollicitudin aliquam ultrices. "
      + "Consequat nisl vel pretium lectus quam id leo in. Adipiscing elit ut aliquam purus sit. "
      + "A diam sollicitudin tempor id eu nisl nunc. Cursus euismod quis viverra nibh cras pulvinar "
      + "mattis nunc sed. Aliquet bibendum enim facilisis gravida neque.",

        "Lorem sed risus ultricies tristique nulla aliquet enim tortor. Elementum nisi quis eleifend "
      + "quam adipiscing vitae proin sagittis nisl. Cursus turpis massa tincidunt dui. Quam adipiscing "
      + "vitae proin sagittis. Porta nibh venenatis cras sed felis eget. Morbi blandit cursus risus at "
      + "ultrices mi tempus. Ac auctor augue mauris augue neque gravida. Proin nibh nisl condimentum "
      + "id venenatis a condimentum vitae. Purus in massa tempor nec feugiat nisl. Phasellus egestas "
      + "tellus rutrum tellus pellentesque eu.",

        "Pharetra diam sit amet nisl. Rhoncus urna neque viverra justo nec. Auctor elit sed vulputate "
      + "mi sit amet mauris commodo quis. Penatibus et magnis dis parturient montes nascetur ridiculus "
      + "mus. Mi ipsum faucibus vitae aliquet nec ullamcorper sit amet risus. Blandit volutpat "
      + "maecenas volutpat blandit aliquam etiam erat. In arcu cursus euismod quis viverra nibh cras "
      + "pulvinar. Elementum integer enim neque volutpat ac tincidunt vitae semper. Feugiat pretium "
      + "nibh ipsum consequat nisl vel pretium lectus quam. Aenean sed adipiscing diam donec "
      + "adipiscing tristique risus. Nunc sed augue lacus viverra vitae congue eu consequat. "
      + "Malesuada fames ac turpis egestas. Libero volutpat sed cras ornare arcu dui vivamus arcu "
      + "felis. Id aliquet risus feugiat in ante metus dictum. Netus et malesuada fames ac turpis "
      + "egestas sed. Enim tortor at auctor urna nunc id cursus. Ligula ullamcorper malesuada proin "
      + "libero nunc consequat. Diam volutpat commodo sed egestas egestas fringilla. Habitasse platea "
      + "dictumst quisque sagittis purus sit amet volutpat consequat. Gravida cum sociis natoque "
      + "penatibus et magnis dis parturient.",

        "Aliquam vestibulum morbi blandit cursus risus at. Lectus vestibulum mattis ullamcorper velit "
      + "sed. Congue nisi vitae suscipit tellus mauris a diam maecenas sed. Eleifend donec pretium "
      + "vulputate sapien nec sagittis aliquam malesuada. Nulla facilisi cras fermentum odio eu "
      + "feugiat pretium nibh. Nibh tortor id aliquet lectus proin nibh nisl condimentum. Sodales "
      + "ut eu sem integer. Amet dictum sit amet justo. Non pulvinar neque laoreet suspendisse "
      + "interdum consectetur libero id faucibus. Fermentum leo vel orci porta non pulvinar neque "
      + "laoreet. Blandit massa enim nec dui nunc mattis. Arcu non odio euismod lacinia. Orci porta "
      + "non pulvinar neque laoreet suspendisse. Feugiat in ante metus dictum. Sodales ut etiam sit "
      + "amet nisl purus. Ut aliquam purus sit amet luctus.",

        "Rhoncus dolor purus non enim praesent elementum facilisis leo vel. Vivamus at augue eget "
      + "arcu dictum. Pellentesque elit ullamcorper dignissim cras tincidunt lobortis. Facilisis "
      + "gravida neque convallis a cras semper. Tempor orci eu lobortis elementum nibh. Consequat "
      + "nisl vel pretium lectus quam id leo in. Sed pulvinar proin gravida hendrerit. Velit "
      + "laoreet id donec ultrices. Etiam non quam lacus suspendisse faucibus interdum posuere "
      + "lorem ipsum. Tellus at urna condimentum mattis pellentesque id nibh tortor. Egestas congue "
      + "quisque egestas diam in arcu cursus euismod. Amet facilisis magna etiam tempor orci eu. "
      + "Aliquet enim tortor at auctor urna. Nulla facilisi nullam vehicula ipsum a arcu. Nisl "
      + "condimentum id venenatis a condimentum. Vel fringilla est ullamcorper eget nulla facilisi "
      + "etiam dignissim. Quis varius quam quisque id.",

        "Massa enim nec dui nunc mattis enim ut tellus. Amet dictum sit amet justo. Viverra "
      + "accumsan in nisl nisi. Nulla facilisi nullam vehicula ipsum a. Proin nibh nisl condimentum "
      + "id. Vulputate ut pharetra sit amet. Consectetur lorem donec massa sapien faucibus et "
      + "molestie ac feugiat. Ut morbi tincidunt augue interdum velit euismod in. Non consectetur "
      + "a erat nam at. Aliquam sem fringilla ut morbi tincidunt augue interdum velit. Nisl nisi "
      + "scelerisque eu ultrices vitae auctor eu augue ut. Ut venenatis tellus in metus. Massa eget "
      + "egestas purus viverra accumsan in nisl nisi. In est ante in nibh. Elementum sagittis vitae "
      + "et leo duis ut. Nibh sed pulvinar proin gravida. Tristique senectus et netus et malesuada. "
      + "In hac habitasse platea dictumst. Elit scelerisque mauris pellentesque pulvinar "
      + "pellentesque habitant morbi tristique senectus. Elementum sagittis vitae et leo duis ut "
      + "diam quam nulla.",

        "Orci eu lobortis elementum nibh. Facilisis volutpat est velit egestas dui id. Eleifend "
      + "quam adipiscing vitae proin sagittis nisl rhoncus. Massa sed elementum tempus egestas sed "
      + "sed risus. Tortor dignissim convallis aenean et tortor. Pellentesque adipiscing commodo "
      + "elit at imperdiet dui accumsan sit. Habitasse platea dictumst vestibulum rhoncus est. "
      + "Eleifend donec pretium vulputate sapien. Aliquam sem et tortor consequat. Dignissim "
      + "suspendisse in est ante. Neque convallis a cras semper auctor neque. Eget magna fermentum "
      + "iaculis eu non. Suscipit tellus mauris a diam.",

        "Adipiscing bibendum est ultricies integer. Amet dictum sit amet justo donec enim diam "
      + "vulputate ut. Sagittis aliquam malesuada bibendum arcu vitae elementum curabitur vitae "
      + "nunc. Ac turpis egestas maecenas pharetra. A arcu cursus vitae congue mauris rhoncus. "
      + "Sed pulvinar proin gravida hendrerit lectus. Pharetra massa massa ultricies mi quis. "
      + "Velit ut tortor pretium viverra suspendisse. Tristique sollicitudin nibh sit amet "
      + "commodo. Cursus in hac habitasse platea dictumst quisque.",

        "Pharetra diam sit amet nisl suscipit adipiscing bibendum. Imperdiet massa tincidunt "
      + "nunc pulvinar. Aliquet sagittis id consectetur purus ut. Volutpat maecenas volutpat "
      + "blandit aliquam. At erat pellentesque adipiscing commodo elit at imperdiet dui accumsan. "
      + "Lectus arcu bibendum at varius vel pharetra. Mi in nulla posuere sollicitudin aliquam "
      + "ultrices sagittis orci a. Hac habitasse platea dictumst quisque sagittis purus sit. In "
      + "ante metus dictum at. Fermentum et sollicitudin ac orci phasellus egestas tellus rutrum "
      + "tellus. Malesuada fames ac turpis egestas maecenas. Scelerisque viverra mauris in "
      + "aliquam sem fringilla ut morbi tincidunt. Ac placerat vestibulum lectus mauris ultrices "
      + "eros in. Non pulvinar neque laoreet suspendisse interdum consectetur libero. Varius "
      + "morbi enim nunc faucibus a pellentesque."
    };
    private static final int NUM_PARAGRAPHS = PARAGRAPHS.length;

    private static int randomInt(final int max) {
        return (int) Math.floor(Math.random() * (double) max);
    }

    @Override
    public void startup(final Display display, final Map<String, String> properties) {
        BXMLSerializer serializer = new BXMLSerializer();
        try {
            serializer.readObject(TextAreaConsoleTest.class, "console_test.bxml");
            serializer.bind(this);
        } catch (IOException | SerializationException ex) {
            throw new RuntimeException(ex);
        }
        console = new Console(new TextAreaOutputStream(consoleArea, 8192).toPrintStream());
        logMessageButton.getButtonPressListeners().add(
            b -> console.log(String.format("%1$d. %2$s", line++, PARAGRAPHS[randomInt(NUM_PARAGRAPHS)])));
        window.open(display);
    }

    @Override
    public boolean shutdown(final boolean optional) {
        if (window != null) {
            window.close();
        }
        return false;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(TextAreaConsoleTest.class, args);
    }
}
