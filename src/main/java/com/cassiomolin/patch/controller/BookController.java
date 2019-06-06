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

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Book>> findBooks() {
        List<Book> books = service.findBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Book> findBook(@PathVariable Long id) {
        Book book = service.findBook(id).orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.ok(book);
    }

    @PatchMapping(path = "/{id}", consumes = PatchMediaType.APPLICATION_JSON_PATCH_VALUE)
    public ResponseEntity<Object> patchBook(@PathVariable Long id,
                                            @RequestBody JsonPatch patchDocument) {

        Book book = service.findBook(id).orElseThrow(ResourceNotFoundException::new);
        Book patched = patchHelper.patch(patchDocument, book, Book.class);
        service.updateBook(patched);
        return ResponseEntity.ok(patchDocument);
    }

    @PatchMapping(path = "/{id}", consumes = PatchMediaType.APPLICATION_MERGE_PATCH_VALUE)
    public ResponseEntity<Object> mergePatchBook(@PathVariable Long id,
                                                 @RequestBody JsonMergePatch mergePatchDocument) {

        Book book = service.findBook(id).orElseThrow(ResourceNotFoundException::new);
        Book patched = patchHelper.mergePatch(mergePatchDocument, book, Book.class);
        service.updateBook(patched);
        return ResponseEntity.ok(mergePatchDocument);
    }
}
