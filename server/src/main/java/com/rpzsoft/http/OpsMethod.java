package com.rpzsoft.http;



public enum OpsMethod {
    NOP((byte)0x00),
    READ((byte)0x01),         /* 0.01 */     //GET
    WRITE((byte)0x02),        /* 0.02 */     //POST
    UPDATE((byte)0x03),       /* 0.03 */     //PUT
    DELETE((byte)0x04),       /* 0.04 */     //DELETE
    EXEC((byte)0x05),         /* 0.05 */     //POST
    PATCH((byte)0x06),        /* 0.06 */     //PATCH
    INFO((byte)0x07),         /* 0.07 */     //OPTIONS
    TUN((byte)0x08),          /* 0.08 */     //TUN - use in tunneling */
    SES((byte)0x09),          /* 0.09 establish a session */ //CONNECT
    SSE((byte)0x0a),          /* 0.0a establish a session */ //CONNECT

    UNDEFINED((byte)0xFF);

    byte code;
    public byte code() {
        return code;
    }

    OpsMethod(byte code) {
        this.code = code;
    }

    public static OpsMethod valueOf(int code){
        return valueOf((byte) code);
    }
    public static OpsMethod valueOf(byte code){
        switch (code){
            case (byte)0x01: return READ;
            case (byte)0x02: return WRITE;
            case (byte)0x03: return UPDATE;
            case (byte)0x04: return DELETE;
            case (byte)0x05: return EXEC;
            case (byte)0x06: return PATCH;
            case (byte)0x07: return INFO;
            case (byte)0x08: return TUN;
            case (byte)0x09: return SES;
            case (byte)0x0a: return SSE;

        }
        return UNDEFINED;
    }

    public static OpsMethod valueOf(HttpMethod method){
        switch (method){
            case GET: return READ;
            case POST: return WRITE;
            case PUT: return UPDATE;
            case DELETE: return DELETE;
            case PATCH: return PATCH;
            case HEAD:
            case OPTIONS: return INFO;
            case CONNECT: return SES;
        }
        return UNDEFINED;
    }
}

