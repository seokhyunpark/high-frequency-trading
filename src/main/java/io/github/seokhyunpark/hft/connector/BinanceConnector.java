package io.github.seokhyunpark.hft.connector;

import com.binance.connector.client.common.ApiException;
import com.binance.connector.client.common.configuration.ClientConfiguration;
import com.binance.connector.client.common.configuration.SignatureConfiguration;
import com.binance.connector.client.spot.rest.SpotRestApiUtil;
import com.binance.connector.client.spot.rest.api.SpotRestApi;

import io.github.seokhyunpark.hft.config.ApplicationConstants;

public class BinanceConnector {
    private final SpotRestApi api;

    public BinanceConnector() throws ApiException {
        ClientConfiguration clientConfiguration = SpotRestApiUtil.getClientConfiguration();
        SignatureConfiguration signatureConfiguration = new SignatureConfiguration();
        signatureConfiguration.setApiKey(ApplicationConstants.API_KEY);
        signatureConfiguration.setPrivateKey(ApplicationConstants.PRIVATE_KEY_PATH);
        clientConfiguration.setSignatureConfiguration(signatureConfiguration);
        this.api = new SpotRestApi(clientConfiguration);
    }
}
