package com.example.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CoinGeckoPriceDTO {

    private String id;
    private String symbol;
    private String name;
    private String image;
    private Double current_price;
    private Double market_cap;
    private Double market_cap_rank;
    private Double total_volume;
    private Double high_24h;
    private Double low_24h;
    private Double price_change_24h;
    private Double price_change_percentage_24h;
    private Double market_cap_change_24h;
    private Double market_cap_change_percentage_24h;
    private Double circulating_supply;
    private Double total_supply;
    private Double ath;
    private Double ath_change_percentage;
    private Double ath_date;
    private Double atl;
    private Double atl_change_percentage;
    private Double atl_date;

    @JsonProperty("last_updated")
    private String lastUpdated;
}
