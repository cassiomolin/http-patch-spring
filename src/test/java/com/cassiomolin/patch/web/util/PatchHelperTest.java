package com.cassiomolin.patch.web.util;


import com.cassiomolin.patch.config.JacksonConfig;
import com.cassiomolin.patch.domain.Book;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import javax.json.Json;
import javax.json.JsonMergePatch;
import javax.json.JsonPatch;
import javax.json.JsonValue;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import({JacksonConfig.class, PatchHelper.class})
public class PatchHelperTest {

    @Autowired
    private PatchHelper patchHelper;

    @Test
    public void patch_shouldPatchDocument() {

        Book target = Book.builder()
                .title("Foo Adventures")
                .author("John Appleseed")
                .edition(1)
                .build();

        JsonPatch patch = Json.createPatchBuilder()
                .replace("/title", "My Adventures")
                .remove("/edition")
                .replace("/author", "Jane Appleseed")
                .build();

        Book expected = Book.builder()
                .title("My Adventures")
                .author("Jane Appleseed")
                .build();

        Book result = patchHelper.patch(patch, target, Book.class);
        assertThat(result).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void mergePatch_shouldMergePatchDocument() {

        Book target = Book.builder()
                .title("Foo Adventures")
                .author("John Appleseed")
                .build();

        JsonMergePatch mergePatch = Json.createMergePatch(Json.createObjectBuilder()
                .add("title", "My Adventures")
                .add("edition", JsonValue.NULL)
                .add("author", "Jane Appleseed")
                .build());

        Book expected = Book.builder()
                .title("My Adventures")
                .author("Jane Appleseed")
                .build();

        Book result = patchHelper.mergePatch(mergePatch, target, Book.class);
        assertThat(result).isEqualToComparingFieldByField(expected);
    }
}
