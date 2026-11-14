package com.rpzsoft.service;

import com.rpzsoft.common.Json;
import com.rpzsoft.common.Vld;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 */
public class AuthService extends BaseService{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public AuthService() {

    }

    public void auth(HttpRequest req){
        if(req.headers().contains("Authorization")){
            //bearer check
            //throw new OpsRuntimeException(OpsStatus.UNAUTHORIZED);
        }
        //throw new OpsRuntimeException(OpsStatus.UNAUTHORIZED);
    }

    @Override
    public Json read(String id) throws Exception {
        Json ret = tokens(id);
        if(!Vld.isEmpty(ret))
            return new Json("value", id+":"+ret.getString("id"));
        return new Json();
    }

    public Json tokens(String id) throws Exception {
        return repo.readByKV("svc", id);
    }
}
