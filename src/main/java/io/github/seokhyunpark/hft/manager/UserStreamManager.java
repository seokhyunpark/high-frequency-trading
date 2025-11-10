package io.github.seokhyunpark.hft.manager;

import java.util.concurrent.BlockingQueue;

public class UserStreamManager {

    public void startProcessing(BlockingQueue<String> queue) {
        System.out.println("[UserManager] 데이터 처리 시작. 큐 대기 중...");
        try {
            while (true) {
                String data = queue.take();
                System.out.println("[UserManager] 수신: " + data);
            }
        } catch (InterruptedException e) {
            System.err.println("[InterruptedException] " + e);
        }
    }
}