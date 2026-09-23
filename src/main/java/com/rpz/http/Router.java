package com.rpz.http;

import com.rpz.common.Context;
import com.rpz.common.Json;
import com.rpz.handler.ReqHandler;
import com.rpz.handler.ReqHandlerBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Express.js inspired HTTP router
 */
public class Router {
    static final Map<String, Map<String, ReqHandler>> rtHandlers = new ConcurrentHashMap<>();
//    static final Map<String, ReqHandler> handlers = new ConcurrentHashMap<>();

    //simple url pattern matcher
    public static Context match(String method, String path){
        Map<String, ReqHandler> handlers = rtHandlers.get(method.toLowerCase());

        //need to remove query string, as it is not a part of the path
        int loc = path.indexOf('?');
        if(loc >= 0) path = path.substring(0, loc);
        Json params = new Json();
        List<String> pnames = new ArrayList<>();
        if(handlers == null || handlers.isEmpty()) return null;

        for(Map.Entry<String, ReqHandler> req: handlers.entrySet()){
            final String hpath = req.getKey();
            int plen = path.length();
            int hplen = hpath.length();
            boolean matched= true;

            if(hpath.equals("*")) {
                return new Context(method, path, req.getValue());
            }

            for(int i = 0, j=0; plen > 0 && hplen > 0; i++, j++) {
                if(hpath.charAt(j) == ':') {
                    int idx = path.indexOf('/', i);
                    String pv = (idx < 0)? path.substring(i): path.substring(i, idx);
                    plen -= pv.length();
                    i = idx;

                    idx = hpath.indexOf('/', ++j);
                    String pk = (idx < 0)? hpath.substring(j): hpath.substring(j, idx);
                    hplen -= pk.length() + 1;//tbd one more for ':'
                    j = idx;
                    params.put(pk, pv);
                    pnames.add(pk);
                } else if(path.charAt(i) != hpath.charAt(j)) {
                    matched = false;
                    break;
                }
                plen--; hplen--;
            }
            if(!matched) continue;

            if(plen <= 0 && hplen <= 0) {
                return new Context(method, path, req.getValue(), pnames, params);
            }
        }
        return null;
    }

    public static void get(String path, ReqHandler handler){register(HttpMethod.GET.lmethod(), path, handler);}
    public static void post(String path, ReqHandler handler){
        register(HttpMethod.POST.lmethod(), path, handler);
    }
    public static void put(String path, ReqHandler handler){
        register(HttpMethod.PUT.lmethod(), path, handler);
    }
    public static void delete(String path, ReqHandler handler){
        register(HttpMethod.DELETE.lmethod(), path, handler);
    }
    public static void patch(String path, ReqHandler handler){
        register(HttpMethod.PATCH.lmethod(), path, handler);
    }
    public static void options(String path, ReqHandler handler){register(HttpMethod.OPTIONS.lmethod(), path, handler);}
    public static void head(String path, ReqHandler handler){
        register(HttpMethod.HEAD.lmethod(), path, handler);
    }

    public static void all(String path, ReqHandler handler){
        get(path, handler);
        post(path, handler);
        put(path, handler);
        delete(path, handler);
        patch(path, handler);
        options(path, handler);
        head(path, handler);
    }

    private static void register(String method, String path, ReqHandler handler){
        Map<String, ReqHandler> handlers = rtHandlers.get(method);
        if(handlers == null) handlers = new ConcurrentHashMap<>();
        handlers.put(path, handler);
        rtHandlers.put(method, handlers);
    }

    static {
        options("*", new ReqHandlerBase());
    }

}
