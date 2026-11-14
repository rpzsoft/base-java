package com.rpzsoft.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public interface RpzException {
    Logger logger = LoggerFactory.getLogger(RpzException.class);

    <T> T getStatus();
    OpsStatus getOpsStatus();
    String getContentType();
    Json getJsonMsg();
    String getJsonStrMsg();

    //predict error here
    static String getErr(String err, boolean ...nowrap){
        if(Vld.isEmpty(err)) return  getMsg("UNKNOWN", nowrap);
        err = err.trim();
        if(err.startsWith("{") || err.startsWith("[")) {
            try {
                Json e = Json.fromJson(err);
                if(e.get("err") != null) return err;
                Object oerr;
                StringBuilder emsg= new StringBuilder();
                int size = e.entrySet().size();
                for(Map.Entry<String, Object> entry: e.entrySet()){
                    String key = entry.getKey();
                    Object v = entry.getValue();
                    if(key.equals("msg") || key.equals("err") || key.equals("error") || key.equals("errors")
                            || key.equals("message") || key.equals("code")) {
                        if( v instanceof Json){
                            emsg.append(v.toString());
                            /*for(Map.Entry<String, Object> ve: ((Json)v).entrySet())
                                emsg.append(ve.getKey()).append("= ").append(ve.getValue()).append("; \n");*/
                        }else {
                            emsg.append(key).append("= ").append(v).append("; \n");
                        }
                    }
                }
                return getMsg(emsg.toString(), nowrap);
            } catch (Exception e1) {
                //
            }
        }

        return  getMsg(err, nowrap);
    }

    //whether to wrap err in a json {err:msg}
    static String getMsg(String msg, boolean ...nowrap){
        return nowrap.length > 0 && nowrap[0]?  msg : Json.jsonStr("err", msg);
    }

    static String getErr(Exception e){
        if(e.getCause()  != null){
            return e.getCause().getMessage();
        }
        return e.getMessage();
    }
}
