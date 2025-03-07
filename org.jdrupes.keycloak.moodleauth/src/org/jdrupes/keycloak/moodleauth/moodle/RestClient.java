/*
 * This file is part of the Keycloak Moodle authenticator
 * Copyright (C) 2024 Michael N. Lipp
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package org.jdrupes.keycloak.moodleauth.moodle;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdrupes.keycloak.moodleauth.moodle.model.MoodleErrorValues;
import org.jdrupes.keycloak.moodleauth.moodle.service.QueryValueEncoder;

/**
 * A class for invoking REST services.
 */
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class RestClient implements AutoCloseable {

    @SuppressWarnings("PMD.FieldNamingConventions")
    private static final Logger logger
        = Logger.getLogger(RestClient.class.getName());
    protected static final ObjectMapper mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private HttpClient httpClient;
    private Map<String, Object> defaultParams;
    private URI uri;

    /**
     * Instantiates a new rest client.
     *
     * @param uri the uri
     * @param defaultParams the default params
     */
    public RestClient(URI uri, Map<String, Object> defaultParams) {
        createHttpClient();
        this.uri = uri;
        this.defaultParams = new HashMap<>(defaultParams);
    }

    /**
     * Instantiates a new rest client.
     *
     * @param uri the uri
     */
    public RestClient(URI uri) {
        this(uri, Collections.emptyMap());
    }

    /**
     * @param uri the uri to set
     */
    @SuppressWarnings("PMD.LinguisticNaming")
    public RestClient setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    /**
     * @return the uri
     */
    public URI uri() {
        return uri;
    }

    /**
     * Sets the default params.
     *
     * @param params the params
     * @return the rest client
     */
    @SuppressWarnings("PMD.LinguisticNaming")
    public RestClient setDefaultParams(Map<String, Object> params) {
        this.defaultParams = new HashMap<>(params);
        return this;
    }

    @Override
    public void close() throws Exception {
        httpClient = null;
    }

    private void createHttpClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20)).build();
    }

    /**
     * Invoke a request with the parameters specified.
     *
     * @param <T> the generic type
     * @param resultType the result type
     * @param queryParams parameters to be added to the query
     * @param data to be send in the body
     * @return the result
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings({ "PMD.GuardLogStatement", "PMD.AvoidDuplicateLiterals",
        "PMD.AvoidRethrowingException" })
    public <T> T invoke(Class<T> resultType, Map<String, Object> queryParams,
            Map<String, Object> data) throws IOException {
        try {
            var query = Stream.concat(defaultParams.entrySet().stream(),
                queryParams.entrySet().stream())
                .map(e -> URLEncoder.encode(e.getKey(),
                    Charset.forName("utf-8")) + "="
                    + URLEncoder.encode(e.getValue().toString(),
                        Charset.forName("utf-8")))
                .collect(Collectors.joining("&"));
            var formData = encodeData(data);
            for (int attempt = 0; attempt < 10; attempt++) {
                try {
                    return doInvoke(resultType, query, formData);
                } catch (MoodleException e) {
                    if ("ex_unabletolock".equals(e.getMessage())) {
                        logger.log(Level.FINE, e,
                            () -> "Retrying due to: " + e.getMessage()
                                + " with query params " + queryParams);
                        continue;
                    }
                    throw e;
                } catch (IOException e) {
                    logger.log(Level.FINE, e,
                        () -> "Reconnecting due to: " + e.getMessage());
                }
                createHttpClient();
                Thread.sleep(1000);
            }
            // Final attempt
            return doInvoke(resultType, query, formData);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    /**
     * Encodes the map following the non-standard conventions of
     * PHP's `http_build_query`
     *
     * @param data the data
     * @return the query string
     * @see https://github.com/pear/PHP_Compat/blob/master/PHP/Compat/Function/http_build_query.php
     */
    public static String encodeData(Map<String, Object> data) {
        return encodeStream(data.entrySet().stream(), null);
    }

    /**
     * Used by {@link #encodeData(Map)} and recursively invoked as required.
     *
     * @param data a stream of entries
     * @param keyBase the key base of `null` for the top-lebel invocation 
     * @return the query string
     */
    @SuppressWarnings({ "unchecked", "PMD.CognitiveComplexity" })
    public static String encodeStream(Stream<Map.Entry<String, Object>> data,
            String keyBase) {
        // Iterate over all entries in stream.
        return data.map(e -> {
            // Use entry's key as result's key or as "index" of existing key.
            String key;
            if (keyBase == null) {
                key = e.getKey();
            } else {
                key = keyBase + '[' + e.getKey() + ']';
            }
            if (e.getValue() instanceof Map) {
                return encodeStream(
                    ((Map<String, Object>) e.getValue()).entrySet().stream(),
                    key);
            }
            Stream<Object> valueStream = null;
            if (e.getValue().getClass().isArray()) {
                valueStream = Stream.of((Object[]) e.getValue());
            } else if (e.getValue() instanceof Collection) {
                valueStream = ((Collection<Object>) e.getValue()).stream();
            }
            if (valueStream != null) {
                AtomicInteger counter = new AtomicInteger();
                return encodeStream(valueStream.map(
                    v -> new AbstractMap.SimpleEntry<>(
                        Integer.toString(counter.getAndIncrement()), v)),
                    key);
            }
            StringBuilder res = new StringBuilder()
                .append(URLEncoder.encode(key, Charset.forName("utf-8")))
                .append('=');
            if (e.getValue() instanceof QueryValueEncoder) {
                res.append(
                    ((QueryValueEncoder) e.getValue()).asQueryValue());
            } else {
                res.append(URLEncoder.encode(e.getValue().toString(),
                    Charset.forName("utf-8")));
            }
            return res.toString();
        }).collect(Collectors.joining("&"));
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private <T> T doInvoke(Class<T> resultType, String query, String formData)
            throws IOException, InterruptedException {
        URI fullUri;
        try {
            fullUri = new URI(uri.getScheme(), uri.getAuthority(),
                uri.getPath(), null, null);
            fullUri = new URI(fullUri.toString()
                + (query == null ? "" : "?" + query)
                + (uri.getRawFragment() == null ? ""
                    : "#" + uri.getRawFragment()));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        HttpRequest request = HttpRequest.newBuilder().uri(fullUri)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formData)).build();

        // Execute and get the response.
        HttpResponse<InputStream> response
            = httpClient.send(request, BodyHandlers.ofInputStream());
        if (response.body() == null) {
            return null;
        }

        try (var resultData = new PushbackReader(
            new InputStreamReader(response.body(), "utf-8"), 8)) {
            if (resultType.isArray()) {
                // Errors for requests returning an array are
                // reported as JSON object.
                char[] peekData = new char[2];
                int peeked = resultData.read(peekData, 0, peekData.length);
                resultData.unread(peekData, 0, peeked);
                if (peeked > 0 && peekData[0] != '[') {
                    mapper.readValue(resultData, MoodleErrorValues.class);
                    throw new MoodleException(
                        mapper.readValue(resultData, MoodleErrorValues.class));
                }
            }
            return mapper.readValue(resultData, resultType);
        } catch (IOException e) {
            throw new IOException("Unparsable result: " + e.getMessage(), e);
        }
    }
}
