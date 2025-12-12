package ru.skypro.homework.dto;

import lombok.Data;

import java.util.List;

@Data
public class Comments {

    private Integer count;
    private List<Comment> results;

    public Comments(Integer count, List<Comment> results) {
        this.count = count;
        this.results = results;
    }

    public Comments() {

    }
}
