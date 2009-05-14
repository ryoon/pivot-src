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
package pivot.demos.clock;

import java.awt.Graphics2D;
import java.util.Calendar;

import pivot.wtk.media.Image;
import pivot.wtk.media.ImageListener;
import pivot.wtk.media.Movie;
import pivot.wtk.media.drawing.Shape;
import pivot.wtkx.WTKXSerializer;

/**
 * A concrete movie that animates a clock.
 *
 * @author tvolkert
 */
public class Clock extends Movie {
    private Calendar calendar = Calendar.getInstance();
    private WTKXSerializer wtkxSerializer;
    private Image image = null;

    public Clock() {
        wtkxSerializer = new WTKXSerializer();
        try {
            image = (Image)wtkxSerializer.readObject(getClass().getResource("clock.wtkd"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        image.getImageListeners().add(new ImageListener() {
            public void sizeChanged(Image image, int previousWidth, int previousHeight) {
                movieListeners.sizeChanged(Clock.this, previousWidth, previousHeight);
            }

            public void regionUpdated(Image image, int x, int y, int width, int height) {
                movieListeners.regionUpdated(Clock.this, x, y, width, height);
            }
        });

        setLooping(true);
        setFrameRate(1);
    }

    public void setCurrentFrame(int currentFrame) {
        Shape.Rotate secondsRotation = wtkxSerializer.getObjectByID("secondsRotation");
        Shape.Rotate minutesRotation = wtkxSerializer.getObjectByID("minutesRotation");
        Shape.Rotate hoursRotation = wtkxSerializer.getObjectByID("hoursRotation");

        calendar.setTimeInMillis(System.currentTimeMillis());

        int seconds = calendar.get(Calendar.SECOND);
        int minutes = calendar.get(Calendar.MINUTE);
        int hours = calendar.get(Calendar.HOUR) + 1;

        secondsRotation.setAngle(seconds * 6);
        minutesRotation.setAngle(minutes * 6 + seconds * (6d / 60));
        hoursRotation.setAngle(hours * 30 + minutes * (6d / 12));

        super.setCurrentFrame(currentFrame);
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public void paint(Graphics2D graphics) {
        image.paint(graphics);
    }

    public int getTotalFrames() {
        return 60;
    }
}
