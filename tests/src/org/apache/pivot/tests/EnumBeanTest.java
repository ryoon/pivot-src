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

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Orientation;

public class EnumBeanTest {
    public static void main(String[] args) {

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        try {
            EnumBean enumBean = (EnumBean) bxmlSerializer.readObject(EnumBeanTest.class,
                "enum_bean.bxml");
            System.out.println("Bean read OK - " + enumBean);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SerializationException e) {
            e.printStackTrace();
        }

        // Test BeanAdapter get/put using field access prefix '~'
        EnumBean enumBean = new EnumBean();
        BeanAdapter ba = new BeanAdapter(enumBean);

        ba.put("~orientationField", Orientation.HORIZONTAL);
        dump(enumBean, ba);

        ba.put("~orientationField", "vertical");
        dump(enumBean, ba);
    }

    private static void dump(EnumBean enumBean, BeanAdapter ba) {
        Object value = enumBean.orientationField;
        System.out.println(String.format("\n%-40s %-20s %s", "Direct field access", value,
            (value == null) ? "[null]" : value.getClass().getName()));

        value = ba.get("~orientationField");
        System.out.println(String.format("%-40s %-20s %s", "~orientationField", value,
            (value == null) ? "[null]" : value.getClass().getName()));
    }
}
