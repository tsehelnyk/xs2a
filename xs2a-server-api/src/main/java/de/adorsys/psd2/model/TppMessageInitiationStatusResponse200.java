package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * TppMessageInitiationStatusResponse200
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class TppMessageInitiationStatusResponse200   {
  @JsonProperty("category")
  private TppMessageCategory category = null;

  @JsonProperty("code")
  private MessageCode200InitiationStatus code = null;

  @JsonProperty("path")
  private String path = null;

  @JsonProperty("text")
  private String text = null;

  public TppMessageInitiationStatusResponse200 category(TppMessageCategory category) {
    this.category = category;
    return this;
  }

  /**
   * Get category
   * @return category
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("category")
  public TppMessageCategory getCategory() {
    return category;
  }

  public void setCategory(TppMessageCategory category) {
    this.category = category;
  }

  public TppMessageInitiationStatusResponse200 code(MessageCode200InitiationStatus code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * @return code
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("code")
  public MessageCode200InitiationStatus getCode() {
    return code;
  }

  public void setCode(MessageCode200InitiationStatus code) {
    this.code = code;
  }

  public TppMessageInitiationStatusResponse200 path(String path) {
    this.path = path;
    return this;
  }

  /**
   * Get path
   * @return path
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("path")
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public TppMessageInitiationStatusResponse200 text(String text) {
    this.text = text;
    return this;
  }

  /**
   * Get text
   * @return text
  **/
  @ApiModelProperty(value = "")

@Size(max=500)

  @JsonProperty("text")
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    TppMessageInitiationStatusResponse200 tppMessageInitiationStatusResponse200 = (TppMessageInitiationStatusResponse200) o;
    return Objects.equals(this.category, tppMessageInitiationStatusResponse200.category) &&
    Objects.equals(this.code, tppMessageInitiationStatusResponse200.code) &&
    Objects.equals(this.path, tppMessageInitiationStatusResponse200.path) &&
    Objects.equals(this.text, tppMessageInitiationStatusResponse200.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, code, path, text);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TppMessageInitiationStatusResponse200 {\n");

    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    text: ").append(toIndentedString(text)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

