# Using HTTP `PATCH` in Spring MVC

This article describes one possible approach to support HTTP `PATCH` for performing partial updates in Spring MVC and the challenges that come with it.

## The problem with `PUT` and the need for `PATCH`

The `PUT` HTTP method is defined in the [RFC 7231][rfc7231], one of the documents that currently define the HTTP/1.1 protocol:

>[**4.3.4.  PUT**][put]
>
>The `PUT` method requests that the state of the target resource be created or replaced with the state defined by the representation enclosed in the request message payload. [...]

So `PUT` is meant for two things:

- Creating resources. However you may not want to support `PUT` for this purpose if you rely on the server to generate identifiers for your resources (see my [answer][so.56241060] on Stack Overflow for details on this).
- Replacing the state of a given resource.
 
As per definition, the `PUT` method is not meant for performing partial modifications to a resource.

To fill this gap, the `PATCH` method was created and it's currently defined in the [RFC 5789][rfc5789]:

> [**2. The PATCH Method**][patch]
>
>The `PATCH` method requests that a set of changes described in the request entity be applied to the resource identified by the Request-URI. The set of changes is represented in a format called a "patch document" identified by a media type. [...]

The difference between the `PUT` and `PATCH` requests is reflected in the way the server processes the request payload to modify a given resource:

- In a `PUT` request, the payload is a modified version of the resource stored on the server. And the client is requesting the stored version to be _replaced_ with the new version.
- In a `PATCH` request, the request payload contains a set of instructions describing how a resource currently stored on the server should be modified to produce a new version.

So the `PATCH` method is suitable for performing _partial modifications_ to a resource (while `PUT` is not).

The `PATCH` method definition, however, doesn't enforce any format for the request payload apart from mentioning that the request payload should contain a set of instructions describing how the resource will be modified and that such set of instructions is identified by a media type.

Some standard and suitable formats for the `PATCH`ing are listed below:

- JSON Patch, which is defined in the [RFC 6902][rfc6902] and is identified by the `application/json-patch+json` media type.
- JSON Merge Patch, which is defined in the [RFC 7396][rfc7396] is identified by the `application/merge-patch+json` media type.

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
  [rfc6902]: https://tools.ietf.org/html/rfc6902
  [rfc7396]: https://tools.ietf.org/html/rfc7396
  [so.56241060]: https://stackoverflow.com/a/56241060/1426227