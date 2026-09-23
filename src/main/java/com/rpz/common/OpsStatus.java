package com.rpz.common;


/**
 * Message execution (ops) status
 */
/* 	response status code
    3-bit class and a 5-bit detail

	0 1 2 3 4 5 6 7
	+-+-+-+-+-+-+-+-+
	|major|  minor  |
	+-+-+-+-+-+-+-+-+

	e.g. response status
	 1. 0x41 -> 010 00001  -> 2.01  => CREATED
     2. 0x9d -> 100 11101  -> 4.29  => TOO_MANY_REQUESTS
     3. 0xA5 -> 101 00101  -> 5.20  => UNKNOWN

*/
public enum OpsStatus {

    CONTINUE((byte)0x20),                   /* 1.00 */
    SWITCHING_PROTOCOLS((byte)0x21),        /* 1.01 */
    PROCESSING((byte)0x22),                 /* 1.02 async operation in progress - rpz clients */

    OK((byte)0x40),                         /* 2.00 success */
    CREATED((byte)0x41), 					/* 2.01 */
    ACCEPTED((byte)0x42),                   /* 2.02 request is accepted for processing */
    CHANGED((byte)0x44),					/* 2.04 delete/update operation success*/
    NO_CONTENT((byte)0x44),                 /* 2.04 - same as above */
    TUN_ACK((byte)0x45),                    /* 2.05 */

    FOUND((byte)0x62),				        /* 3.02 */
    NOT_CHANGED((byte)0x64),				/* 3.04 update, delete operation failed and no change is made */

    /** Client Error 4xx */
    BAD_REQUEST((byte)0x80),				/* 4.00 */
    UNAUTHORIZED((byte)0x81),				/* 4.01 */
    FORBIDDEN((byte)0x83),					/* 4.03 */
    NOT_FOUND((byte)0x84),					/* 4.04 */
    //METHOD_NOT_ALLOWED -> FORBIDDEN
    NOT_ACCEPTABLE((byte)0x86),			    /* 4.06 */
    REQUEST_TIMEOUT((byte)0x88),		    /* 4.08 */
    CONFLICT((byte)0x89),			        /* 4.09 */
    GONE((byte)0x8A),			            /* 4.10 */
    INVALID_CONTENT_SIZE((byte)0x8B),		/* 4.11 */ //LENGTH_REQUIRED
    PRECONDITION_FAILED((byte)0x8C),		/* 4.12 */
    PAYLOAD_TOO_LARGE((byte)0x8D),	        /* 4.13 */
    UNSUPPORTED_MEDIA_TYPE((byte)0x8F),     /* 4.15 */
    TOO_MANY_REQUESTS((byte)0x9D),          /* 4.29 */
    //EXPECTATION_FAILED -> BAD_REQUEST

    /** Server Error 5xx */
    INTERNAL_SERVER_ERROR((byte)0xA0),		/* 5.00 */
    //NOT_IMPLEMENTED  -> FORBIDDEN
    BAD_GATEWAY((byte)0xA2),				/* 5.02 */
    SERVICE_UNAVAILABLE((byte)0xA3),		/* 5.03 */
    GATEWAY_TIMEOUT((byte)0xA4),			/* 5.04 */
    INVALID_VERSION((byte)0xA5),  		    /* 5.05 */
    UNKNOWN((byte)0xB4);		            /* 5.20 */
    /*

    UNDEFINED((byte)0xFF);*/


    byte code;
    public byte code() {
        return code;
    }

    OpsStatus(byte code) {
        this.code = code;
    }

    public int status(){
        return (code >> 5 & 0x07) * 100 + (code & 0x1f);
    }

    public static OpsStatus valueOf(int code){
        switch (code){
            case 100: return CONTINUE;
            case 101: return SWITCHING_PROTOCOLS;
            case 102: return PROCESSING;

            /** Successful 2xx */
            case 200: return OK;
            case 201: return CREATED;
            case 202: return ACCEPTED;
            case 204: return NO_CONTENT;

            /** Redirection 3xx */
            case 302: return FOUND;
            case 304: return NOT_CHANGED;

            /** Client Error 4xx */
            case 400: return BAD_REQUEST;
            case 401: return UNAUTHORIZED;
            case 403: return FORBIDDEN;
            case 404: return NOT_FOUND;
            case 406: return NOT_ACCEPTABLE;
            case 408: return REQUEST_TIMEOUT;
            case 409: return CONFLICT;
            case 410: return GONE;
            case 412: return PRECONDITION_FAILED;
            case 413: return PAYLOAD_TOO_LARGE;
            case 415: return UNSUPPORTED_MEDIA_TYPE;
            case 429: return TOO_MANY_REQUESTS;

            /** Server Error 5xx */
            case 500: return INTERNAL_SERVER_ERROR;
            case 502: return BAD_GATEWAY;
            case 503: return SERVICE_UNAVAILABLE;
            case 504: return GATEWAY_TIMEOUT;
        }
        return UNKNOWN;
    }
}
