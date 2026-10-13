package com.mrwhite.config;

import com.mrwhite.game.GameService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import java.security.Principal;

@Configuration
public class SessionChannelInterceptor implements ChannelInterceptor {
    private final GameService game;
    public SessionChannelInterceptor(@Lazy GameService game){this.game=game;}
    @Override public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor a=MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);
        if(a==null)return message;
        if(StompCommand.CONNECT.equals(a.getCommand())){
            String session=a.getFirstNativeHeader("sessionId");
            if(session==null||game.player(session)==null) throw new MessagingException("INVALID_SESSION");
            a.setUser(new Principal(){public String getName(){return session;}});
            game.connected(session);
        } else if(StompCommand.DISCONNECT.equals(a.getCommand())&&a.getUser()!=null) game.disconnected(a.getUser().getName());
        return message;
    }
}
