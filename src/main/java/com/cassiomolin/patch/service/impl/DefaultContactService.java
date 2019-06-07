package com.cassiomolin.patch.service.impl;

import com.cassiomolin.patch.domain.Contact;
import com.cassiomolin.patch.domain.Phone;
import com.cassiomolin.patch.domain.Work;
import com.cassiomolin.patch.service.ContactService;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DefaultContactService implements ContactService {

    private List<Contact> contacts;

    @PostConstruct
    public void init() {

        contacts = new ArrayList<>();

        contacts.add(Contact.builder()
                .id(1L)
                .name("John Appleseed")
                .birthday(LocalDate.parse("1990-01-01"))
                .work(Work.builder().company("Acme").title("Engineer").build())
                .phones(Lists.newArrayList(Phone.builder().phone("0000000000").build()))
                .notes("Cool guy!")
                .favorite(false)
                //.createdDateTime(OffsetDateTime.parse("2019-01-01T10:00:00Z"))
                .build());
    }

    @Override
    public List<Contact> findContacts() {
        return contacts;
    }

    @Override
    public Optional<Contact> findContact(Long id) {
        return contacts.stream()
                .filter(contact -> id.equals(contact.getId()))
                .findFirst();
    }

    @Override
    public void updateContact(Contact contact) {
        contacts.set(contacts.indexOf(contact), contact);
    }
}
