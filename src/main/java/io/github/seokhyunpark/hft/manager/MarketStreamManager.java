package io.github.seokhyunpark.hft.manager;

import com.binance.connector.client.common.websocket.service.StreamBlockingQueueWrapper;
import com.binance.connector.client.spot.websocket.stream.model.PartialBookDepthResponse;
import io.github.seokhyunpark.hft.connector.BinanceConnector;
import java.util.concurrent.CountDownLatch;

public class MarketStreamManager {
    private final BinanceConnector binanceConnector;
    private final CountDownLatch userStreamReadyLatch;

    public MarketStreamManager(BinanceConnector binanceConnector, CountDownLatch userStreamReadyLatch) {
        this.binanceConnector = binanceConnector;
        this.userStreamReadyLatch = userStreamReadyLatch;
    }

    public void startProcessing(StreamBlockingQueueWrapper<PartialBookDepthResponse> queue) {
        System.out.println("[ManagerStream] 데이터 처리 시작. 큐 대기 중...");
        try {
            System.out.println("[ManagerStream] UserStream 준비 신호 대기 중...");
            this.userStreamReadyLatch.await();

            while (true) {
                PartialBookDepthResponse response = queue.take();
                System.out.println("[MarketStream] " + response.toJson());
            }
        } catch (InterruptedException e) {
            System.err.println("[InterruptedException] " + e);
        }
    }
}
