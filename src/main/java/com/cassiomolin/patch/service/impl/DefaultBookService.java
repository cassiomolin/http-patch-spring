package com.cassiomolin.patch.service.impl;

import com.cassiomolin.patch.domain.Book;
import com.cassiomolin.patch.service.BookService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DefaultBookService implements BookService {

    private List<Book> books;

    @PostConstruct
    public void init() {

        books = new ArrayList<>();

        books.add(Book.builder()
                .id(1L)
                .title("Foo Adventures")
                .author("John Appleseed")
                .edition(1)
                .build());

        books.add(Book.builder()
                .id(2L)
                .title("Fifty Shades of Green")
                .author("Jane Doe")
                .build());
    }

    @Override
    public List<Book> findBooks() {
        return books;
    }

    @Override
    public Optional<Book> findBook(Long id) {
        return books.stream()
                .filter(book -> id.equals(book.getId()))
                .findFirst();
    }

    @Override
    public void updateBook(Book book) {
        books.set(books.indexOf(book), book);
    }
}
