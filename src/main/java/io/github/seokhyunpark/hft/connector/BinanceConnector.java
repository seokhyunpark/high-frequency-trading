package io.github.seokhyunpark.hft.connector;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.binance.connector.client.common.ApiException;
import com.binance.connector.client.common.ApiResponse;
import com.binance.connector.client.common.configuration.ClientConfiguration;
import com.binance.connector.client.common.configuration.SignatureConfiguration;
import com.binance.connector.client.spot.rest.SpotRestApiUtil;
import com.binance.connector.client.spot.rest.api.SpotRestApi;
import com.binance.connector.client.spot.rest.model.CancelRestrictions;
import com.binance.connector.client.spot.rest.model.DeleteOrderResponse;
import com.binance.connector.client.spot.rest.model.GetAccountResponse;
import com.binance.connector.client.spot.rest.model.GetAccountResponseBalancesInner;
import com.binance.connector.client.spot.rest.model.NewOrderRequest;
import com.binance.connector.client.spot.rest.model.NewOrderResponse;
import com.binance.connector.client.spot.rest.model.OrderType;
import com.binance.connector.client.spot.rest.model.Side;

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

    public NewOrderResponse buyLimitMaker(String symbol, BigDecimal quantity, BigDecimal price) throws ApiException {
        NewOrderRequest request = new NewOrderRequest();
        request.symbol(symbol);
        request.side(Side.BUY);
        request.type(OrderType.LIMIT_MAKER);
        request.quantity(quantity.doubleValue());
        request.price(price.doubleValue());

        ApiResponse<NewOrderResponse> response = api.newOrder(request);
        return response.getData();
    }

    public NewOrderResponse sellLimitMaker(String symbol, BigDecimal quantity, BigDecimal price) throws ApiException {
        NewOrderRequest request = new NewOrderRequest();
        request.symbol(symbol);
        request.side(Side.SELL);
        request.type(OrderType.LIMIT_MAKER);
        request.quantity(quantity.doubleValue());
        request.price(price.doubleValue());

        ApiResponse<NewOrderResponse> response = api.newOrder(request);
        return response.getData();
    }

    public NewOrderResponse buyMarket(String symbol, BigDecimal quoteOrderQty) throws ApiException {
        NewOrderRequest request = new NewOrderRequest();
        request.symbol(symbol);
        request.side(Side.BUY);
        request.type(OrderType.MARKET);
        request.quoteOrderQty(quoteOrderQty.doubleValue());

        ApiResponse<NewOrderResponse> response = api.newOrder(request);
        return response.getData();
    }

    public NewOrderResponse sellMarket(String symbol, BigDecimal quoteOrderQty) throws ApiException {
        NewOrderRequest request = new NewOrderRequest();
        request.symbol(symbol);
        request.side(Side.SELL);
        request.type(OrderType.MARKET);
        request.quantity(quoteOrderQty.doubleValue());

        ApiResponse<NewOrderResponse> response = api.newOrder(request);
        return response.getData();
    }

    public DeleteOrderResponse cancelOrder(String symbol, Long orderId) throws ApiException {
        String origClientOrderId = null;
        String newClientOrderId = null;
        CancelRestrictions cancelRestrictions = null;
        Long recvWindow = null;

        ApiResponse<DeleteOrderResponse> response = api.deleteOrder(
                symbol,
                orderId,
                origClientOrderId,
                newClientOrderId,
                cancelRestrictions,
                recvWindow
        );
        return response.getData();
    }

    public GetAccountResponse getAccount() throws ApiException {
        Boolean omitZeroBalances = true;
        Long recvWindow = null;

        ApiResponse<GetAccountResponse> response = api.getAccount(omitZeroBalances, recvWindow);
        return response.getData();
    }

    public BigDecimal getFreeBalance(String symbol) {
        GetAccountResponse account = getAccount();
        if (account == null) {
            return BigDecimal.ZERO;
        }

        List<GetAccountResponseBalancesInner> balances = account.getBalances();
        if (balances == null) {
            return BigDecimal.ZERO;
        }

        for (GetAccountResponseBalancesInner balance : balances) {
            if (Objects.equals(balance.getAsset(), symbol)) {
                String freeBalance = balance.getFree();
                if (freeBalance == null) {
                    return BigDecimal.ZERO;
                }
                return new BigDecimal(freeBalance);
            }
        }
        return BigDecimal.ZERO;
    }
}
