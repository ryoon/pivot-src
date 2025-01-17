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
package org.apache.pivot.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.pivot.io.IOTask;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.util.Constants;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Abstract base class for web queries. A web query is an asynchronous operation
 * that executes one of the following HTTP methods: <ul> <li>GET</li>
 * <li>POST</li> <li>PUT</li> <li>DELETE</li> </ul>
 *
 * @param <V> The type of the value retrieved or sent via the query. For GET
 * operations, it is {@link Object}; for POST operations, the type is
 * {@link URL}. For PUT and DELETE, it is {@link Void}.
 */
public abstract class Query<V> extends IOTask<V> {
    /**
     * Supported HTTP methods.
     */
    public enum Method {
        GET, POST, PUT, DELETE;
    }

    /**
     * Query status codes.
     */
    public static class Status {
        public static final int OK = 200;
        public static final int CREATED = 201;
        public static final int NO_CONTENT = 204;

        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int METHOD_NOT_ALLOWED = 405;
        public static final int REQUEST_TIMEOUT = 408;
        public static final int CONFLICT = 409;
        public static final int LENGTH_REQUIRED = 411;
        public static final int PRECONDITION_FAILED = 412;
        public static final int REQUEST_ENTITY_TOO_LARGE = 413;
        public static final int REQUEST_URI_TOO_LONG = 414;
        public static final int UNSUPPORTED_MEDIA_TYPE = 415;

        public static final int INTERNAL_SERVER_ERROR = 500;
        public static final int NOT_IMPLEMENTED = 501;
        public static final int SERVICE_UNAVAILABLE = 503;
        public static final int HTTP_VERSION_NOT_SUPPORTED = 505;
    }

    private URL locationContext = null;
    private HostnameVerifier hostnameVerifier = null;
    private Proxy proxy = null;

    private QueryDictionary parameters = new QueryDictionary(true);
    private QueryDictionary requestHeaders = new QueryDictionary(false);
    private QueryDictionary responseHeaders = new QueryDictionary(false);
    private int status = 0;

    private volatile long bytesExpected = -1;

    private Serializer<?> serializer = new JSONSerializer();

    private QueryListener.Listeners<V> queryListeners = new QueryListener.Listeners<>();

    public static final int DEFAULT_PORT = -1;

    static {
        try {
            // See http://java.sun.com/javase/6/docs/technotes/guides/net/proxies.html
            // for more info on this system property.
            System.setProperty("java.net.useSystemProxies", "true");
        } catch (SecurityException exception) {
            // No-op
        }
    }

    /**
     * Creates a new web query.
     *
     * @param hostname Name of the host to contact for this web query.
     * @param port Port number on that host.
     * @param path The resource path on the host that is the target of this query.
     * @param secure A flag to say whether to use {@code http} or {@code https} as the protocol.
     * @param executorService The executor to use for running the query (in the background).
     * @throws IllegalArgumentException if the {@code URL} cannot be constructed.
     */
    public Query(final String hostname, final int port, final String path, final boolean secure,
        final ExecutorService executorService) {
        super(executorService);

        try {
            locationContext =
                new URL(secure ? Constants.HTTPS_PROTOCOL : Constants.HTTP_PROTOCOL, hostname, port, path);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Unable to construct context URL.", exception);
        }
    }

    public abstract Method getMethod();

    public String getHostname() {
        return locationContext.getHost();
    }

    public String getPath() {
        return locationContext.getFile();
    }

    public int getPort() {
        return locationContext.getPort();
    }

    public boolean isSecure() {
        String protocol = locationContext.getProtocol();
        return protocol.equalsIgnoreCase(Constants.HTTPS_PROTOCOL);
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public void setHostnameVerifier(final HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    /**
     * Gets the proxy associated with this query.
     *
     * @return This query's proxy, or {@code null} if the query is using the
     * default JVM proxy settings
     */
    public Proxy getProxy() {
        return proxy;
    }

    /**
     * Sets the proxy associated with this query.
     *
     * @param proxy This query's proxy, or {@code null} to use the default JVM
     * proxy settings
     */
    public void setProxy(final Proxy proxy) {
        this.proxy = proxy;
    }

    public URL getLocation() {
        StringBuilder queryStringBuilder = new StringBuilder();

        for (String key : parameters) {
            for (int index = 0; index < parameters.getLength(key); index++) {
                try {
                    if (queryStringBuilder.length() > 0) {
                        queryStringBuilder.append("&");
                    }

                    queryStringBuilder.append(URLEncoder.encode(key, Constants.URL_ENCODING) + "="
                        + URLEncoder.encode(parameters.get(key, index), Constants.URL_ENCODING));
                } catch (UnsupportedEncodingException exception) {
                    throw new IllegalStateException("Unable to construct query string.", exception);
                }
            }
        }

        URL location = null;
        try {
            String queryString = queryStringBuilder.length() > 0 ? "?"
                + queryStringBuilder.toString() : "";

            location = new URL(locationContext.getProtocol(), locationContext.getHost(),
                locationContext.getPort(), locationContext.getPath() + queryString);
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("Unable to construct query URL.", exception);
        }

        return location;
    }

    /**
     * Returns the web query's parameter dictionary. Parameters are passed via
     * the query string of the web query's URL.
     * @return The current set of query parameters.
     */
    public QueryDictionary getParameters() {
        return parameters;
    }

    /**
     * Returns the web query's request header dictionary. Request headers are
     * passed via HTTP headers when the query is executed.
     * @return The current set of request headers.
     */
    public QueryDictionary getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * Returns the web query's response header dictionary. Response headers are
     * returned via HTTP headers when the query is executed.
     * @return The current set of response headers.
     */
    public QueryDictionary getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Returns the status of the most recent execution.
     *
     * @return An HTTP code representing the most recent execution status.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Returns the serializer used to stream the value passed to or from the web
     * query. By default, an instance of {@link JSONSerializer} is used.
     * @return The current serializer.
     */
    public Serializer<?> getSerializer() {
        return serializer;
    }

    /**
     * Sets the serializer used to stream the value passed to or from the web
     * query.
     *
     * @param serializer The serializer (must be non-null).
     * @throws IllegalArgumentException if the input is {@code null}.
     */
    public void setSerializer(final Serializer<?> serializer) {
        Utils.checkNull(serializer, "serializer");

        this.serializer = serializer;
    }

    /**
     * Gets the number of bytes that have been sent in the body of this query's
     * HTTP request. This will only be non-zero for POST and PUT requests, as
     * GET and DELETE requests send no content to the server. <p> For POST and
     * PUT requests, this number will increment in between the
     * {@link QueryListener#connected(Query) connected} and
     * {@link QueryListener#requestSent(Query) requestSent} phases of the
     * {@code QueryListener} lifecycle methods. Interested listeners can poll
     * for this value during that phase.
     * @return Number of bytes sent by POST or PUT requests, or 0 for GET and
     * DELETE.
     */
    public long getBytesSent() {
        return bytesSent.get();
    }

    /**
     * Gets the number of bytes that have been received from the server in the
     * body of the server's HTTP response. This will generally only be non-zero
     * for GET requests, as POST, PUT, and DELETE requests generally don't
     * solicit response content from the server. <p> This number will increment
     * in between the {@link QueryListener#requestSent(Query) requestSent} and
     * {@link QueryListener#responseReceived(Query) responseReceived} phases of
     * the {@code QueryListener} lifecycle methods. Interested listeners can
     * poll for this value during that phase.
     * @return The number of bytes received.
     */
    public long getBytesReceived() {
        return bytesReceived.get();
    }

    /**
     * Gets the number of bytes that are expected to be received from the server
     * in the body of the server's HTTP response. This value reflects the
     * <code>Content-Length</code> HTTP response header and is thus merely an
     * expectation. The actual total number of bytes that will be received is
     * not known for certain until the full response has been received. <p> If
     * the server did not specify a <code>Content-Length</code> HTTP response
     * header, a value of <code>-1</code> will be returned to indicate that this
     * value is unknown.
     * @return The expected number of bytes to received based on the content length.
     */
    public long getBytesExpected() {
        return bytesExpected;
    }

    @SuppressWarnings("unchecked")
    protected Object execute(final Method method, final Object value) throws QueryException {
        Object result = value;
        URL location = getLocation();
        HttpURLConnection connection = null;

        Serializer<Object> serializerLocal = (Serializer<Object>) this.serializer;

        bytesSent.set(0);
        bytesReceived.set(0);
        bytesExpected = -1;

        status = 0;
        String message = null;

        try {
            // Clear any properties from a previous response
            responseHeaders.clear();

            // Open a connection
            if (proxy == null) {
                connection = (HttpURLConnection) location.openConnection();
            } else {
                connection = (HttpURLConnection) location.openConnection(proxy);
            }

            connection.setRequestMethod(method.toString());
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);

            if (connection instanceof HttpsURLConnection && hostnameVerifier != null) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                httpsConnection.setHostnameVerifier(hostnameVerifier);
            }

            // Set the request headers
            if (result != null) {
                connection.setRequestProperty(Constants.CONTENT_TYPE_HEADER, serializerLocal.getMIMEType(result));
            }

            for (String key : requestHeaders) {
                for (int i = 0, n = requestHeaders.getLength(key); i < n; i++) {
                    if (i == 0) {
                        connection.setRequestProperty(key, requestHeaders.get(key, i));
                    } else {
                        connection.addRequestProperty(key, requestHeaders.get(key, i));
                    }
                }
            }

            // Set the input/output state
            connection.setDoInput(true);
            connection.setDoOutput(result != null);

            // Connect to the server
            connection.connect();
            queryListeners.connected(this);

            // Write the request body
            if (result != null) {
                try (OutputStream outputStream = connection.getOutputStream()) {
                    serializerLocal.writeObject(result, new MonitoredOutputStream(outputStream));
                }
            }

            // Notify listeners that the request has been sent
            queryListeners.requestSent(this);

            // Set the response info
            status = connection.getResponseCode();
            message = connection.getResponseMessage();

            // Record the content length
            bytesExpected = connection.getContentLengthLong();

            // NOTE Header indexes start at 1, not 0
            int i = 1;
            for (String key = connection.getHeaderFieldKey(i); key != null; key = connection.getHeaderFieldKey(++i)) {
                responseHeaders.add(key, connection.getHeaderField(i));
            }

            // If the response was anything other than 2xx, throw an exception
            int statusPrefix = status / 100;
            if (statusPrefix != 2) {
                queryListeners.failed(this);
                throw new QueryException(status, message);
            }

            // Read the response body
            if (method == Method.GET && status == Query.Status.OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    result = serializerLocal.readObject(new MonitoredInputStream(inputStream));
                }
            }

            // Notify listeners that the response has been received
            queryListeners.responseReceived(this);
        } catch (IOException | SerializationException exception) {
            queryListeners.failed(this);
            throw new QueryException(exception);
        } catch (RuntimeException exception) {
            queryListeners.failed(this);
            throw exception;
        }

        return result;
    }

    /**
     * @return The query listener list.
     */
    public ListenerList<QueryListener<V>> getQueryListeners() {
        return queryListeners;
    }
}
