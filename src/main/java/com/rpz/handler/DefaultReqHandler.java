package com.rpz.handler;


import com.rpz.common.*;
import com.rpz.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class DefaultReqHandler extends ReqHandlerBase {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected BaseService service;

    public DefaultReqHandler(BaseService service) {
        this.service = service;
    }


    @Override
    public Json read(Context ctx, Json req) throws Exception {
        //single entity read
        String param = ctx.endingPrm();
        if(param!=null)
            return service.read(param);

        return find(ctx, req);
    }

    @Override
    public Json find(Context ctx, Json req) throws Exception {
        String query = ctx.query;
        //TODO: simple query. we only support and operation for all ctx.params for now!
        Object[] prms = null;
        if(!Vld.isEmpty(query)) {
            String[] qarr = query.split("&");
            prms = new Object[qarr.length];
            StringBuilder qq = new StringBuilder();
            int i = 0;
            for (String a : qarr) {
                if (i > 0) qq.append(" and ");
                String[] qp = a.split("=");
                qq.append(qp[0]).append("=?");
                prms[i++] = qp[1];
            }

            query = qq.toString();
        }

        return service.find(query, prms);
    }

    public Json create(Context ctx, Json req) throws Exception{
        Vld.checkEmpty(req, "request");
        return  service.create(req);
    }

    public Json update(Context ctx, Json req) throws Exception{
        Vld.checkEmpty(req, "request");
        return service.update(req);
    }

    //partial updates - NoP for the time being
    public Json patch(Context ctx, Json req) throws Exception{
        throw new OpsException(OpsStatus.FORBIDDEN);
    }


    public Json delete(Context ctx, Json req) throws Exception{
        Vld.checkEmpty(ctx.params, "id");
        boolean cascade = false;

        if(!Vld.isEmpty(ctx.query)) {
            String [] qfrag = ctx.query.split("&");
            for(String q: qfrag){
                if(q.startsWith("cascade")) cascade = Boolean.valueOf(q.split("=")[1]);
            }
        }

        int ret = service.delete(ctx.getParam("id"), cascade);
        return new Json("status", ret);
    }
}
