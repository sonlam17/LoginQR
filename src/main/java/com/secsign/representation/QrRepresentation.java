package com.secsign.representation;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;

public class QrRepresentation {

  private @Valid String id = null;
  private @Valid String content = null;
  private @Valid String state = null;
  private @Valid String realm = null;
  public QrRepresentation id(String id) {
    this.id = id;
    return this;
  }

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public QrRepresentation content(String content) {
    this.content = content;
    return this;
  }

  @JsonProperty("content")
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public QrRepresentation state(String state) {
    this.state = state;
    return this;
  }

  @JsonProperty("state")
  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }


  /** */
  public QrRepresentation realm(String realm) {
    this.realm = realm;
    return this;
  }

  @JsonProperty("realm")
  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QrRepresentation qr = (QrRepresentation) o;
    return Objects.equals(id, qr.id)
        && Objects.equals(content, qr.content)
        && Objects.equals(state, qr.state)
        && Objects.equals(realm, qr.realm);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, content, state, realm);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Organization {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(content)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(state)).append("\n");
    sb.append("    realm: ").append(toIndentedString(realm)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
