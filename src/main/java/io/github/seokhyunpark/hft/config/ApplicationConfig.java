package io.github.seokhyunpark.hft.config;

import com.binance.connector.client.spot.websocket.stream.model.Levels;

public class ApplicationConfig {
    public static final String SYMBOL = "btcfdusd";
    public static final String SPEED = "100ms";
    public static final Levels LEVEL = Levels.LEVELS_20;

    public static final String USER_STREAM_API_URL = "wss://ws-api.binance.com:443/ws-api/v3";

    private ApplicationConfig() {
    }
}
