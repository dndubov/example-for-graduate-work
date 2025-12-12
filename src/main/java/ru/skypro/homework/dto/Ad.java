package ru.skypro.homework.dto;

import lombok.Data;

@Data
public class Ad {
    private Integer author;
    private String image;
    private Integer pk;
    private Integer price;
    private String title;

    public Ad(Integer author, String image, Integer pk, Integer price, String title) {
        this.author = author;
        this.image = image;
        this.pk = pk;
        this.price = price;
        this.title = title;
    }

    public Ad() {

    }
}
