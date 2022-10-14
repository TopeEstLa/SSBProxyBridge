package com.bgsoftware.ssbproxybridge.core.requests;

import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;

public interface RequestHandlerConsumer<E> {

    void accept(Bundle bundle, E element) throws RequestHandlerException;

}
