package com.cassiomolin.patch.web.mapper;

import com.cassiomolin.patch.domain.Book;
import com.cassiomolin.patch.web.resource.BookResourceInput;
import com.cassiomolin.patch.web.resource.BookResourceOutput;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor"
)
@Component
public class BookMapperImpl implements BookMapper {

    @Override
    public Book asBook(BookResourceInput resourceInput) {
        if ( resourceInput == null ) {
            return null;
        }

        Book book = new Book();

        book.setTitle( resourceInput.getTitle() );
        book.setEdition( resourceInput.getEdition() );
        book.setAuthor( resourceInput.getAuthor() );

        return book;
    }

    @Override
    public BookResourceInput asInput(Book book) {
        if ( book == null ) {
            return null;
        }

        BookResourceInput bookResourceInput = new BookResourceInput();

        bookResourceInput.setTitle( book.getTitle() );
        bookResourceInput.setEdition( book.getEdition() );
        bookResourceInput.setAuthor( book.getAuthor() );

        return bookResourceInput;
    }

    @Override
    public void update(BookResourceInput resourceInput, Book book) {
        if ( resourceInput == null ) {
            return;
        }

        book.setTitle( resourceInput.getTitle() );
        book.setEdition( resourceInput.getEdition() );
        book.setAuthor( resourceInput.getAuthor() );
    }

    @Override
    public BookResourceOutput asOutput(Book book) {
        if ( book == null ) {
            return null;
        }

        BookResourceOutput bookResourceOutput = new BookResourceOutput();

        bookResourceOutput.setId( book.getId() );
        bookResourceOutput.setTitle( book.getTitle() );
        bookResourceOutput.setEdition( book.getEdition() );
        bookResourceOutput.setAuthor( book.getAuthor() );

        return bookResourceOutput;
    }

    @Override
    public List<BookResourceOutput> asOutput(List<Book> books) {
        if ( books == null ) {
            return null;
        }

        List<BookResourceOutput> list = new ArrayList<BookResourceOutput>( books.size() );
        for ( Book book : books ) {
            list.add( asOutput( book ) );
        }

        return list;
    }
}
