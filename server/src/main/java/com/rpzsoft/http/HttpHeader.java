

package com.rpzsoft.http;


import com.rpzsoft.common.Json;
import com.rpzsoft.common.Pair;

import java.util.List;

/**
 * 
 *
 * it is good practice to send header fields that contain control data first, such as Host on requests and Date on
 * responses, so that implementations can decide when not to handle a message as early as possible.
 *
 * Each header field consists of a case-insensitive field name followed by a colon (":"), optional leading whitespace,
 * the field value, and optional trailing whitespace.
 *
 * sender MUST NOT generate multiple header fields with the same field name in a message unless either the entire field
 * value for that header field is defined as a comma-separated list [i.e., #(values)] or the header field is a
 * well-known exception (as noted in rfc7230).
 */
public enum HttpHeader {

    ACCEPT("Accept"),
    ACCEPT_CHARSET("Accept-Charset"),
    ACCEPT_ENCODING("Accept-Encoding"),
    ACCEPT_LANGUAGE("Accept-Language"),
    ACCEPT_RANGES("Accept-Ranges"),
    AGE("Age"),
    ALLOW("Allow"),
    AUTHORIZATION("Authorization"), /** e.g. Basic base64_encoded_username:password | Bearer 'token' */
    RPZ_AUTHORIZATION("x-rpz-auth"),/* base64_encoded token */
    CACHE_CONTROL("Cache-Control"),
    CONNECTION("Connection"),
    CONTENT_ENCODING("Content-Encoding"),
    CONTENT_LANGUAGE("Content-Language"),
    CONTENT_LENGTH("Content-Length"),
    CONTENT_LOCATION("Content-Location"),
    CONTENT_MD5("Content-MD5"),
    CONTENT_RANGE("Content-Range"),
    CONTENT_TYPE("Content-Type"),
    DATE("Date"),
    ETAG("ETag"),
    EXPECT("Expect"),
    EXPIRES("Expires"),

    /**-standard-https://tools.ietf.org/html/rfc7239-;non-standard:-X_Forwarded_For,-X_Forwarded_By,-X_Forwarded_Proto*/
    FORWARDED("Forwarded"),
    X_FORWARDED("X-Forwarded-For"),
    X_FORWARDED_PORT("X-Forwarded-Port"),
    X_FORWARDED_PROTO("X-Forwarded-Proto"),
    FROM("From"),
    HOST("Host"),
    IF_MATCH("If-Match"),
    IF_MODIFIED_SINCE("If-Modified-Since"),
    IF_NONE_MATCH("If-None-Match"),
    IF_RANGE("If-Range"),
    IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
    LAST_MODIFIED("Last-Modified"),
    LOCATION("Location"),
    MAX_FORWARDS("Max-Forwards"),
    ORIGIN("Origin"),
    PRAGMA("Pragma"),
    PROXY_AUTHENTICATE("Proxy-Authenticate"),
    PROXY_AUTHORIZATION("Proxy-Authorization"),
    RANGE("Range"),
    REFERER("Referer"),
    RETRY_AFTER("Retry-After"),
    SERVER("Server"),
    TE("TE"),
    TRAILER("Trailer"),
    TRANSFER_ENCODING("Transfer-Encoding"),
    UPGRADE("Upgrade"),
    USER_AGENT("User-Agent"),
    VARY("Vary"),
    VIA("Via"),
    WARNING("Warning"),
    WWW_AUTHENTICATE("WWW-Authenticate");

    private String header;

    HttpHeader(String header) {
        this.header = header;
    }

    public boolean equals(String oheader) {
        return header.equalsIgnoreCase(oheader);
    }

    public boolean equals(HttpHeader other) {
        return header.equalsIgnoreCase(other.header);
    }

    public String str() {
        return header;
    }

    @Override
    public String toString() {
        return header;
    }

    //we remove if any token on the URI for logging purpose
    public static String rmSensitive(String suri){
        String ruri = suri;
        int sttk = suri.indexOf("?"); //just to be sure
        if(sttk > 0){
            int tklen = "access_token=".length();
            sttk = suri.indexOf("access_token=", sttk + 1);
            if(sttk < 0) {
                tklen = "token=".length();
                sttk = suri.indexOf("token=", sttk + 1);
            }
            if(sttk > 0){
                int entk = suri.indexOf("&", sttk + tklen);
                if(entk < 0) entk = suri.length();
                suri = suri.substring(0, sttk+tklen)+"..."+suri.substring(entk);
                ruri = suri;
            }
            if(suri.charAt(suri.length() - 1) == '?')
                ruri = suri.substring(0, suri.length()-1);
        }
        return ruri;
    }

    public static Json rmSensitive(List<Pair<String, String>> headers){
        final Json ret = new Json();
        if(headers == null) return ret;
        for(Pair<String, String> h: headers){
            //following is for security reasons
            switch (h.key.toLowerCase()){
                case "x-forwarded-for":
                case "authorization": continue;
            }
            ret.put(h.key, h.value);
        }
        return ret;
    }
}
