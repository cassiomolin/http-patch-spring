package com.cassiomolin.patch.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.json.*;

@Component
@RequiredArgsConstructor
public class PatchHelper {

    private final ObjectMapper mapper;

    public <T> T patch(JsonPatch patch, T targetBean, Class<T> clazz) {
        JsonStructure target = mapper.convertValue(targetBean, JsonStructure.class);
        JsonValue patched = patch.apply(target);
        return mapper.convertValue(patched, clazz);
    }

    public <T> T mergePatch(JsonMergePatch mergePatch, T targetBean, Class<T> clazz) {
        JsonValue target = mapper.convertValue(targetBean, JsonValue.class);
        JsonValue patched = mergePatch.apply(target);
        return mapper.convertValue(patched, clazz);
    }
}
