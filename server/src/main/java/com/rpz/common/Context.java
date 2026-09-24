package com.rpz.common;

import com.rpz.handler.ReqHandler;
import com.rpz.http.HttpMethod;
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

    public String path;
    public String query;

    public Json icapp;
    public Json conf;
    public Json state;
    public Json in;
    public Json out;

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
}
