package org.openlmis.esw.extension;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusLogEntryDto {
  private UUID authorId;

  public UUID getAuthorId() {
    return authorId;
  }

  public void setAuthorId(UUID authorId) {
    this.authorId = authorId;
  }

  @Override
  public String toString() {
    return "StatusLogEntryDto{" +
        "authorId=" + authorId +
        '}';
  }
}