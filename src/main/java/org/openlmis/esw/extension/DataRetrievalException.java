package org.openlmis.esw.extension;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Signals we were unable to retrieve reference data
 * due to a communication error.
 */
@Getter
public class DataRetrievalException extends RuntimeException {
  private final String resource;
  private final HttpStatus status;
  private final String response;

  /**
   * Constructs the exception.
   *
   * @param resource the resource that we were trying to retrieve
   * @param ex       exception with status code and response body from server
   */
  DataRetrievalException(String resource, HttpStatusCodeException ex) {
    this(resource, ex.getStatusCode(), ex.getResponseBodyAsString());
  }

  /**
   * Constructs the exception.
   *
   * @param resource the resource that we were trying to retrieve
   * @param status   the http status that was returned
   * @param response the response from referencedata service
   */
  DataRetrievalException(String resource, HttpStatus status, String response) {
    super(String.format("Unable to retrieve %s. Error code: %d, response message: %s",
        resource, status.value(), response));
    this.resource = resource;
    this.status = status;
    this.response = response;
  }
}
