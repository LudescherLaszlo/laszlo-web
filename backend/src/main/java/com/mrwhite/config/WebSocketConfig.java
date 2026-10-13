package com.mrwhite.config;

import com.mrwhite.game.GameService;
import org.springframework.context.annotation.*;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final SessionChannelInterceptor interceptor;
    private final String allowedOrigin;
    public WebSocketConfig(SessionChannelInterceptor interceptor, @Value("${mrwhite.allowedOrigin:http://localhost:5173}") String allowedOrigin){this.interceptor=interceptor;this.allowedOrigin=allowedOrigin;}
    @Override public void registerStompEndpoints(StompEndpointRegistry registry){registry.addEndpoint("/ws").setAllowedOriginPatterns(allowedOrigin);}
    @Override public void configureMessageBroker(MessageBrokerRegistry registry){registry.setApplicationDestinationPrefixes("/app");registry.enableSimpleBroker("/topic","/queue");registry.setUserDestinationPrefix("/user");}
    @Override public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration){registration.interceptors(interceptor);}
}
