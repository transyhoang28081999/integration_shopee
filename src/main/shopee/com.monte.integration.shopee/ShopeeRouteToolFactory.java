package com.monte.integration.shopee;

import org.apache.camel.CamelContext;
import org.apache.camel.component.http4.HttpClientConfigurer;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.rest.RestComponent;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.moqui.context.ExecutionContextFactory;
import org.moqui.context.ToolFactory;
import org.moqui.impl.service.camel.CamelToolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;
public class ShopeeRouteToolFactory implements ToolFactory<CamelContext> {
    protected final static Logger logger = LoggerFactory.getLogger(ShopeeRouteToolFactory.class);
    final static String TOOL_NAME = "integration_shopee";

    ShopeeRouteToolFactory() { };

    @Override
    public String getName() {return TOOL_NAME; }

    @Override
    public void init(ExecutionContextFactory ecf) {
        logger.info("Starting integration_shopee...");
        CamelContext camelContext = (CamelContext) ecf.getTool("Camel", CamelToolFactory.class);

        PropertiesComponent pc = camelContext.getComponent("properties", PropertiesComponent.class);
        pc.setLocation("file:runtime/component/integration_shopee/config/integration.properties");

        HttpClientConfigurer hcc = new HttpClientConfigurer() {
            public void configureHttpClient(HttpClientBuilder clientBuilder) {
                clientBuilder.setRedirectStrategy(new LaxRedirectStrategy(){
                    protected URI createLocationURI(final String orig ) throws ProtocolException
                    {
                        // Replace spaces
                        Objects.requireNonNull( orig );
                        var fixed = orig.replaceAll("\\s","%20"); // Not \\s+
                        return super.createLocationURI( fixed );
                    }

                });
            }
        };

        RestComponent shopeeRestComponent = camelContext.getComponent("rest", RestComponent.class);

        try {
            String host = camelContext.resolvePropertyPlaceholders("{{shopee.graph_api.host}}");
            shopeeRestComponent.setHost(host);
            camelContext.addComponent("shopee", shopeeRestComponent);

            /*The place to add route*/
//            camelContext.addRoutes(new GetAccessTokenRoute());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CamelContext getInstance(Object... parameters) { return null; }
}
