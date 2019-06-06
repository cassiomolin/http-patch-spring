package com.cassiomolin.patch.controller;

import com.cassiomolin.patch.PatchMediaType;
import com.cassiomolin.patch.domain.Book;
import com.cassiomolin.patch.exception.ResourceNotFoundException;
import com.cassiomolin.patch.service.BookService;
import com.cassiomolin.patch.util.PatchHelper;
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

    private final BookService service;

    private final PatchHelper patchHelper;

    /**
     * Retrieve a representation of all books.
     *
     * @return HTTP response with the 200 status code with the operation completed successfully
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Book>> findBooks() {
        List<Book> books = service.findBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * Retrieve a representation of the book with the given id.
     *
     * @param id book identifier
     * @return HTTP response with the 200 status code if the operation completed successfully
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Book> findBook(@PathVariable Long id) {
        Book book = service.findBook(id).orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.ok(book);
    }

    /**
     * Patch a book with the given id using JSON Patch (RFC 6902).
     *
     * @param id            book identifier
     * @param patchDocument JSON Patch document
     * @return HTTP response with the 204 status code if the operation completed successfully
     */
    @PatchMapping(path = "/{id}", consumes = PatchMediaType.APPLICATION_JSON_PATCH_VALUE)
    public ResponseEntity<Book> patchBook(@PathVariable Long id,
                                          @RequestBody JsonPatch patchDocument) {

        Book book = service.findBook(id).orElseThrow(ResourceNotFoundException::new);
        Book patched = patchHelper.patch(patchDocument, book, Book.class);
        service.updateBook(patched);
        return ResponseEntity.noContent().build();
    }

    /**
     * Patch a book with the given id using JSON Merge Patch (RFC 7396).
     *
     * @param id                 book identifier
     * @param mergePatchDocument JSON Merge Patch document
     * @return HTTP response with the 204 status code if the operation completed successfully
     */
    @PatchMapping(path = "/{id}", consumes = PatchMediaType.APPLICATION_MERGE_PATCH_VALUE)
    public ResponseEntity<Book> mergePatchBook(@PathVariable Long id,
                                               @RequestBody JsonMergePatch mergePatchDocument) {

        Book book = service.findBook(id).orElseThrow(ResourceNotFoundException::new);
        Book patched = patchHelper.mergePatch(mergePatchDocument, book, Book.class);
        service.updateBook(patched);
        return ResponseEntity.noContent().build();
    }
}
