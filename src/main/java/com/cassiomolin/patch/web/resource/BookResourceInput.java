package com.cassiomolin.patch.web.resource;

import lombok.Data;

@Data
public class BookResourceInput {

    private String title;

    private Integer edition;

    private String author;
}
