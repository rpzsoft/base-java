

package com.rpz.http;


import com.rpz.common.OpsStatus;

/**
 * HTTP Status codes as per https://tools.ietf.org/html/rfc7231#section-6.1
 */
public enum  HttpStatus {

    /** Informational 1xx */
    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    PROCESSING(102, "Processing"),  //e.g. taking more than 20 seconds to process
    //EARLY_HINTS(103, "Early Hints"),

    /** Successful 2xx */
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    //NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    NO_CONTENT(204, "No Content"),
    RESET_CONTENT(205, "Reset Content"),
    PARTIAL_CONTENT(206, "Partial Content"),

    /** Redirection 3xx */
    MULTIPLE_CHOICES(300, "Multiple Choices"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    USE_PROXY(305, "Use Proxy"),
    /** 306 (Unused) */
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),

    /** Client Error 4xx */
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
    URI_TOO_LONG(414, "URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "Expectation Failed"),
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    PRECONDITION_REQUIRED(428, "Precondition Required"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),

    /** Server Error 5xx */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    UNKNOWN(520, "Unknown Error");

    int code;
    String status;

    public int code() {
        return code;
    }

    HttpStatus(int code, String status) {
        this.code = code;
        this.status = status;
    }

    public boolean equals(String statusline){
        return toString().equalsIgnoreCase(statusline);
    }

    @Override
    public String toString() {
        return (code + " " + status);
    }

    public boolean isError(){
        return isError(code);
    }

    public boolean isSuccess(){
        return isSuccess(code);
    }

    public static boolean isSuccess(int code){
        int mod = code % 200;
        return 0 <= mod && mod <= 31 ;
    }

    public boolean isClientError(){
        return isClientError(code);
    }

    public static boolean isClientError(int code){
        int mod = code % 400;
        return 0 <= mod && mod <= 31 ;
    }

    public boolean  isServerError(){
        return isServerError(code);
    }

    public static boolean  isServerError(int code){
        int mod = code % 500;
        return 0 <= mod && mod <= 31 ;
    }

    public static boolean isNotFound(int code){
        return code == 404;
    }

    public static boolean  isRedirect(int code){
        int mod = code % 300;
        return 0 <= mod && mod <= 7 ;
    }


    public static boolean isError(int code){
        /*int mod = code % 500;
        boolean se = 0 <= mod && mod <= 5;
        mod = code % 400;
        boolean ce = 0 <= mod && mod <= 26 ;*/
        return isServerError(code) || isClientError(code);
    }

    public static HttpStatus valueOf(int code){
        switch (code){
            case 100: return CONTINUE;
            case 101: return SWITCHING_PROTOCOLS;
            case 102: return PROCESSING;

            /** Successful 2xx */
            case 200: return OK;
            case 201: return CREATED;
            case 202: return ACCEPTED;
            case 204: return NO_CONTENT;
            case 205: return RESET_CONTENT;
            case 206: return PARTIAL_CONTENT;

            /** Redirection 3xx */
            case 300: return MULTIPLE_CHOICES;
            case 301: return MOVED_PERMANENTLY;
            case 302: return FOUND;
            case 303: return SEE_OTHER;
            case 304: return NOT_MODIFIED;
            case 305: return USE_PROXY;
            /** 306 (Unused) */
            case 307: return TEMPORARY_REDIRECT;

            /** Client Error 4xx */
            case 400: return BAD_REQUEST;
            case 401: return UNAUTHORIZED;
            case 402: return PAYMENT_REQUIRED;
            case 403: return FORBIDDEN;
            case 404: return NOT_FOUND;
            case 405: return METHOD_NOT_ALLOWED;
            case 406: return NOT_ACCEPTABLE;
            case 407: return PROXY_AUTHENTICATION_REQUIRED;
            case 408: return REQUEST_TIMEOUT;
            case 409: return CONFLICT;
            case 410: return GONE;
            case 411: return LENGTH_REQUIRED;
            case 412: return PRECONDITION_FAILED;
            case 413: return PAYLOAD_TOO_LARGE;
            case 414: return URI_TOO_LONG;
            case 415: return UNSUPPORTED_MEDIA_TYPE;
            case 416: return RANGE_NOT_SATISFIABLE;
            case 417: return EXPECTATION_FAILED;
            case 426: return UPGRADE_REQUIRED;
            case 428: return PRECONDITION_REQUIRED;
            case 429: return TOO_MANY_REQUESTS;
            case 431: return REQUEST_HEADER_FIELDS_TOO_LARGE;

            /** Server Error 5xx */
            case 500: return INTERNAL_SERVER_ERROR;
            case 501: return NOT_IMPLEMENTED;
            case 502: return BAD_GATEWAY;
            case 503: return SERVICE_UNAVAILABLE;
            case 504: return GATEWAY_TIMEOUT;
            case 505: return HTTP_VERSION_NOT_SUPPORTED;
        }
        return UNKNOWN;
    }

    public static HttpStatus valueOf(OpsStatus status){

//        logger.info("status: {}", status.toString());
        switch (status){
            /** Informational 1xx */
            case CONTINUE: return HttpStatus.CONTINUE;
            case SWITCHING_PROTOCOLS: return HttpStatus.SWITCHING_PROTOCOLS;
            case PROCESSING: return HttpStatus.PROCESSING;

            /** Successful 2xx */
            case OK: return HttpStatus.OK;
            case CREATED: return HttpStatus.CREATED;
            case ACCEPTED: return HttpStatus.ACCEPTED;
            case CHANGED:
            case NO_CONTENT: return HttpStatus.NO_CONTENT;
            case FOUND: return HttpStatus.FOUND;

            /** Redirection 3xx */
            case NOT_CHANGED: return HttpStatus.NOT_MODIFIED;

            /** Client Error 4xx */
            case BAD_REQUEST: return HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED: return HttpStatus.UNAUTHORIZED;
            case FORBIDDEN: return HttpStatus.FORBIDDEN;
            case NOT_FOUND: return HttpStatus.NOT_FOUND;
            case PRECONDITION_FAILED: return HttpStatus.PRECONDITION_FAILED;
            case NOT_ACCEPTABLE: return HttpStatus.NOT_ACCEPTABLE;
            case REQUEST_TIMEOUT: return HttpStatus.REQUEST_TIMEOUT;
            case CONFLICT: return HttpStatus.CONFLICT;
            case GONE: return HttpStatus.GONE;
            case INVALID_CONTENT_SIZE: return HttpStatus.LENGTH_REQUIRED;
            case PAYLOAD_TOO_LARGE: return HttpStatus.PAYLOAD_TOO_LARGE;
            case UNSUPPORTED_MEDIA_TYPE: return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            case TOO_MANY_REQUESTS: return HttpStatus.TOO_MANY_REQUESTS;

            /** Server Error 5xx */
            case INTERNAL_SERVER_ERROR: return HttpStatus.INTERNAL_SERVER_ERROR;
            case BAD_GATEWAY: return HttpStatus.BAD_GATEWAY;
            case SERVICE_UNAVAILABLE: return HttpStatus.SERVICE_UNAVAILABLE;
            case GATEWAY_TIMEOUT: return HttpStatus.GATEWAY_TIMEOUT;
            case INVALID_VERSION: return HttpStatus.HTTP_VERSION_NOT_SUPPORTED;
            case UNKNOWN: return HttpStatus.UNKNOWN;
        }
        return HttpStatus.UNKNOWN;
    }
}