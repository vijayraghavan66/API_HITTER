package com.curlite.config;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.ResourceConfig;

public class AppConfig extends ResourceConfig {

    public AppConfig() {
        packages("com.curlite.resource");
        register(JacksonFeature.class);
        property(ServerProperties.WADL_FEATURE_DISABLE, true);
        register(CorsFilter.class);
        register(DatabaseBootstrapFilter.class);
    }
}
