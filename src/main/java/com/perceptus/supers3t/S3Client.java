package com.perceptus.supers3t;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

public class S3Client {
    public static final String DEFAULT_ENDPOINT = "s3.amazonaws.com";

    private static final Vertx vertx            = Vertx.newVertx();

    private final String       awsAccessKey;
    private final String       awsSecretKey;

    private final HttpClient   client;

    public S3Client() {
        this(null, null, DEFAULT_ENDPOINT);
    }

    public S3Client(String accessKey, String secretKey) {
        this(accessKey, secretKey, DEFAULT_ENDPOINT);
    }

    public S3Client(String accessKey, String secretKey, String endpoint) {
        awsAccessKey = accessKey;
        awsSecretKey = secretKey;

        client = vertx.createHttpClient().setHost(endpoint);
    }

    // Direct call (async)
    // -----------

    // GET (bucket, key) -> handler(Data)
    public void get(String bucket,
                    String key,
                    Handler<HttpClientResponse> handler) {
        S3ClientRequest request = createGetRequest(bucket, key, handler);
        request.end();
    }

    // PUT (bucket, key, data) -> handler(Response)
    public void put(String bucket,
                    String key,
                    Buffer data,
                    Handler<HttpClientResponse> handler) {
        S3ClientRequest request = createPutRequest(bucket, key, handler);
        request.end(data);
    }

    // DELETE (bucket, key) -> handler(Response)
    public void delete(String bucket,
                       String key,
                       Handler<HttpClientResponse> handler) {
        S3ClientRequest request = createDeleteRequest(bucket, key, handler);
        request.end();
    }

    // Create requests which can be customized
    // ---------------------------------------

    // create PUT -> requestObject (which you can do stuff with)
    public S3ClientRequest
            createPutRequest(String bucket,
                             String key,
                             Handler<HttpClientResponse> handler) {
        HttpClientRequest httpRequest = client.put("/" + bucket + "/" + key,
                                                   handler);
        return new S3ClientRequest("PUT",
                                   bucket,
                                   key,
                                   httpRequest,
                                   awsAccessKey,
                                   awsSecretKey);
    }

    // create GET -> request Object
    public S3ClientRequest
            createGetRequest(String bucket,
                             String key,
                             Handler<HttpClientResponse> handler) {
        HttpClientRequest httpRequest = client.get("/" + bucket + "/" + key,
                                                   handler);
        return new S3ClientRequest("GET",
                                   bucket,
                                   key,
                                   httpRequest,
                                   awsAccessKey,
                                   awsSecretKey);
    }

    // create DELETE -> request Object
    public S3ClientRequest
            createDeleteRequest(String bucket,
                                String key,
                                Handler<HttpClientResponse> handler) {
        HttpClientRequest httpRequest = client.delete("/" + bucket + "/" + key,
                                                      handler);
        return new S3ClientRequest("DELETE",
                                   bucket,
                                   key,
                                   httpRequest,
                                   awsAccessKey,
                                   awsSecretKey);
    }
}
