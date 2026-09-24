package com.rpz.service;

import com.rpz.common.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 */
public abstract class BaseService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected RepoService repo;

    public Json create(Json entity) throws Exception{
        return repo.create(entity);
    }

    public Json read(String id) throws Exception{
        return repo.read(id);
    }

    public Json update(Json entity) throws Exception{
        return repo.update(entity);
    }

    public int delete(String id, boolean cascade) throws Exception{
        return repo.delete(id, cascade);
    }


    public Json exec(String query) throws Exception{
        return repo.exec(query);
    }

    public Json find(String query, Object [] prms) throws Exception{
        return repo.find(query, prms);
    }
}
