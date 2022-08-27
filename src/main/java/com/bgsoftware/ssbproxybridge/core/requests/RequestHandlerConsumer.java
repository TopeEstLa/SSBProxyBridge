package com.bgsoftware.ssbproxybridge.core.requests;

public interface RequestHandlerConsumer<E> {

    void accept(E element) throws RequestHandlerException;

}
