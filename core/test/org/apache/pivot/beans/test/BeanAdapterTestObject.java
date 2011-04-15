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
package org.apache.pivot.beans.test;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BeanAdapterTestObject {

    private BigDecimal bd;
    private BigInteger bi;
    private String string;

    public BigDecimal getBd() {
        return bd;
    }

    protected BigInteger getBi() {
        return bi;
    }

    protected void setBi(BigInteger bi) {
        this.bi = bi;
    }

    protected void setBd(BigDecimal bd) {
        this.bd = bd;
    }

    protected String getString() {
        return string;
    }

    protected void setString(String string) {
        this.string = string;
    }

}
