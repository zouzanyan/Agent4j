package com.example.ai.service;

import com.example.ai.dto.CoinGeckoPriceDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class CoinGeckoService {

    private static final String BASE_URL = "https://api.coingecko.com/api/v3";

    private final OkHttpClient okHttpClient;

    private final ObjectMapper objectMapper;

    public CoinGeckoService(@Qualifier("proxyOkHttpClient") OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取加密货币实时行情
     *
     * @param coinIds 币种ID列表，逗号分隔，如 "bitcoin,ethereum"
     * @param vsCurrency 计价货币，如 "usd", "cny"
     * @return 行情数据JSON字符串
     */
    public String getCoinPrices(String coinIds, String vsCurrency) throws IOException {
        String url = String.format("%s/coins/markets?vs_currency=%s&ids=%s&order=market_cap_desc&sparkline=false",
                BASE_URL, vsCurrency, coinIds);

        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("CoinGecko API request failed: " + response.code());
            }

            String body = response.body().string();
            log.info("CoinGecko API response for coins: {}", coinIds);
            return body;
        }
    }

    /**
     * 获取单个币种价格
     *
     * @param coinId 币种ID，如 "bitcoin"
     * @param vsCurrency 计价货币
     * @return 价格信息
     */
    public String getCoinPrice(String coinId, String vsCurrency) throws IOException {
        String url = String.format("%s/simple/price?ids=%s&vs_currencies=%s&include_24hr_change=true",
                BASE_URL, coinId, vsCurrency);

        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("CoinGecko API request failed: " + response.code());
            }

            String body = response.body().string();
            log.info("CoinGecko API response for coin: {}", coinId);
            return body;
        }
    }

    /**
     * 获取热门币种列表
     *
     * @param vsCurrency 计价货币
     * @param limit 数量限制
     * @return 行情数据JSON字符串
     */
    public String getTopCoins(String vsCurrency, int limit) throws IOException {
        String url = String.format("%s/coins/markets?vs_currency=%s&order=market_cap_desc&per_page=%d&page=1&sparkline=false",
                BASE_URL, vsCurrency, limit);

        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("CoinGecko API request failed: " + response.code());
            }

            String body = response.body().string();
            log.info("CoinGecko API response for top {} coins", limit);
            return body;
        }
    }
}
