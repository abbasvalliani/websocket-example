package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class WebSocketHandler extends BinaryWebSocketHandler {
    Set<WebSocketSession> sessions = new HashSet<WebSocketSession>();
    Map<WebSocketSession, InFlightData> data = new HashMap<WebSocketSession, InFlightData>();

    public WebSocketHandler() {
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        synchronized (sessions) {
            log.info("Adding websocket server session {}", session.getId());
            sessions.add(session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        synchronized (data) {
            data.remove(session);
        }
        synchronized (sessions) {
            log.info("Removing websocket server session {}", session.getId());
            sessions.remove(session);
        }
    }

    public void send(byte[] bytes) {
        BinaryMessage message = new BinaryMessage(bytes, true);
        log.info("Broadcasting bytes {} to {} clients", bytes, sessions.size());

        sessions.forEach(s -> {
            try {
                log.info("Broadcasting bytes {} to session: {}->{} bytes",
                         bytes, s.getId(), message.getPayloadLength());
                s.sendMessage(message);
            } catch (Exception e) {
                log.error("Unable to broadcast message to client session {}", s.getId(), e);
            }
        });
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            log.info("Partial binary payload from client with size {}", message.getPayloadLength());

            synchronized (data) {
                data.putIfAbsent(session, InFlightData.create());
                data.get(session).append(message.getPayload());
            }

            if (message.isLast()) {
                byte[] bytes;
                synchronized (data) {
                    bytes = data.get(session).getBytes();
                    data.remove(session);
                }

                //got the message
                String content = new String(bytes, StandardCharsets.UTF_8);
                log.info("Binary message {} data {} from client with size {}", bytes, content, bytes.length);

                String response = "Hello " + content;
                send(response.getBytes());
            }
        } catch (Exception e) {
            log.error("Uknown websocket server error", e);
            synchronized (data) {
                data.remove(session);
            }
            synchronized (sessions) {
                sessions.remove(session);
            }
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable t) throws Exception {
        log.error("Transport error {}->", t.getMessage());

        if (!session.isOpen()) {
            synchronized (sessions) {
                sessions.remove(session);
            }
        }
    }

    static class InFlightData {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();

        public InFlightData() {
        }

        public static InFlightData create() {
            return new InFlightData();
        }

        public InFlightData append(ByteBuffer byteBuffer) throws IOException {
            bs.write(byteBuffer.array());
            return this;
        }

        public byte[] getBytes() {
            return bs.toByteArray();
        }
    }
}
