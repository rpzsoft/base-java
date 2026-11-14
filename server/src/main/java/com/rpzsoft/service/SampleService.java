package com.rpzsoft.service;

import com.rpzsoft.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 */
public class SampleService extends BaseService{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    AuthService authService = new AuthService();

    public SampleService() {

    }


    public Json auth(Context ctx, Json req) throws Exception {
        logger.debug("auth {}", req);
        //impl here

        return new Json();
    }

    public Json token(Context ctx, Json req) throws Exception {
        logger.debug("token {}", req);
        //impl here
        return new Json();
    }

    public Json conf(Context ctx, Json req) throws Exception {
        logger.debug("conf{}", req);
        //impl here
        return new Json();
    }

    public Json invoke(Context ctx, Json req) throws Exception{
        logger.debug("invoke {}", req);
        //impl here
        return new Json();
    }

    Json getConf(Json req){
        Json conf = (Json) req.clone();
        conf.put("operation", req.getString("operation"));
        List<Object> prms = req.getArray("params");
        if (prms != null) {
            for (Object p : prms) {
                Json prm = (Json) p;
                if(prm.getString("type").equals("button")) continue;

                conf.put(prm.getString("key"), prm.get("value"));
            }
            //TODO: this could remove param with that name
            //remove params as we have flattened it:
            conf.remove("params");
        }

        return conf;
    }
}
