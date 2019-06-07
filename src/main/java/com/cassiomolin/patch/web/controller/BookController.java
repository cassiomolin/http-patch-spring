package com.cassiomolin.patch.web.controller;

import com.cassiomolin.patch.PatchMediaType;
import com.cassiomolin.patch.domain.Book;
import com.cassiomolin.patch.web.exception.ResourceNotFoundException;
import com.cassiomolin.patch.web.mapper.BookMapper;
import com.cassiomolin.patch.web.resource.BookResourceInput;
import com.cassiomolin.patch.web.resource.BookResourceOutput;
import com.cassiomolin.patch.service.BookService;
import com.cassiomolin.patch.web.util.PatchHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.json.JsonMergePatch;
import javax.json.JsonPatch;
import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookMapper mapper;

    private final BookService service;

    private final PatchHelper patchHelper;

    /**
     * Retrieve a representation of all books.
     *
     * @return HTTP response with the 200 status code with the operation completed successfully
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BookResourceOutput>> findBooks() {

        List<Book> books = service.findBooks();
        return ResponseEntity.ok(mapper.asOutput(books));
    }

    /**
     * Retrieve a representation of the book with the given id.
     *
     * @param id book identifier
     * @return HTTP response with the 200 status code if the operation completed successfully
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookResourceOutput> findBook(@PathVariable Long id) {

        Book book = service.findBook(id).orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.ok(mapper.asOutput(book));
    }

    /**
     * Update the book with the given id.
     *
     * @param id            book identifier
     * @param resourceInput resource input representation
     * @return HTTP response with the 204 status code if the operation completed successfully
     */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Book> updateBook(@PathVariable Long id,
                                           @RequestBody BookResourceInput resourceInput) {

        Book book = service.findBook(id).orElseThrow(ResourceNotFoundException::new);
        mapper.update(resourceInput, book);
        service.updateBook(book);

        return ResponseEntity.noContent().build();
    }

    /**
     * Update the book with the given id using JSON Patch (RFC 6902).
     *
     * @param id            book identifier
     * @param patchDocument JSON Patch document
     * @return HTTP response with the 204 status code if the operation completed successfully
     */
    @PatchMapping(path = "/{id}", consumes = PatchMediaType.APPLICATION_JSON_PATCH_VALUE)
    public ResponseEntity<Book> updateBook(@PathVariable Long id,
                                           @RequestBody JsonPatch patchDocument) {

        Book book = service.findBook(id).orElseThrow(ResourceNotFoundException::new);
        BookResourceInput resourceInput = mapper.asInput(book);
        BookResourceInput patched = patchHelper.patch(patchDocument, resourceInput, BookResourceInput.class);

        mapper.update(patched, book);
        service.updateBook(book);

        return ResponseEntity.noContent().build();
    }

    /**
     * Update the book with the given id using JSON Merge Patch (RFC 7396).
     *
     * @param id                 book identifier
     * @param mergePatchDocument JSON Merge Patch document
     * @return HTTP response with the 204 status code if the operation completed successfully
     */
    @PatchMapping(path = "/{id}", consumes = PatchMediaType.APPLICATION_MERGE_PATCH_VALUE)
    public ResponseEntity<Book> updateBook(@PathVariable Long id,
                                           @RequestBody JsonMergePatch mergePatchDocument) {

        Book book = service.findBook(id).orElseThrow(ResourceNotFoundException::new);
        BookResourceInput resourceInput = mapper.asInput(book);
        BookResourceInput patched = patchHelper.mergePatch(mergePatchDocument, resourceInput, BookResourceInput.class);

        mapper.update(patched, book);
        service.updateBook(book);

        return ResponseEntity.noContent().build();
    }
}
