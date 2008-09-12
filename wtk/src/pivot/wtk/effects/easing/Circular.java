/*
 * Copyright (c) 2003 Robert Penner, all rights reserved.
 *
 * This work is subject to the terms in
 * http://www.robertpenner.com/easing_terms_of_use.html.
 *
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
package pivot.wtk.effects.easing;

/**
 * <p>Circular easing operation.</p>
 *
 * @author Robert Penner
 * @author gbrown
 */
public class Circular implements Easing {
    public float easeIn(float time, float begin, float change, float duration) {
        return -change * ((float)Math.sqrt(1f - (time /= duration) * time) - 1f) + begin;
    }

    public float easeOut(float time, float begin, float change, float duration) {
        return change * (float)Math.sqrt(1f - (time = time / duration - 1f) * time) + begin;
    }

    public float easeInOut(float time, float begin, float change, float duration) {
        if ((time /= duration / 2f) < 1f) {
            return -change / 2f * ((float)Math.sqrt(1f - time * time) - 1f) + begin;
        } else {
            return change / 2f * ((float)Math.sqrt(1f - (time -= 2f) * time) + 1f) + begin;
        }
    }
}
