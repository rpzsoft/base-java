package com.rpzsoft.common;

import com.rpzsoft.handler.ReqHandler;
import com.rpzsoft.http.HttpMethod;
import com.rpzsoft.http.HttpStatus;
import com.rpzsoft.http.Response;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.HashMap;
import java.util.List;

public class Context extends HashMap<String, Object> {
    public transient ReqHandler handler;
    public transient HttpMethod method;
    public transient HttpHeaders headers;
    public transient HttpRequest req;
    public transient HttpResponse res;
    public transient Json params;
    public transient List<String> pnames; //order matters use to distinguish read vs find
    public transient HttpStatus status = HttpStatus.OK;

    public String path;
    public String query;
    /** Path prefix this context has been mounted under (Express' req.baseUrl). */
    public String baseUrl = "";

    public Json icapp;
    public Json conf;
    public Json state;
    public Json in;
    public Json out;
    /** Full response produced by middleware via {@link #send(Response)}; takes precedence over {@link #out}. */
    public transient Response response;

    public Context(String method, String path) {
        this.method = HttpMethod.valueOf(method.toUpperCase());
        this.path = path;
    }

    public Context(String method, String path, ReqHandler handler) {
        this.method = HttpMethod.valueOf(method.toUpperCase());
        this.path = path;
        this.handler = handler;
    }

    public Context(String method, String path, ReqHandler handler, List<String> pnames, Json params) {
        this.method = HttpMethod.valueOf(method.toUpperCase());
        this.path = path;
        this.handler = handler;
        this.params = params;
        this.pnames = pnames;
    }

    public String getParam(String pname) throws OpsException {
        if(!Vld.isEmpty(params)){
            String value = params.getString(pname);
            if(value != null) return value;
        }
        throw new OpsException(OpsStatus.BAD_REQUEST, "Parameter "+pname+" is required");
    }

    public String endingPrm() throws OpsException {
        if(!Vld.isEmpty(params)){
            final String lp = pnames.get(pnames.size() - 1);
            final String lpv = getParam(lp);
            if(path.endsWith(lpv)) return lpv;
        }
        return null;
    }

    /** Route parameter value, or {@code null} when absent. */
    public String param(String name) {
        return Vld.isEmpty(params) ? null : params.getString(name);
    }

    /** Express-style fluent status setter. */
    public Context status(HttpStatus status) {
        this.status = status;
        return this;
    }

    /** Express-style fluent response body setter. */
    public Context send(Json body) {
        this.out = body;
        return this;
    }

    /** Express-style fluent full-response setter (status, content type, headers and body). */
    public Context send(Response response) {
        this.response = response;
        return this;
    }
}
