# Using HTTP `PATCH` in Spring MVC

This article describes an approach to support HTTP `PATCH` for performing partial updates in Spring MVC.

## The problem with `PUT` and the need for `PATCH`

The `PUT` HTTP method is defined in the [RFC 7231][rfc7231], one of the documents that currently define the HTTP/1.1 protocol:

>[**4.3.4.  PUT**][put]
>
>The `PUT` method requests that the state of the target resource be created or replaced with the state defined by the representation enclosed in the request message payload. [...]

So `PUT` is meant for two things:

- Creating resources <sup>*</sup>
- Replacing the state of a given resource.
 
As per definition, the `PUT` method is not meant for performing _partial modifications_ to a resource. And, to fill this gap, the `PATCH` method was created. It is currently defined in the [RFC 5789][rfc5789]:

> [**2. The PATCH Method**][patch]
>
>The `PATCH` method requests that a set of changes described in the request entity be applied to the resource identified by the Request-URI. The set of changes is represented in a format called a "patch document" identified by a media type. [...]

The difference between the `PUT` and `PATCH` requests is reflected in the way the server processes the request payload to modify a given resource:

- In a `PUT` request, the payload is a modified version of the resource stored on the server. And the client is requesting the stored version to be _replaced_ with the new version.
- In a `PATCH` request, the request payload contains a set of instructions describing how a resource currently stored on the server should be modified to produce a new version.

So the `PATCH` method is suitable for performing _partial modifications_ to a resource (while `PUT` is not).

## Describing how the resource will be modified

The `PATCH` method definition, however, doesn't enforce any format for the request payload apart from mentioning that the request payload should contain a set of instructions describing how the resource will be modified and that such set of instructions is identified by a media type.

Let's have a look at some formats for expressing how a resource will be `PATCH`ed:

### JSON Patch

JSON Patch is a format for expressing a sequence of operations to be applied to a JSON document. 

It is defined in the [RFC 6902][rfc6902] and is identified by the `application/json-patch+json` media type.

(add examples)

### JSON Merge Patch

JSON Merge Patch defines a format and processing rules for applying operations to a JSON document that are based upon specific content of the target document.

It is defined in the [RFC 7396][rfc7396] is identified by the `application/merge-patch+json` media type.

(add examples)

## Java API for JSON Processing

JSON-P 1.1, also known as Java API for JSON Processing, brought official support for JSON Patch and JSON Merge Patch to Java EE: 

- [`JsonPatch`][javax.json.JsonPatch]: Represents an implementation of JSON Patch
- [`JsonMergePatch`][javax.json.JsonMergePatch]: Represents an implementation of JSON Merge Patch

JSON-P 1.1 is, however, just an API (see the [`javax.json`][javax.json] package for reference). If we want to work with it, we need a concrete implementation such as [Apache Johnzon][johnzon]: 

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

Spring MVC, however, doesn't know how to create instances of [`JsonPatch`][javax.json.JsonPatch] or [`JsonMergePatch`][javax.json.JsonMergePatch]. So we need to provide a custom [`HttpMessageConverter<T>`][org.springframework.http.converter.HttpMessageConverter] for each type. Fortunately it's pretty straightforward:

```java
@Component
public class JsonPatchHttpMessageConverter extends AbstractHttpMessageConverter<JsonPatch> {

    public JsonPatchHttpMessageConverter() {
        super(MediaType.valueOf("application/json-patch+json"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return JsonPatch.class.isAssignableFrom(clazz);
    }

    @Override
    protected JsonPatch readInternal(Class<? extends JsonPatch> clazz, HttpInputMessage inputMessage)
            throws HttpMessageNotReadableException {

        try (JsonReader reader = Json.createReader(inputMessage.getBody())) {
            return Json.createPatch(reader.readArray());
        } catch (Exception e) {
            throw new HttpMessageNotReadableException(e.getMessage(), inputMessage);
        }
    }

    @Override
    protected void writeInternal(JsonPatch jsonPatch, HttpOutputMessage outputMessage)
            throws HttpMessageNotWritableException {

        try (JsonWriter writer = Json.createWriter(outputMessage.getBody())) {
            writer.write(jsonPatch.toJsonArray());
        } catch (Exception e) {
            throw new HttpMessageNotWritableException(e.getMessage(), e);
        }
    }
}
```

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
    return ResponseEntity.noContent().build();
}
```

```java
@PatchMapping(path = "/{id}", consumes = "application/merge-patch+json")
public ResponseEntity<Void> updateContact(@PathVariable Long id,
                                          @RequestBody JsonMergePatch mergePatchDocument) {
    ...
    return ResponseEntity.noContent().build();
}
```

## Applying the patch

(coming soon)

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