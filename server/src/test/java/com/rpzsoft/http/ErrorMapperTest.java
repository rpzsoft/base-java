package com.rpzsoft.http;

import com.rpzsoft.common.OpsException;
import com.rpzsoft.common.OpsRuntimeException;
import com.rpzsoft.common.OpsStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ErrorMapperTest {

    @Test
    public void nullPointerBecomesGeneric500() {
        NullPointerException npe =
                new NullPointerException("Cannot invoke \"com.rpzsoft.service.RepoService.find(String, Object[])\" because \"this.repo\" is null");

        HttpStatus status = ErrorMapper.status(npe);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, status);
        assertEquals(ErrorMapper.SERVER_ERROR, ErrorMapper.message(npe, status));
    }

    @Test
    public void illegalArgumentIsBadRequest() {
        IllegalArgumentException e = new IllegalArgumentException("request cannot be null/empty");

        HttpStatus status = ErrorMapper.status(e);
        assertEquals(HttpStatus.BAD_REQUEST, status);
        assertEquals("request cannot be null/empty", ErrorMapper.message(e, status));
    }

    @Test
    public void opsExceptionKeepsStatusAndMessage() {
        OpsException e = new OpsException(OpsStatus.FORBIDDEN);

        HttpStatus status = ErrorMapper.status(e);
        assertEquals(HttpStatus.FORBIDDEN, status);
        assertEquals("FORBIDDEN", ErrorMapper.message(e, status));
    }

    @Test
    public void opsRuntimeExceptionKeepsStatus() {
        OpsRuntimeException e = new OpsRuntimeException(OpsStatus.UNAUTHORIZED);

        assertEquals(HttpStatus.UNAUTHORIZED, ErrorMapper.status(e));
    }

    @Test
    public void serverErrorMessagesAreSanitized() {
        OpsException e = new OpsException(OpsStatus.INTERNAL_SERVER_ERROR, "db down: password=secret");

        HttpStatus status = ErrorMapper.status(e);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, status);
        assertEquals(ErrorMapper.SERVER_ERROR, ErrorMapper.message(e, status));
    }
}
