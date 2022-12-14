package com.example;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableAutoConfiguration
public class WebConfig {
    int port = 8080;

    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> webServerFactoryCustomizer() {
        return new WebServerFactoryCustomizer<JettyServletWebServerFactory>() {
            @Override
            public void customize(JettyServletWebServerFactory factory) {
                factory.addServerCustomizers(new JettyServerCustomizer() {
                    @Override
                    public void customize(Server server) {
                        HttpConfiguration httpsConfiguration = new HttpConfiguration();

                        ServerConnector httpsConnector = new ServerConnector(
                            server,
                            new HttpConnectionFactory(
                                httpsConfiguration));
                        httpsConnector.setPort(8080);
                        server.setConnectors(new Connector[] { httpsConnector });
                    }
                });
            }
        };
    }

    @Configuration
    @EnableWebSecurity
    public static class CustomWebSecurity {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf()
                .disable()
                .cors().disable();

            return http.build();
        }
    }

    @Configuration
    @EnableWebSocket
    public class CustomWebSocketConfigurer implements WebSocketConfigurer {
        //REQUIRED FOR TOMCAT SUPPORT
        //        @Bean
        //        public ServletServerContainerFactoryBean createWebSocketContainer() {
        //            ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        //            container.setMaxTextMessageBufferSize(90000000);
        //            container.setMaxBinaryMessageBufferSize(90000000);
        //            return container;
        //        }

        @Bean
        public DefaultHandshakeHandler handshakeHandler() {
            WebSocketPolicy policy = WebSocketPolicy.newServerPolicy();
            policy.setMaxBinaryMessageSize(10 * 1024 * 1024); //10MB
            policy.setMaxTextMessageSize(10 * 1024 * 1024); //10MB
            policy.setIdleTimeout(600000);

            return new DefaultHandshakeHandler(
                new JettyRequestUpgradeStrategy(policy));
        }

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            registry.addHandler(new WebSocketHandler(), "/ws")
                    .setAllowedOrigins("*")
                    .setHandshakeHandler(handshakeHandler());
        }
    }
}
