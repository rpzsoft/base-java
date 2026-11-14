package com.rpzsoft.http;

import java.util.ArrayList;
import java.util.List;

/**
 * A single entry in a {@link Router}'s ordered stack: a path matcher, an optional HTTP method
 * ({@code null} = any, used by middleware) and the handlers registered for it.
 */
final class Layer {

    final PathMatcher matcher;
    /** {@code null} means "any method" (middleware registered with {@link Router#use}). */
    final HttpMethod method;
    /** {@code true} for middleware layers, which match on a path prefix instead of the whole path. */
    final boolean prefix;
    final List<Object> handlers;
    final boolean hasError;

    Layer(String path, HttpMethod method, boolean prefix, boolean caseSensitive, List<Object> handlers) {
        this.matcher = PathMatcher.compile(path, caseSensitive, prefix);
        this.method = method;
        this.prefix = prefix;
        this.handlers = new ArrayList<>(handlers);
        boolean err = false;
        for (Object h : this.handlers) {
            if (h instanceof ErrorMiddleware) {
                err = true;
                break;
            }
        }
        this.hasError = err;
    }
}
