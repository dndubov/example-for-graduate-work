package ru.skypro.homework.dto;

import lombok.Data;
import java.util.List;

@Data
public class Ads {

    private Integer count;
    private List<Ad> results;

    public Ads(Integer count, List<Ad> results) {
        this.count = count;
        this.results = results;
    }
}
