package com.cassiomolin.patch.web.mapper;

import com.cassiomolin.patch.domain.Book;
import com.cassiomolin.patch.web.resource.BookResourceInput;
import com.cassiomolin.patch.web.resource.BookResourceOutput;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper
public interface BookMapper {

    Book asBook(BookResourceInput resourceInput);

    BookResourceInput asInput(Book book);

    void update(BookResourceInput resourceInput, @MappingTarget Book book);

    BookResourceOutput asOutput(Book book);

    List<BookResourceOutput> asOutput(List<Book> books);
}
