package com.rpzsoft.http;

import com.rpzsoft.common.Json;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A complete HTTP response.
 */
public class Response {

    public int status = HttpStatus.OK.code();
    public String contentType = "application/json; charset=utf-8";
    public byte[] body = new byte[0];
    public final Map<String, String> headers = new LinkedHashMap<String, String>();

    public static Response json(Json json) {
        Response r = new Response();
        r.contentType = "application/json; charset=utf-8";
        r.body = json.toString().getBytes(StandardCharsets.UTF_8);
        return r;
    }

    public static Response json(Object value) {
        Response r = new Response();
        r.contentType = "application/json; charset=utf-8";
        r.body = String.valueOf(value).getBytes(StandardCharsets.UTF_8);
        return r;
    }

    /** Builds a real JSON array for embedding inside another JSON object. */
    public static Json array(java.util.List<Json> items) {
        Json out = new Json(true);
        if (items != null) {
            for (Json item : items) out.add(item);
        }
        return out;
    }

    public static Response text(String text) {
        Response r = new Response();
        r.contentType = "text/plain; charset=utf-8";
        r.body = text.getBytes(StandardCharsets.UTF_8);
        return r;
    }

    public static Response html(String html) {
        Response r = new Response();
        r.contentType = "text/html; charset=utf-8";
        r.body = html.getBytes(StandardCharsets.UTF_8);
        return r;
    }

    public static Response bytes(int status, String contentType, byte[] data) {
        Response r = new Response();
        r.status = status;
        r.contentType = contentType;
        r.body = data == null ? new byte[0] : data;
        return r;
    }

    public static Response error(int status, String message) {
        Response r = new Response();
        r.status = status;
        r.contentType = "application/json; charset=utf-8";
        r.body = Json.jsonStr("error", message).getBytes(StandardCharsets.UTF_8);
        return r;
    }

    public static Response badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST.code(), message);
    }

    public static Response notFound(String message) {
        return error(HttpStatus.NOT_FOUND.code(), message);
    }

    public static Response noContent() {
        Response r = new Response();
        r.status = HttpStatus.NO_CONTENT.code();
        r.body = new byte[0];
        return r;
    }

    public static Response redirect(String location) {
        Response r = new Response();
        r.status = HttpStatus.FOUND.code();
        r.headers.put("Location", location);
        r.body = new byte[0];
        return r;
    }

    public Response with(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public static Response methodNotAllowed() {
        Response r = new Response();
        r.status = HttpStatus.METHOD_NOT_ALLOWED.code();
        r.contentType = "application/json; charset=utf-8";
        r.body = Json.jsonStr("error", "method not allowed").getBytes(StandardCharsets.UTF_8);
        return r;
    }
}
