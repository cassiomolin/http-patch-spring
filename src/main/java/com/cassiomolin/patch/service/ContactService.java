package com.cassiomolin.patch.service;

import com.cassiomolin.patch.domain.Contact;

import java.util.List;
import java.util.Optional;

public interface ContactService {

    List<Contact> findContacts();

    Optional<Contact> findContact(Long id);

    void updateContact(Contact contact);
}
