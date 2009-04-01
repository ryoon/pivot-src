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
package pivot.web;

import pivot.util.Base64;

/**
 * Implementation of the {@link Authentication} interface supporting the
 * HTTP <a href="http://tools.ietf.org/rfc/rfc2617.txt">Basic
 * Authentication</a> scheme.
 */
public class BasicAuthentication implements Authentication {
    private String username = null;
    private String password = null;

    public BasicAuthentication(String username, String password) {
        this.username = (username == null) ? "" : username;
        this.password = (password == null) ? "" : password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void authenticate(Query<?> query) {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.encode(credentials.getBytes());

        query.getRequestProperties().put("Authorization", "Basic " + encodedCredentials);
    }
}
