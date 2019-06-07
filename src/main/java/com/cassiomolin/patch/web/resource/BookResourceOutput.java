package com.cassiomolin.patch.web.resource;

import lombok.Data;

@Data
public class BookResourceOutput {

    private Long id;

    private String title;

    private Integer edition;

    private String author;
}
