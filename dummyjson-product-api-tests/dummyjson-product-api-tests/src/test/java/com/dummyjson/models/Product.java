package com.dummyjson.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO representation of a DummyJSON "product".
 *
 * The live API returns many more fields (discountPercentage, rating, stock,
 * tags, sku, weight, dimensions, reviews, meta, images, etc.) but per the
 * requirements of this suite we only model and validate:
 *   id, title, description, category, price, brand
 *
 * {@link JsonIgnoreProperties} ensures Jackson silently ignores every other
 * field present in the actual response instead of failing deserialization.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

    private Integer id;
    private String title;
    private String description;
    private String category;
    private Double price;
    private String brand;

    public Product() {
        // required no-arg constructor for Jackson
    }

    public Product(Integer id, String title, String description, String category, Double price, String brand) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
        this.brand = brand;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", brand='" + brand + '\'' +
                '}';
    }
}
