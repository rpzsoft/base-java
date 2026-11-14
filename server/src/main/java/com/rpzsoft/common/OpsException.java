

package com.rpzsoft.common;


/**
 *  default to application/json
 */
public class OpsException extends Exception implements RpzException{
    public OpsStatus status;
    //type of the exception message - for protocol translation purpose
    public Object ref;

    public OpsException(OpsStatus status) {
        this(status, status.toString(), null, null);
    }

    public OpsException(OpsStatus status, Throwable cause) {
        this(status, cause.getMessage(), cause);
    }

    public OpsException(OpsStatus status, String message) {
        this(status, message, null, null);
    }

    public OpsException(OpsStatus status, String message, Throwable cause) {
        this(status, message, cause, null);
    }

    public OpsException(OpsStatus status, String message, MediaType contentType) {
        this(status, message, null, contentType.str());
    }

    public OpsException(OpsStatus status, String message, String ref) {
        this(status, message, null, ref);
    }

    public OpsException(OpsStatus status, String message, Throwable cause, String ref) {
        super(RpzException.getErr(message, true), cause);
        this.status = status == null? OpsStatus.UNKNOWN: status;
        this.ref = ref;
    }

    public Json getJsonMsg() {
        return new Json("err", getMessage());
    }

    public String getJsonStrMsg(){
        return RpzException.getMsg(super.getMessage());
    }

    public <T> T getStatus() {
        return (T) status;
    }

    public OpsStatus getOpsStatus() {
        return status;
    }

    public String getContentType() {
        return (String) ref;
    }

    public String getReference() {
        return (String) ref;
    }

    @Override
    public String toString() {
        return status + ": "+super.getLocalizedMessage();
    }
}
