package com.cassiomolin.patch.web.controller;

import com.cassiomolin.patch.domain.Contact;
import com.cassiomolin.patch.service.ContactService;
import com.cassiomolin.patch.web.PatchMediaType;
import com.cassiomolin.patch.web.exception.ResourceNotFoundException;
import com.cassiomolin.patch.web.mapper.ContactMapper;
import com.cassiomolin.patch.web.resource.input.ContactResourceInput;
import com.cassiomolin.patch.web.resource.output.ContactResourceOutput;
import com.cassiomolin.patch.web.util.PatchHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.json.JsonMergePatch;
import javax.json.JsonPatch;
import java.util.List;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactMapper mapper;

    private final ContactService service;

    private final PatchHelper patchHelper;

    /**
     * Retrieve a representation of all contacts.
     *
     * @return HTTP response with the 200 status code with the operation completed successfully
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ContactResourceOutput>> findContacts() {

        List<Contact> contacts = service.findContacts();
        return ResponseEntity.ok(mapper.asOutput(contacts));
    }

    /**
     * Retrieve a representation of the contact with the given id.
     *
     * @param id contact identifier
     * @return HTTP response with the 200 status code if the operation completed successfully
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContactResourceOutput> findContact(@PathVariable Long id) {

        Contact contact = service.findContact(id).orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.ok(mapper.asOutput(contact));
    }

    /**
     * Update the contact with the given id.
     *
     * @param id            contact identifier
     * @param resourceInput resource input representation
     * @return HTTP response with the 204 status code if the operation completed successfully
     */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Contact> updateContact(@PathVariable Long id,
                                                 @RequestBody ContactResourceInput resourceInput) {

        Contact contact = service.findContact(id).orElseThrow(ResourceNotFoundException::new);
        mapper.update(resourceInput, contact);
        service.updateContact(contact);

        return ResponseEntity.noContent().build();
    }

    /**
     * Update the contact with the given id using JSON Patch (RFC 6902).
     *
     * @param id            contact identifier
     * @param patchDocument JSON Patch document
     * @return HTTP response with the 204 status code if the operation completed successfully
     */
    @PatchMapping(path = "/{id}", consumes = PatchMediaType.APPLICATION_JSON_PATCH_VALUE)
    public ResponseEntity<Contact> updateContact(@PathVariable Long id,
                                                 @RequestBody JsonPatch patchDocument) {

        Contact contact = service.findContact(id).orElseThrow(ResourceNotFoundException::new);
        ContactResourceInput resourceInput = mapper.asInput(contact);
        ContactResourceInput patched = patchHelper.patch(patchDocument, resourceInput, ContactResourceInput.class);

        mapper.update(patched, contact);
        service.updateContact(contact);

        return ResponseEntity.noContent().build();
    }

    /**
     * Update the contact with the given id using JSON Merge Patch (RFC 7396).
     *
     * @param id                 contact identifier
     * @param mergePatchDocument JSON Merge Patch document
     * @return HTTP response with the 204 status code if the operation completed successfully
     */
    @PatchMapping(path = "/{id}", consumes = PatchMediaType.APPLICATION_MERGE_PATCH_VALUE)
    public ResponseEntity<Contact> updateContact(@PathVariable Long id,
                                                 @RequestBody JsonMergePatch mergePatchDocument) {

        Contact contact = service.findContact(id).orElseThrow(ResourceNotFoundException::new);
        ContactResourceInput resourceInput = mapper.asInput(contact);
        ContactResourceInput patched = patchHelper.mergePatch(mergePatchDocument, resourceInput, ContactResourceInput.class);

        mapper.update(patched, contact);
        service.updateContact(contact);

        return ResponseEntity.noContent().build();
    }
}
