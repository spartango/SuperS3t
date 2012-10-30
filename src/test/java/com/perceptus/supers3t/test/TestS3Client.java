/**
 * 
 */
package com.perceptus.supers3t.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClientResponse;

import com.perceptus.supers3t.S3Client;
import com.perceptus.supers3t.S3ClientRequest;

/**
 * @author spartango
 * 
 */
public class TestS3Client {
    private static final Logger logger         = LoggerFactory.getLogger(TestS3Client.class);

    private static final int    expectedLength = 4096;                                       // bytes

    private static final String accessKey      = "";
    private static final String secretKey      = "";

    private static final String testBucket     = "Perceptus";

    private S3Client            client;

    /**
     * @throws java.lang.Exception
     */
    @Before public void setUp() throws Exception {
        client = new S3Client(accessKey, secretKey);
    }

    /**
     * Test method for
     * {@link com.perceptus.supers3t.S3Client#get(java.lang.String, java.lang.String, org.vertx.java.core.Handler)}
     * .
     */
    public void testGet() {
        // Put a test object up

        // Things we need from the response
        final Buffer responseBody = new Buffer();
        final AtomicInteger responseCode = new AtomicInteger();

        synchronized (responseBody) {
            client.get(testBucket,
                       "testObject",
                       new Handler<HttpClientResponse>() {

                           @Override public void
                                   handle(HttpClientResponse event) {
                               responseCode.set(event.statusCode);
                               if (event.statusCode != 200) {
                                   synchronized (responseBody) {
                                       // This is a failed request
                                       logger.error("Bad response: "
                                                    + event.statusCode);
                                       responseBody.notify();
                                   }
                                   return;
                               }

                               // Try to download the body
                               event.bodyHandler(new Handler<Buffer>() {
                                   @Override public void handle(Buffer event) {
                                       // Append the body on
                                       synchronized (responseBody) {
                                           logger.info("Got body: "
                                                       + event.length()
                                                       + "bytes");
                                           responseBody.appendBuffer(event);
                                           responseBody.notify();
                                       }
                                   }
                               });
                           }
                       });

            try {
                responseBody.wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Check that we got the right response...
        Assert.assertEquals(200, responseCode.intValue());
        Assert.assertEquals(expectedLength, responseBody.length());
    }

    /**
     * Test method for
     * {@link com.perceptus.supers3t.S3Client#put(java.lang.String, java.lang.String, org.vertx.java.core.buffer.Buffer, org.vertx.java.core.Handler)}
     * .
     */
    public void testPut() {
        // Things we need from the response
        final AtomicInteger responseCode = new AtomicInteger();

        synchronized (responseCode) {
            client.put(testBucket,
                       "testObject",
                       new Buffer(new byte[expectedLength]),
                       new Handler<HttpClientResponse>() {
                           @Override public void
                                   handle(HttpClientResponse event) {
                               synchronized (responseCode) {
                                   responseCode.set(event.statusCode);
                                   responseCode.notify();
                               }
                           }
                       });

            try {
                responseCode.wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Check that we got the right response...
        Assert.assertEquals(200, responseCode.intValue());
    }

    /**
     * Test method for
     * {@link com.perceptus.supers3t.S3Client#delete(java.lang.String, java.lang.String, org.vertx.java.core.Handler)}
     * .
     */
    public void testDelete() {
        // Things we need from the response
        final AtomicInteger responseCode = new AtomicInteger();

        synchronized (responseCode) {
            client.delete(testBucket,
                          "testObject",
                          new Handler<HttpClientResponse>() {
                              @Override public void
                                      handle(HttpClientResponse event) {
                                  synchronized (responseCode) {
                                      responseCode.set(event.statusCode);
                                      responseCode.notify();
                                  }
                              }
                          });

            try {
                responseCode.wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Check that we got the right response...
        Assert.assertEquals(204, responseCode.intValue());
    }

    /**
     * Test method for
     * {@link com.perceptus.supers3t.S3Client#createPutRequest(java.lang.String, java.lang.String, org.vertx.java.core.Handler)}
     * .
     */
    @Test public void testCreatePutRequest() {
        // Things we need from the response
        final AtomicInteger responseCode = new AtomicInteger();

        final Buffer toUpload = new Buffer(new byte[expectedLength]);
        synchronized (responseCode) {
            S3ClientRequest request = client.createPutRequest(testBucket,
                                                              "testObject",
                                                              new Handler<HttpClientResponse>() {
                                                                  @Override public void
                                                                          handle(HttpClientResponse event) {
                                                                      logger.info("Response message: "
                                                                                  + event.statusMessage);

                                                                      synchronized (responseCode) {
                                                                          responseCode.set(event.statusCode);
                                                                          responseCode.notify();
                                                                      }
                                                                  }
                                                              });
            request.end(toUpload);
            try {
                responseCode.wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Check that we got the right response...
        Assert.assertEquals(200, responseCode.intValue());
    }

    /**
     * Test method for
     * {@link com.perceptus.supers3t.S3Client#createGetRequest(java.lang.String, java.lang.String, org.vertx.java.core.Handler)}
     * .
     */
    @Test public void testCreateGetRequest() {
        // Do a testPut first
        testPut();

        // Things we need from the response
        final Buffer responseBody = new Buffer();
        final AtomicInteger responseCode = new AtomicInteger();

        synchronized (responseBody) {
            S3ClientRequest request = client.createGetRequest(testBucket,
                                                              "testObject",
                                                              new Handler<HttpClientResponse>() {

                                                                  @Override public void
                                                                          handle(HttpClientResponse event) {
                                                                      responseCode.set(event.statusCode);
                                                                      if (event.statusCode != 200) {
                                                                          synchronized (responseBody) {
                                                                              // This
                                                                              // is
                                                                              // a
                                                                              // failed
                                                                              // request
                                                                              logger.error("Bad response: "
                                                                                           + event.statusCode);
                                                                              responseBody.notify();
                                                                          }
                                                                          return;
                                                                      }

                                                                      // Try to
                                                                      // download
                                                                      // the
                                                                      // body
                                                                      event.bodyHandler(new Handler<Buffer>() {
                                                                          @Override public void
                                                                                  handle(Buffer event) {
                                                                              // Append
                                                                              // the
                                                                              // body
                                                                              // on
                                                                              synchronized (responseBody) {
                                                                                  logger.info("Got body: "
                                                                                              + event.length()
                                                                                              + "bytes");
                                                                                  responseBody.appendBuffer(event);
                                                                                  responseBody.notify();
                                                                              }
                                                                          }
                                                                      });
                                                                  }
                                                              });
            request.end();
            try {
                responseBody.wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Check that we got the right response...
        Assert.assertEquals(200, responseCode.intValue());
        Assert.assertEquals(expectedLength, responseBody.length());
    }

    /**
     * Test method for
     * {@link com.perceptus.supers3t.S3Client#createDeleteRequest(java.lang.String, java.lang.String, org.vertx.java.core.Handler)}
     * .
     */
    @Test public void testCreateDeleteRequest() {
        // Test put may be required
        testPut();

        // Things we need from the response
        final AtomicInteger responseCode = new AtomicInteger();

        synchronized (responseCode) {
            S3ClientRequest request = client.createDeleteRequest(testBucket,
                                                                 "testObject",
                                                                 new Handler<HttpClientResponse>() {
                                                                     @Override public void
                                                                             handle(HttpClientResponse event) {
                                                                         synchronized (responseCode) {
                                                                             responseCode.set(event.statusCode);
                                                                             responseCode.notify();
                                                                         }
                                                                     }
                                                                 });
            request.end();

            try {
                responseCode.wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Check that we got the right response...
        Assert.assertEquals(204, responseCode.intValue());
    }

    @Test public void testLifeCycle() {
        testPut();
        testGet();
        testDelete();
    }
}
