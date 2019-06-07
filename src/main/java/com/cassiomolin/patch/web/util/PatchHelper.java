package com.cassiomolin.patch.web.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.json.JsonMergePatch;
import javax.json.JsonPatch;
import javax.json.JsonStructure;
import javax.json.JsonValue;

@Component
@RequiredArgsConstructor
public class PatchHelper {

    private final ObjectMapper mapper;

    /**
     * Performs a JSON Patch operation.
     *
     * @param patch      JSON Patch document
     * @param targetBean object that will be patched
     * @param clazz      class of the object the will be patched
     * @param <T>
     * @return patched object
     */
    public <T> T patch(JsonPatch patch, T targetBean, Class<T> clazz) {
        JsonStructure target = mapper.convertValue(targetBean, JsonStructure.class);
        JsonValue patched = patch.apply(target);
        return mapper.convertValue(patched, clazz);
    }

    /**
     * Performs a JSON Merge Patch operation
     *
     * @param mergePatch JSON Merge Patch document
     * @param targetBean object that will be patched
     * @param clazz      class of the object the will be patched
     * @param <T>
     * @return patched object
     */
    public <T> T mergePatch(JsonMergePatch mergePatch, T targetBean, Class<T> clazz) {
        JsonValue target = mapper.convertValue(targetBean, JsonValue.class);
        JsonValue patched = mergePatch.apply(target);
        return mapper.convertValue(patched, clazz);
    }
}
