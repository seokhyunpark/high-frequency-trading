package io.github.seokhyunpark.hft.connector;

import com.binance.connector.client.common.ApiException;
import com.binance.connector.client.common.websocket.configuration.WebSocketClientConfiguration;
import com.binance.connector.client.common.websocket.service.StreamBlockingQueueWrapper;
import com.binance.connector.client.spot.websocket.stream.SpotWebSocketStreamsUtil;
import com.binance.connector.client.spot.websocket.stream.api.SpotWebSocketStreams;
import com.binance.connector.client.spot.websocket.stream.model.Levels;
import com.binance.connector.client.spot.websocket.stream.model.PartialBookDepthRequest;
import com.binance.connector.client.spot.websocket.stream.model.PartialBookDepthResponse;

public class MarketStreamConnector {
    private SpotWebSocketStreams api;

    private SpotWebSocketStreams getApi() {
        if (api == null) {
            WebSocketClientConfiguration configuration = SpotWebSocketStreamsUtil.getClientConfiguration();
            api = new SpotWebSocketStreams(configuration);
        }
        return api;
    }

    public StreamBlockingQueueWrapper<PartialBookDepthResponse> connect() throws ApiException {
        PartialBookDepthRequest request = new PartialBookDepthRequest();
        request.symbol("btcfdusd");
        request.levels(Levels.LEVELS_20);
        request.updateSpeed("100ms");

        return getApi().partialBookDepth(request);
    }
}
