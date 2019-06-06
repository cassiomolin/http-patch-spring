package com.cassiomolin.patch.service;

import com.cassiomolin.patch.domain.Book;

import java.util.List;
import java.util.Optional;

public interface BookService {

    List<Book> findBooks();

    Optional<Book> findBook(Long id);

    void updateBook(Book book);
}
