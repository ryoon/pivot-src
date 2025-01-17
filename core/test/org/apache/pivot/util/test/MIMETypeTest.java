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
package org.apache.pivot.util.test;

import static org.junit.Assert.assertEquals;

import org.apache.pivot.util.MIMEType;
import org.junit.Test;

/**
 * Testing the {@link MIMEType} decoding method.
 */
public class MIMETypeTest {
    /**
     * Basic tests.
     */
    @Test
    public void testMIMEType() {
        MIMEType mimeType = MIMEType.decode("foo; a=123; b=456; c=789");
        assertEquals(mimeType.getBaseType(), "foo");
        assertEquals(mimeType.get("a"), "123");
        assertEquals(mimeType.get("b"), "456");
        assertEquals(mimeType.get("c"), "789");
    }
}
