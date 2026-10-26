package com.dummyjson.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Wrapper POJO for the "Get all products" endpoint response shape:
 * {
 *   "products": [ {...}, {...} ],
 *   "total": 194,
 *   "skip": 0,
 *   "limit": 30
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductsResponse {

    private List<Product> products;
    private Integer total;
    private Integer skip;
    private Integer limit;

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
