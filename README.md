# Using HTTP `PATCH` with Spring MVC

While `PUT` is pretty handy to perform resource updates, it is meant for _replacing_ the resource state with the representation sent in the request payload.

This article describes one possible approach to support HTTP `PATCH` for performing partial updates in Spring MVC and the challenges that come with it.

### The problem with `PUT`

The `PUT` HTTP method is defined in the [RFC 7231][rfc7231]:

>[**4.3.4.  PUT**]
>
>The `PUT` method requests that the state of the target resource be created or replaced with the state defined by the representation enclosed in the request message payload. [...]

The specification is pretty clear: `PUT` is intended for replacing the state of the resource. When updating a resource with `PUT`, the full representation must be sent in the request payload and it may not be desirable in some situations.

### The challenge with `PATCH`

(coming soon)

### JSON Patch and JSON Merge Patch

(coming soon)

### Creating the controller endpoints

(coming soon)

### Parsing the request payload

(coming soon)

### Applying the patch

(coming soon)


  [put]: https://tools.ietf.org/html/rfc7231#section-4.3.4
  [rfc7231]: https://tools.ietf.org/html/rfc7231