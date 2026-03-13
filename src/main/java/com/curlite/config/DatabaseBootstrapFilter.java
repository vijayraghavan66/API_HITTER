package com.curlite.config;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class DatabaseBootstrapFilter implements ContainerRequestFilter {

    private static volatile boolean initialized = false;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!initialized) {
            synchronized (DatabaseBootstrapFilter.class) {
                if (!initialized) {
                    DatabaseManager.initSchema();
                    initialized = true;
                }
            }
        }
    }
}
