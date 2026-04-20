package com.example.ai.agent;

import com.example.ai.service.CoinGeckoService;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class Tools {

    @Autowired
    private CoinGeckoService coinGeckoService;

    @Tool("获取当前日期和时间")
    public String getCurrentTime() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("Tool called: getCurrentTime, result: {}", time);
        return time;
    }

    @Tool("获取加密货币实时行情数据。参数coinIds为币种ID列表，用逗号分隔，如bitcoin,ethereum；参数vsCurrency为计价货币，如usd或cny。返回包含价格、市值、24小时涨跌幅等信息的JSON数据。")
    public String getCryptoPrices(String coinIds, String vsCurrency) {
        log.info("Tool called: getCryptoPrices, coinIds: {}, vsCurrency: {}", coinIds, vsCurrency);
        try {
            String result = coinGeckoService.getCoinPrices(coinIds, vsCurrency);
            return result;
        } catch (Exception e) {
            log.error("Failed to get crypto prices: {}", e.getMessage(), e);
            return "获取行情数据失败: " + e.getMessage();
        }
    }

    @Tool("获取热门加密货币行情数据。参数vsCurrency为计价货币，如usd或cny；参数limit为返回数量，如10表示前10个币种。返回按市值排序的行情JSON数据。")
    public String getTopCryptoPrices(String vsCurrency, int limit) {
        log.info("Tool called: getTopCryptoPrices, vsCurrency: {}, limit: {}", vsCurrency, limit);
        try {
            String result = coinGeckoService.getTopCoins(vsCurrency, limit);
            return result;
        } catch (Exception e) {
            log.error("Failed to get top crypto prices: {}", e.getMessage(), e);
            return "获取热门行情数据失败: " + e.getMessage();
        }
    }

    @Tool("获取单个加密货币的简单价格信息。参数coinId为币种ID，如bitcoin；参数vsCurrency为计价货币，如usd。返回包含价格和24小时涨跌幅的JSON数据。")
    public String getSimpleCryptoPrice(String coinId, String vsCurrency) {
        log.info("Tool called: getSimpleCryptoPrice, coinId: {}, vsCurrency: {}", coinId, vsCurrency);
        try {
            String result = coinGeckoService.getCoinPrice(coinId, vsCurrency);
            return result;
        } catch (Exception e) {
            log.error("Failed to get simple crypto price: {}", e.getMessage(), e);
            return "获取价格数据失败: " + e.getMessage();
        }
    }
}
