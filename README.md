SuperS3t
========

*An asynchronous, vert.x-based S3 library for Java* 

##About

SuperS3t is a super simple library for [Amazon Web Services' Simple Storage Service (S3)](http://aws.amazon.com/s3/). It provides commonly-used functionality for manipulating objects in S3 buckets, such as creating, downloading, and deleting objects. Critically, SuperS3t's API is asynchronous and built on the fast I/O substrate, [Vert.x](http://vertx.io). 

SuperS3t is very fast, and compatible with applications using Vert.x, Netty, or Java's NIO/2 APIs.

##Motivation

We built SuperS3t out of a need of our own; previously our Application had been using the excellent [JetS3t](http://www.jets3t.org) Library to interface with S3. Our application is built with Vert.x, and thus architected with asynchronous calls everywhere. Unfortunately, JetS3t provides only a synchronous API, and it is incompatible with NIO and Vertx. We found ourselves incurring buffer copy penalties as we moved data between NIO buffers and input/outputstreams, and our code got uglier and uglier. 

##Features

SuperS3t is little more than a wrapper around Vertx's [HttpClient API](http://vertx.io/core_manual_java.html#writing-http-clients). You can use it to PUT, GET, and DELETE objects from S3. You use it in much the same way as you would a normal HTTPClient:

    S3Client client = new S3Client(accessKey, secretKey);
    S3ClientRequest putRequest = client.createPutRequest(bucket, key, handler);
    S3ClientRequest getRequest = client.createGetRequest(bucket, key, handler);
    S3ClientRequest deleteRequest = client.createDeleteRequest(bucket, key, handler);

The handlers are `Handler<HttpClientResponse>`, so you can easily get the S3 status code from them. If you need to attach a `Handler<Buffer>` to `event.bodyHandler()` or `event.dataHandler()` and `event.endHandler()` as part of your response handler. 

You may write to or modify these requests as you need to, but must end them to send. For example:

    putRequest.setChunked(true);
    putRequest.write(part1);
    putRequest.write(part2);
    putRequest.end();
    
Note also that the requests are [WriteStreams](http://vertx.io/core_manual_java.html#writestream) and you may [Pump](http://vertx.io/core_manual_java.html#flow-control-streams-and-pumps) data into them as such. 

There are also some shortcut calls to quickly make requests:

    client.put(bucket, key, data, handler);
    client.get(bucket, key, handler);
    client.delete(bucket, key, handler);
    
These end the request as part of the call, sending it off and calling the handler when there is a response. 

The key functionality that SuperS3t provides is handling authentication of requests to and from S3; SuperS3t will automatically sign requests right as they are ready to be sent.

##Build & Test

SuperS3t uses Gradle as its build system, and includes JUnit tests to demonstrate that it works. You can include Rational Option as a subproject in existing build systems, or you can use gradle to generate a jar.