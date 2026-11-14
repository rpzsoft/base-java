package com.rpzsoft.http;

/**
 * Continuation passed to {@link Middleware} and {@link ErrorMiddleware}, mirroring Express'
 * {@code next()}.
 *
 * <ul>
 *   <li>{@link #next()} - run the next handler in the current layer, then subsequent layers.</li>
 *   <li>{@link #next(Throwable)} - skip normal handlers and jump to the next error handler.</li>
 *   <li>{@link #next(String)} - skip the rest of the current route/layer ("route").</li>
 * </ul>
 */
public interface Next {

    /** Value recognised by {@link #next(String)} to skip the remaining handlers of the current route. */
    String ROUTE = "route";

    void next() throws Exception;

    void next(Throwable err) throws Exception;

    void next(String route) throws Exception;
}
