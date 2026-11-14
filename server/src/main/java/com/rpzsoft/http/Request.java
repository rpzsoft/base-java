package com.rpzsoft.http;

import com.rpzsoft.common.Json;
import io.netty.buffer.ByteBuf;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parsed HTTP request handed to route handlers.
 */
public class Request {

    private final HttpMethod method;
    private final String path;
    private final String rawQuery;
    private final Map<String, String> query = new LinkedHashMap<String, String>();
    private final Map<String, String> headers = new LinkedHashMap<String, String>();
    private final Map<String, String> params = new LinkedHashMap<String, String>();
    private final byte[] body;
    private Json json;
    private String userId;

    public Request(HttpMethod method, String uri, Map<String, String> headers, ByteBuf content) throws Exception {
        this.method = method;
        int q = uri.indexOf('?');
        String rawPath = q >= 0 ? uri.substring(0, q) : uri;
        this.path = rawPath;
        this.rawQuery = q >= 0 ? uri.substring(q + 1) : "";
        if (q >= 0) parseQuery(this.rawQuery);
        for (Map.Entry<String, String> e : headers.entrySet()) {
            headers.put(e.getKey(), e.getValue());
        }
        this.body = new byte[content.readableBytes()];
        content.getBytes(content.readerIndex(), this.body);
    }

    private void parseQuery(String qs) throws Exception {
        for (String pair : qs.split("&")) {
            if (pair.isEmpty()) continue;
            int i = pair.indexOf('=');
            String k = i >= 0 ? pair.substring(0, i) : pair;
            String v = i >= 0 ? pair.substring(i + 1) : "";
            query.put(URLDecoder.decode(k, "UTF-8"), URLDecoder.decode(v, "UTF-8"));
        }
    }

    public HttpMethod method() {
        return method;
    }

    public String path() {
        return path;
    }

    public String query(String name) {
        return query.get(name);
    }

    public String queryString() {
        return rawQuery;
    }

    public String header(String name) {
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (e.getKey().equalsIgnoreCase(name)) return e.getValue();
        }
        return null;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public String bodyString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public byte[] body() {
        return body;
    }

    public Json json() throws Exception {
        if (json == null) {
            json = body.length == 0 ? new Json() : Json.fromJson(body);
        }
        return json;
    }

    public String param(String name) {
        return params.get(name);
    }

    public String userId() {
        return userId;
    }

    public void userId(String userId) {
        this.userId = userId;
    }

    public void param(String name, String value) {
        params.put(name, value);
    }

    public String contentType() {
        String c = header("Content-Type");
        return c == null ? "" : c;
    }
}
