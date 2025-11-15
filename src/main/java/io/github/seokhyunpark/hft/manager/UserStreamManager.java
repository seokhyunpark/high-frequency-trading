package io.github.seokhyunpark.hft.manager;

import com.binance.connector.client.common.websocket.service.StreamBlockingQueueWrapper;
import com.binance.connector.client.spot.websocket.api.model.UserDataStreamEventsResponse;
import io.github.seokhyunpark.hft.connector.BinanceConnector;

public class UserStreamManager {
    private final BinanceConnector binanceConnector;

    public UserStreamManager(BinanceConnector binanceConnector) {
        this.binanceConnector = binanceConnector;
    }

    public void startProcessing(StreamBlockingQueueWrapper<UserDataStreamEventsResponse> queue) {
        System.out.println("[UserStream] 데이터 처리 시작. 큐 대기 중...");
        try {
            while (true) {
                Object data = queue.take().getActualInstance();
                System.out.println("[UserStream] " + data.toString());
            }
        } catch (InterruptedException e) {
            System.err.println("InterruptedException " + e);
        }
    }
}