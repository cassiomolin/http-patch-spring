# Using HTTP `PATCH` in Spring MVC

Here I intend to describe an approach to support HTTP `PATCH` with JSON Patch and JSON Merge Patch for performing partial updates to resources in Spring MVC.

## The problem with `PUT` and the need for `PATCH`

Consider, for example, we are creating an API to manage contacts. On the server, we have a resource that can be represented with the following JSON document:

```json
{
  "id": 1,
  "name": "John Appleseed",
  "work": {
    "title": "Engineer",
    "company": "Acme"
  },
  "phones": [
    {
      "phone": "0000000000",
      "type": "mobile"
    }
  ]
}
```

Now consider we want to update this resource. John has been promoted as a senior engineer and we want to keep our contact list updated. We could achieve it with a `PUT` request:

```http
PUT /contacts/1 HTTP/1.1
Host: example.org
Content-Type: application/json

{
  "id": 1,
  "name": "John Appleseed",
  "work": {
    "title": "Senior Engineer",
    "company": "Acme"
  },
  "phones": [
    {
      "phone": "0000000000",
      "type": "mobile"
    }
  ]
}
```

With `PUT`, however, we have to send the full representation of the resource even when we need to update a _single_ field of a resource, which may not be desirable in some situations.

Let's have a look on how the `PUT` HTTP method is defined in the [RFC 7231][rfc7231], one of the documents that currently define the HTTP/1.1 protocol:

>[**4.3.4.  PUT**][put]
>
>The `PUT` method requests that the state of the target resource be created or replaced with the state defined by the representation enclosed in the request message payload. [...]

As per definition, the `PUT` method is meant to be used for:

- Creating resources <sup>*</sup> and/or;
- Replacing the state of a given resource.
 
It's not meant for performing _partial modifications_ to resource. To fill this gap, the `PATCH` method was created a it is currently defined in the [RFC 5789][rfc5789]:

> [**2. The PATCH Method**][patch]
>
>The `PATCH` method requests that a set of changes described in the request entity be applied to the resource identified by the Request-URI. The set of changes is represented in a format called a "patch document" identified by a media type. [...]

The difference between the `PUT` and `PATCH` requests is reflected in the way the server processes the request payload to modify a given resource:

- In a `PUT` request, the payload is a modified version of the resource stored on the server. And the client is requesting the stored version to be _replaced_ with the new version.
- In a `PATCH` request, the request payload contains a set of instructions describing how a resource currently stored on the server should be modified to produce a new version.

## Describing how the resource will be modified

The `PATCH` method definition, however, doesn't enforce any format for the request payload apart from mentioning that the request payload should contain a set of instructions describing how the resource will be modified and that set of instructions is identified by a media type.

Let's have a look at some formats for describing how a resource will be `PATCH`ed:

### JSON Patch

JSON Patch is a format for expressing a sequence of operations to be applied to a JSON document. It is defined in the [RFC 6902][rfc6902] and is identified by the `application/json-patch+json` media type.

A request to update the John's job title could be as follows:

```http
PATCH /contacts/1 HTTP/1.1
Host: example.org
Content-Type: application/json-patch+json

[
  { "op": "replace", "path": "/work/title", "value": "Senior Engineer" }
]
```

### JSON Merge Patch

JSON Merge Patch defines a format and processing rules for applying operations to a JSON document that are based upon specific content of the target document. It is defined in the [RFC 7396][rfc7396] is identified by the `application/merge-patch+json` media type.

A request to update the John's job title could be as follows:

```http
PATCH /contacts/1 HTTP/1.1
Host: example.org
Content-Type: application/merge-patch+json

{
  "work": {
    "title": "Senior Engineer"
  }
}
```

## Java API for JSON Processing

JSON-P 1.1, also known as Java API for JSON Processing, brought official support for JSON Patch and JSON Merge Patch to Java EE: 

- [`JsonPatch`][javax.json.JsonPatch]: Represents an implementation of JSON Patch
- [`JsonMergePatch`][javax.json.JsonMergePatch]: Represents an implementation of JSON Merge Patch

To patch using JSON Patch, we would have the following: 

```java
// Target JSON document to be patched
JsonObject target = ...;

// Create JSON Patch document
JsonPatch jsonPatch = Json.createPatchBuilder()
        .replace("/work/title", "Senior Engineer")
        .build();

// Apply patch to the target document
JsonValue patched = jsonPatch.apply(target);
```

And to patch using JSON Merge Patch, we would have the following:

```java
// Target JSON document to be patched
JsonObject target = ...;

// Create JSON Merge Patch document
JsonMergePatch mergePatch = Json.createMergePatch(Json.createObjectBuilder()
        .add("work", Json.createObjectBuilder()
                .add("title", "Senior Engineer"))
        .build());

// Apply patch to the target document
JsonValue patched = mergePatch.apply(target);
```

Having said that, it's important to mention that JSON-P 1.1 is only an API (see the [`javax.json`][javax.json] package for reference). If we want to work with it, we need a concrete implementation such as [Apache Johnzon][johnzon]: 

```xml
<dependency>
  <groupId>org.apache.johnzon</groupId>
  <artifactId>johnzon-core</artifactId>
  <version>${johnzon.version}</version>
</dependency>
```

## Parsing the request payload

For an incoming request with the `application/json-patch+json` content type, we want to read the payload as an instance of [`JsonPatch`][javax.json.JsonPatch]. 

And for an incoming request with the `application/merge-patch+json` content type, we want to read the payload as an instance of [`JsonMergePatch`][javax.json.JsonMergePatch].

Spring MVC, however, doesn't know how to create instances of [`JsonPatch`][javax.json.JsonPatch] or [`JsonMergePatch`][javax.json.JsonMergePatch]. So we need to provide a custom [`HttpMessageConverter<T>`][org.springframework.http.converter.HttpMessageConverter] for each type. Fortunately it's pretty straightforward.

For convenience, let's extend `AbstractHttpMessageConverter` and annotate our implementation with `@Component`, so Spring can pick it up:

```java
@Component
public class JsonPatchHttpMessageConverter extends AbstractHttpMessageConverter<JsonPatch> {
   ...
}
```

Our constructor will invoke the parent's constructor indicating the supported media type:

```java
public JsonPatchHttpMessageConverter() {
    super(MediaType.valueOf("application/json-patch+json"));
}
```

Then we indicate that this converted will support the [`JsonPatch`][javax.json.JsonPatch] class:

```java
@Override
protected boolean supports(Class<?> clazz) {
    return JsonPatch.class.isAssignableFrom(clazz);
}
```

Then we'll implement how the converter will read the HTTP request payload and convert it to a [`JsonPatch`][javax.json.JsonPatch] instance:

```java
@Override
protected JsonPatch readInternal(Class<? extends JsonPatch> clazz, HttpInputMessage inputMessage)
        throws HttpMessageNotReadableException {

    try (JsonReader reader = Json.createReader(inputMessage.getBody())) {
        return Json.createPatch(reader.readArray());
    } catch (Exception e) {
        throw new HttpMessageNotReadableException(e.getMessage(), inputMessage);
    }
}
```

It's unlikely we'll write [`JsonPatch`][javax.json.JsonPatch] instances to the responses, but we could implement it as follows:

```java
@Override
protected void writeInternal(JsonPatch jsonPatch, HttpOutputMessage outputMessage)
        throws HttpMessageNotWritableException {

    try (JsonWriter writer = Json.createWriter(outputMessage.getBody())) {
        writer.write(jsonPatch.toJsonArray());
    } catch (Exception e) {
        throw new HttpMessageNotWritableException(e.getMessage(), e);
    }
}
```

The [`JsonMergePatch`][javax.json.JsonMergePatch] message converter is pretty much the same:

```java
@Component
public class JsonMergePatchHttpMessageConverter extends AbstractHttpMessageConverter<JsonMergePatch> {

    public JsonMergePatchHttpMessageConverter() {
        super(MediaType.valueOf("application/merge-patch+json"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return JsonMergePatch.class.isAssignableFrom(clazz);
    }

    @Override
    protected JsonMergePatch readInternal(Class<? extends JsonMergePatch> clazz, HttpInputMessage inputMessage)
            throws HttpMessageNotReadableException {

        try (JsonReader reader = Json.createReader(inputMessage.getBody())) {
            return Json.createMergePatch(reader.readValue());
        } catch (Exception e) {
            throw new HttpMessageNotReadableException(e.getMessage(), inputMessage);
        }
    }

    @Override
    protected void writeInternal(JsonMergePatch jsonMergePatch, HttpOutputMessage outputMessage)
            throws HttpMessageNotWritableException {

        try (JsonWriter writer = Json.createWriter(outputMessage.getBody())) {
            writer.write(jsonMergePatch.toJsonValue());
        } catch (Exception e) {
            throw new HttpMessageNotWritableException(e.getMessage(), e);
        }
    }
}
```

## Creating the controller endpoints

Whith HTTP message converters in place, we can receive [`JsonPatch`][javax.json.JsonPatch] and [`JsonMergePatch`][javax.json.JsonMergePatch] as method arguments in our controller methods, annotated with [`@RequestBody`][org.springframework.web.bind.annotation.RequestBody]:

```java
@PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
public ResponseEntity<Void> updateContact(@PathVariable Long id,
                                          @RequestBody JsonPatch patchDocument) {
    ...
}
```

```java
@PatchMapping(path = "/{id}", consumes = "application/merge-patch+json")
public ResponseEntity<Void> updateContact(@PathVariable Long id,
                                          @RequestBody JsonMergePatch mergePatchDocument) {
    ...
}
```

## Applying the patch

It's important to keep in mind that both JSON Patch and JSON Merge Patch operate over JSON documents. 

So, to apply the patch to Java beans, we first need to convert the Java bean to a JSON-P type, such as `JsonStructure` or `JsonValue`. Then apply the patch to it and convert the patched document back to a Java bean.

These conversions could be handled by Jackson, which provides an [extension module][jackson-datatype-jsr353] to work with JSON-P types, so that we can read JSON as `JsonValue`s and write `JsonValue`s as JSON as part of normal Jackson processing:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr353</artifactId>
    <version>${jackson.version}</version>
</dependency>
```

Here's what the method to apply a JSON Patch could be like:

```java
public <T> T patch(JsonPatch patch, T targetBean, Class<T> beanClass) {
    
    // Convert the Java bean to a JSON document
    JsonStructure target = mapper.convertValue(targetBean, JsonStructure.class);
    
    // Apply the JSON Patch to the JSON document
    JsonValue patched = patch.apply(target);
    
    // Convert the JSON document to a Java bean
    return mapper.convertValue(patched, beanClass);
}
```

And here's what the method to apply a JSON Merge Patch could be like:

```java
public <T> T mergePatch(JsonMergePatch mergePatch, T targetBean, Class<T> beanClass) {
    
    // Convert the Java bean to a JSON document
    JsonValue target = mapper.convertValue(targetBean, JsonValue.class);
    
    // Apply the JSON Merge Patch to the JSON document
    JsonValue patched = mergePatch.apply(target);
    
    // Convert the JSON document to a Java bean
    return mapper.convertValue(patched, beanClass);
}
```

And here's what the method controller implementation will be like:

```java
@PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
public ResponseEntity<Void> updateContact(@PathVariable Long id,
                                          @RequestBody JsonPatch patchDocument) {

    // Find the model that will be patched
    Contact contact = service.findContact(id).orElseThrow(ResourceNotFoundException::new);
    
    // Apply the patch
    Contact patched = patch(patchDocument, contact, Contact.class);
    
    // Persist the changes
    service.updateContact(patched);

    // Return 204 to indicate the operation has succeeded
    return ResponseEntity.noContent().build();
}
```

For JSON Merge Patch, it's pretty much the same:

```java
@PatchMapping(path = "/{id}", consumes = "application/merge-patch+json")
public ResponseEntity<Void> updateContact(@PathVariable Long id,
                                          @RequestBody JsonMergePatch mergePatchDocument) {

    // Find the model that will be patched
    Contact contact = service.findContact(id).orElseThrow(ResourceNotFoundException::new);
    
    // Apply the patch
    Contact patched = mergePatch(mergePatchDocument, contact, Contact.class);
    
    // Persist the changes
    service.updateContact(patched);

    // Return 204 to indicate the operation has succeeded
    return ResponseEntity.noContent().build();
}
```

## Validating the patch

(coming soon)

---

<sup>*</sup> You may not want to support `PUT` for creating resources if you rely on the server to generate identifiers for your resources. See [my answer][so.56241060] on Stack Overflow for details on this.

  [put]: https://tools.ietf.org/html/rfc7231#section-4.3.4
  [patch]: https://tools.ietf.org/html/rfc5789#section-2
  [rfc7231]: https://tools.ietf.org/html/rfc7231
  [rfc5789]: https://tools.ietf.org/html/rfc5789
  [rfc6902]: https://tools.ietf.org/html/rfc6902
  [rfc7396]: https://tools.ietf.org/html/rfc7396
  [so.56241060]: https://stackoverflow.com/a/56241060/1426227
  [javax.json]: https://javaee.github.io/javaee-spec/javadocs/javax/json/package-summary.html
  [johnzon]: https://johnzon.apache.org/
  [javax.json.JsonPatch]: https://javaee.github.io/javaee-spec/javadocs/javax/json/JsonPatch.html
  [javax.json.JsonMergePatch]: https://javaee.github.io/javaee-spec/javadocs/javax/json/JsonMergePatch.html
  [org.springframework.http.converter.HttpMessageConverter]: https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/http/converter/HttpMessageConverter.html
  [org.springframework.web.bind.annotation.RequestBody]: https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestBody.html
  [jackson-datatype-jsr353]: https://github.com/FasterXML/jackson-datatype-jsr353