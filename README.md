# Using HTTP `PATCH` with Spring MVC

While `PUT` is pretty handy to perform resource updates, it is meant for _replacing_ the resource state with the representation sent in the request payload.

This article describes one possible approach to support HTTP `PATCH` for performing partial updates in Spring MVC and the challenges that come with it.

## The problem with `PUT` and the challenge with `PATCH`

The `PUT` HTTP method is defined in the [RFC 7231][rfc7231], one of the documents that currently define the HTTP/1.1 protocol:

>[**4.3.4.  PUT**][put]
>
>The `PUT` method requests that the state of the target resource be created or replaced with the state defined by the representation enclosed in the request message payload. [...]

The specification is pretty clear: `PUT` is intended for _replacing the state of the resource_. When updating a resource with `PUT`, the new representation of the resource must be sent in the request payload and it may not be desirable in some situations, such as performing partial modifications.

To fill this gap, the `PATCH` method was created and it's currently defined in the [RFC 5789][rfc5789]:

> [**2. The PATCH Method**][patch]
>
>The PATCH method requests that a set of changes described in the request entity be applied to the resource identified by the Request-URI. The set of changes is represented in a format called a "patch document" identified by a media type. [...]

The difference between the `PUT` and `PATCH` requests is reflected in the way the server processes the enclosed entity to modify a given resource:

- In a `PUT` request, the payload is a modified version of the resource stored on the server. And the client is requesting that the stored version to be _replaced_.
- In a `PATCH` request, the payload entity contains a set of instructions describing how a resource currently residing on the origin server should be modified to produce a new version.

So the `PATCH` method is suitable for performing _partial modifications_ to a resource (while `PUT` is not).

The `PATCH` method definition, however, doesn't enforce any format for the request payload apart from mentioning that the request payload should contain a set of instructions describing how the resource will be modified.

## JSON Patch and JSON Merge Patch

(coming soon)

## Creating the controller endpoints

(coming soon)

## Parsing the request payload

(coming soon)

## Applying the patch

(coming soon)


  [put]: https://tools.ietf.org/html/rfc7231#section-4.3.4
  [patch]: https://tools.ietf.org/html/rfc5789#section-2
  [rfc7231]: https://tools.ietf.org/html/rfc7231
  [rfc5789]: https://tools.ietf.org/html/rfc5789