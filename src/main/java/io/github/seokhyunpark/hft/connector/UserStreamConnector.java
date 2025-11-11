package io.github.seokhyunpark.hft.connector;

import com.binance.connector.client.common.ApiException;
import com.binance.connector.client.common.configuration.SignatureConfiguration;
import com.binance.connector.client.common.websocket.configuration.WebSocketClientConfiguration;
import com.binance.connector.client.spot.websocket.api.SpotWebSocketApiUtil;
import com.binance.connector.client.spot.websocket.api.api.SpotWebSocketApi;
import com.binance.connector.client.common.websocket.dtos.StreamResponse;
import com.binance.connector.client.common.websocket.service.StreamBlockingQueueWrapper;
import com.binance.connector.client.spot.websocket.api.model.UserDataStreamEventsResponse;
import com.binance.connector.client.spot.websocket.api.model.UserDataStreamSubscribeResponse;
import io.github.seokhyunpark.hft.config.ApplicationConstants;

public class UserStreamConnector {
    private SpotWebSocketApi api;

    public SpotWebSocketApi getApi() {
        if (api == null) {
            WebSocketClientConfiguration clientConfiguration = SpotWebSocketApiUtil.getClientConfiguration();
            clientConfiguration.setAutoLogon(true);
            SignatureConfiguration signatureConfiguration = new SignatureConfiguration();
            signatureConfiguration.setApiKey(ApplicationConstants.API_KEY);
            signatureConfiguration.setPrivateKey(ApplicationConstants.PRIVATE_KEY_PATH);
            clientConfiguration.setSignatureConfiguration(signatureConfiguration);
            api = new SpotWebSocketApi(clientConfiguration);
        }
        return api;
    }

    public StreamBlockingQueueWrapper<UserDataStreamEventsResponse> connect() throws ApiException {
        StreamResponse<UserDataStreamSubscribeResponse, UserDataStreamEventsResponse> response = getApi().userDataStreamSubscribe();
        return response.getStream();
    }
}
