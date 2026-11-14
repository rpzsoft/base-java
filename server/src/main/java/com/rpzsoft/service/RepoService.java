package com.rpzsoft.service;


import com.rpzsoft.common.Json;

/**
 */
public interface RepoService {
    Json create(Json entity) throws Exception;
    Json read(String id) throws Exception;
    Json readByKV(String key, String value) throws Exception;
    //update single instance
    Json update(Json entity) throws Exception;
    //delete sing;e instance
    int delete(String id, boolean cascade) throws Exception;

    Json exec(String query) throws Exception;
    Json find(String query, Object [] prms) throws Exception;
}
