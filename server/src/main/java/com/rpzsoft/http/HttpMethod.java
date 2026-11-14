

package com.rpzsoft.http;


public enum HttpMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE"),
    CONNECT("CONNECT"),

    /** for internal use */
    ANY("ANY"),
    UNDEFINED("UNDEFINED"),
    METHOD("METHOD");

    private String method;

    HttpMethod(String method) {
        this.method = method;
    }

    public static HttpMethod valueOf(OpsMethod method){
        switch (method){
            case READ: return GET;
            case WRITE: return POST;
            case UPDATE: return PUT;
            case DELETE: return DELETE;
            case EXEC: return POST;
            case PATCH: return PATCH;
            case INFO: return OPTIONS;
            case TUN: return POST;
            case SES: return POST;

        }
        return UNDEFINED;
    }

    public String lmethod() {
        return method.toLowerCase();
    }
}
