package io.github.seokhyunpark.hft.controller;

import com.binance.connector.client.common.ApiException;
import java.util.concurrent.CountDownLatch;

import com.binance.connector.client.common.websocket.service.StreamBlockingQueueWrapper;
import com.binance.connector.client.spot.websocket.api.model.UserDataStreamEventsResponse;
import com.binance.connector.client.spot.websocket.stream.model.PartialBookDepthResponse;
import io.github.seokhyunpark.hft.connector.BinanceConnector;
import io.github.seokhyunpark.hft.connector.MarketStreamConnector;
import io.github.seokhyunpark.hft.connector.UserStreamConnector;
import io.github.seokhyunpark.hft.manager.MarketStreamManager;
import io.github.seokhyunpark.hft.manager.UserStreamManager;

public class HftController {
    private final MarketStreamManager marketManager;
    private final UserStreamManager userManager;
    private final MarketStreamConnector marketStreamConnector;
    private final UserStreamConnector userStreamConnector;

    public HftController() {
        try {
            BinanceConnector binanceConnector = new BinanceConnector();

            this.marketStreamConnector = new MarketStreamConnector();
            this.userStreamConnector = new UserStreamConnector();

            this.marketManager = new MarketStreamManager(binanceConnector);
            this.userManager = new UserStreamManager(binanceConnector);
        } catch (ApiException e) {
            throw new RuntimeException("HFT 컨트롤러 생성 실패: ", e);
        }
    }

    public void start() {
        try {
            StreamBlockingQueueWrapper<PartialBookDepthResponse> marketQueue = marketStreamConnector.connect();
            StreamBlockingQueueWrapper<UserDataStreamEventsResponse> userQueue = userStreamConnector.connect();

            Thread marketThread = new Thread(() -> {
                marketManager.startProcessing(marketQueue);
            });
            marketThread.setName("MarketThread");
            marketThread.start();

            Thread userThread = new Thread(() -> {
                userManager.startProcessing(userQueue);
            });
            userThread.setName("UserThread");
            userThread.start();

            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            System.err.println("[HftController] 오류 발생: " + e);
        }
    }
}
