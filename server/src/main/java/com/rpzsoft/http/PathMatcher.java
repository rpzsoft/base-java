package com.rpzsoft.http;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compiles an Express-style route path into a {@link Pattern} and matches request paths against it.
 *
 * <p>Supported syntax:
 * <ul>
 *   <li>literal segments: {@code /users/list}</li>
 *   <li>required param: {@code /users/:id}</li>
 *   <li>optional param: {@code /users/:id?}</li>
 *   <li>zero-or-more param: {@code /files/:path*}</li>
 *   <li>one-or-more param: {@code /files/:path+}</li>
 *   <li>custom pattern: {@code /users/:id(\\d+)}</li>
 *   <li>splat/wildcard: {@code /static/*} or named {@code /static/*rest}</li>
 * </ul>
 *
 * <p>A matcher is compiled either for "exact" route matching (the whole path must be consumed,
 * with an optional trailing slash) or for "prefix" matching used by middleware
 * ({@link Router#use}), where the match must end on a segment boundary.
 */
final class PathMatcher {

    private final Pattern pattern;
    private final boolean prefix;
    private final Map<Integer, String> groupNames;

    private PathMatcher(Pattern pattern, boolean prefix, Map<Integer, String> groupNames) {
        this.pattern = pattern;
        this.prefix = prefix;
        this.groupNames = groupNames;
    }

    static PathMatcher compile(String path, boolean caseSensitive, boolean prefix) {
        if (path == null || path.isEmpty()) path = "/";

        StringBuilder rx = new StringBuilder("^");
        Map<Integer, String> groups = new LinkedHashMap<>();
        int gc = 0;

        List<String> tokens = new ArrayList<>();
        for (String t : path.split("/")) {
            if (!t.isEmpty()) tokens.add(t);
        }

        if (tokens.isEmpty()) {
            // root: as middleware it matches every path (consuming nothing), as a route only "/".
            rx.append(prefix ? "" : "/?$");
        } else {
            int anonymousWildcard = 0;
            for (String t : tokens) {
                if (t.startsWith("*")) {
                    String name = t.length() > 1 ? t.substring(1) : String.valueOf(anonymousWildcard++);
                    String piece = "(?:/(.*))?";
                    rx.append(piece);
                    gc += countCaptures(piece);
                    groups.put(gc, name);
                } else if (t.startsWith(":")) {
                    Param p = parseParam(t);
                    String base = p.regex != null ? "(" + p.regex + ")" : "([^/]+)";
                    String piece;
                    switch (p.mod) {
                        case '?':
                            piece = "(?:/" + base + ")?";
                            break;
                        case '*':
                            piece = "(?:/(.*))?";
                            break;
                        case '+':
                            piece = "(?:/(.+))";
                            break;
                        default:
                            piece = "/" + base;
                    }
                    int before = gc;
                    gc += countCaptures(piece);
                    groups.put(before + 1, p.name);
                    rx.append(piece);
                } else {
                    rx.append("/").append(Pattern.quote(t));
                }
            }
            if (!prefix) rx.append("/?$");
        }

        int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
        return new PathMatcher(Pattern.compile(rx.toString(), flags), prefix, groups);
    }

    Match match(String path) {
        if (path == null || path.isEmpty()) path = "/";
        Matcher m = pattern.matcher(path);
        boolean ok;
        if (prefix) {
            ok = m.lookingAt();
            if (ok) {
                int end = m.end();
                if (end < path.length() && path.charAt(end) != '/') ok = false;
            }
        } else {
            ok = m.matches();
        }
        if (!ok) return null;

        int consumed = prefix ? m.end() : path.length();
        List<String> names = new ArrayList<>(groupNames.size());
        List<String> values = new ArrayList<>(groupNames.size());
        for (Map.Entry<Integer, String> e : groupNames.entrySet()) {
            String v = m.group(e.getKey());
            if (v != null) {
                names.add(e.getValue());
                values.add(decode(v));
            }
        }
        return new Match(consumed, names, values);
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }

    private static Param parseParam(String token) {
        String body = token.substring(1);
        String name;
        char mod = 0;
        String regex = null;

        int open = body.indexOf('(');
        if (open >= 0) {
            int close = body.lastIndexOf(')');
            if (close > open) {
                name = body.substring(0, open);
                regex = body.substring(open + 1, close);
                if (close + 1 < body.length()) {
                    char c = body.charAt(close + 1);
                    if (c == '?' || c == '*' || c == '+') mod = c;
                }
            } else {
                name = body;
            }
        } else {
            int end = body.length();
            for (int i = 0; i < body.length(); i++) {
                char c = body.charAt(i);
                if (c == '?' || c == '*' || c == '+') {
                    end = i;
                    mod = c;
                    break;
                }
            }
            name = body.substring(0, end);
        }
        return new Param(name, mod, regex);
    }

    private static int countCaptures(String s) {
        int c = 0;
        boolean escaped = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (ch == '\\') {
                escaped = true;
                continue;
            }
            if (ch == '(') {
                if (i + 1 < s.length() && s.charAt(i + 1) == '?') continue;
                c++;
            }
        }
        return c;
    }

    private static final class Param {
        final String name;
        final char mod;
        final String regex;

        Param(String name, char mod, String regex) {
            this.name = name;
            this.mod = mod;
            this.regex = regex;
        }
    }

    /** Result of a successful match. */
    static final class Match {
        /** Number of leading characters of the request path consumed by the pattern. */
        final int consumed;
        final List<String> names;
        final List<String> values;

        Match(int consumed, List<String> names, List<String> values) {
            this.consumed = consumed;
            this.names = names;
            this.values = values;
        }
    }
}
