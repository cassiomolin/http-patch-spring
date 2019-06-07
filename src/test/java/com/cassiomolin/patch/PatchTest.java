package com.cassiomolin.patch;

import com.cassiomolin.patch.web.PatchMediaType;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import java.nio.charset.Charset;

import static com.jayway.jsonassert.JsonAssert.with;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PatchTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @Test
    @SneakyThrows
    public void updateBook_shouldSucceed() {

        Long id = 1L;
        ResponseEntity<String> patchResponse = updateBook(id, fromFile("json/put.json"));

        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(patchResponse.getBody()).isNull();

        ResponseEntity<String> findResponse = findBook(id);
        assertThat(findResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        with(findResponse.getBody())
                .assertThat("$.*", hasSize(4))
                .assertThat("$.id", is(id.intValue()))
                .assertThat("$.title", is("My Adventures"))
                .assertThat("$.edition", is(nullValue()))
                .assertThat("$.author", is("Jane Appleseed"));
    }

    @Test
    @SneakyThrows
    public void updateBookUsingJsonPatch_shouldSucceed() {

        Long id = 1L;
        ResponseEntity<String> patchResponse = updateBookUsingJsonPatch(id, fromFile("json/json-patch.json"));

        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(patchResponse.getBody()).isNull();

        ResponseEntity<String> findResponse = findBook(id);
        assertThat(findResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        with(findResponse.getBody())
                .assertThat("$.*", hasSize(4))
                .assertThat("$.id", is(id.intValue()))
                .assertThat("$.title", is("My Adventures"))
                .assertThat("$.edition", is(nullValue()))
                .assertThat("$.author", is("Jane Appleseed"));
    }

    @Test
    @SneakyThrows
    public void updateBookUsingJsonMergePatch_shouldSucceed() {

        Long id = 1L;
        ResponseEntity<String> patchResponse = updateBookUsingJsonMergePatch(id, fromFile("json/merge-patch.json"));

        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(patchResponse.getBody()).isNull();

        ResponseEntity<String> findResponse = findBook(id);
        assertThat(findResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        with(findResponse.getBody())
                .assertThat("$.*", hasSize(4))
                .assertThat("$.id", is(id.intValue()))
                .assertThat("$.title", is("My Adventures"))
                .assertThat("$.edition", is(nullValue()))
                .assertThat("$.author", is("Jane Appleseed"));
    }

    private ResponseEntity<String> findBook(Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange("/books/{id}", HttpMethod.GET, new HttpEntity<>(headers), String.class, id);
    }

    private ResponseEntity<String> updateBook(Long id, Object payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange("/books/{id}", HttpMethod.PUT, new HttpEntity<>(payload, headers), String.class, id);
    }

    private ResponseEntity<String> updateBookUsingJsonPatch(Long id, Object payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PatchMediaType.APPLICATION_JSON_PATCH);
        return restTemplate.exchange("/books/{id}", HttpMethod.PATCH, new HttpEntity<>(payload, headers), String.class, id);
    }

    private ResponseEntity<String> updateBookUsingJsonMergePatch(Long id, Object payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PatchMediaType.APPLICATION_MERGE_PATCH);
        return restTemplate.exchange("/books/{id}", HttpMethod.PATCH, new HttpEntity<>(payload, headers), String.class, id);
    }

    @SneakyThrows
    private String fromFile(String path) {
        return StreamUtils.copyToString(new ClassPathResource(path).getInputStream(), Charset.defaultCharset());
    }
}
