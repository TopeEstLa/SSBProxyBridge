package com.bgsoftware.ssbproxybridge.core.requests;

import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;

public interface IRequestHandler {

    void handle(Bundle bundle) throws RequestHandlerException;

}
