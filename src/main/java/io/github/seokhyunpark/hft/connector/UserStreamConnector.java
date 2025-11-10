package io.github.seokhyunpark.hft.connector;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.seokhyunpark.hft.config.ApplicationConstants;
import io.github.seokhyunpark.hft.config.ApplicationConfig;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;

public class UserStreamConnector {
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    private final HttpClient client;
    private final Gson gson;
    private BlockingQueue<String> queue;
    private volatile ConnectionState state = ConnectionState.DISCONNECTED;

    public UserStreamConnector() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public BlockingQueue<String> connect() throws Exception {
        queue = new LinkedBlockingQueue<>();

        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                System.out.println("[UserConnector] WebSocket 연결 성공. Session Logon 시도...");
                try {
                    sendSessionLogon(webSocket);
                    state = ConnectionState.AWAITING_LOGON_RESPONSE;
                } catch (Exception e) {
                    System.err.println("[UserConnector] Logon 요청 전송 실패: " + e.getMessage());
                }

                webSocket.request(1);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                String message = data.toString();
                try {
                    if (state == ConnectionState.ACTIVE) {
                        handleActiveMessage(message);
                    } else if (state == ConnectionState.AWAITING_LOGON_RESPONSE) {
                        handleLogonResponse(webSocket, message);
                    } else if (state == ConnectionState.AWAITING_SUBSCRIBE_RESPONSE) {
                        handleSubscribeResponse(webSocket, message);
                    }
                } catch (Exception e) {
                    System.err.println("[UserConnector] 메시지 처리 중 오류: " + e.getMessage());
                }

                webSocket.request(1);
                return null;
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                state = ConnectionState.DISCONNECTED;
                System.out.println("[UserConnector] 연결 종료: " + reason);
                return null;
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                state = ConnectionState.DISCONNECTED;
                System.err.println("[UserConnector] 오류 발생: " + error.getMessage());
            }
        };

        client.newWebSocketBuilder()
                .buildAsync(URI.create(ApplicationConfig.USER_STREAM_API_URL), listener)
                .join();

        return queue;
    }

    private void handleLogonResponse(WebSocket webSocket, String message) {
        Map<String, Object> response = gson.fromJson(message, MAP_TYPE);
        if (response.containsKey("status") && (Double) response.get("status") == 200.0) {
            System.out.println("[UserConnector] Session Logon 성공.");
            sendSubscribeUserStream(webSocket);
            state = ConnectionState.AWAITING_SUBSCRIBE_RESPONSE;
        } else {
            System.err.println("[UserConnector] Session Logon 실패: " + message);
        }
    }

    private void handleSubscribeResponse(WebSocket webSocket, String message) {
        Map<String, Object> response = gson.fromJson(message, MAP_TYPE);
        if (response.containsKey("status") && (Double) response.get("status") == 200.0) {
            System.out.println("[UserConnector] User Stream 구독 성공. 데이터 수신 대기...");
            state = ConnectionState.ACTIVE;
        } else {
            System.err.println("[UserConnector] User Stream 구독 실패: " + message);
        }
    }

    private void handleActiveMessage(String message) throws InterruptedException {
        queue.put(message);
    }

    private void sendSessionLogon(WebSocket webSocket) throws Exception {
        Map<String, String> params = new TreeMap<>();
        params.put("apiKey", ApplicationConstants.API_KEY);
        params.put("timestamp", String.valueOf(Instant.now().toEpochMilli()));

        String query = urlEncode(params);
        String signature = generateSignature(query);
        params.put("signature", signature);

        Map<String, Object> message = Map.of(
                "id", UUID.randomUUID().toString(),
                "method", "session.logon",
                "params", params
        );
        webSocket.sendText(gson.toJson(message), true);
    }

    private void sendSubscribeUserStream(WebSocket webSocket) {
        Map<String, Object> message = Map.of(
                "id", UUID.randomUUID().toString(),
                "method", "userDataStream.subscribe"
        );
        webSocket.sendText(gson.toJson(message), true);
    }

    private static PrivateKey loadPrivateKey() throws Exception {
        String privateKeyPEM = ApplicationConstants.PRIVATE_KEY
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("\\R", "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return keyFactory.generatePrivate(keySpec);
    }

    private static String generateSignature(String data) throws Exception {
        Signature sig = Signature.getInstance("Ed25519");
        sig.initSign(loadPrivateKey());
        sig.update(data.getBytes(StandardCharsets.UTF_8));

        byte[] signatureBytes = sig.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    private static String urlEncode(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private enum ConnectionState {
        ACTIVE,
        DISCONNECTED,
        AWAITING_LOGON_RESPONSE,
        AWAITING_SUBSCRIBE_RESPONSE,
    }
}
