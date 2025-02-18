package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * In cases where the specifically defined criteria (IBAN, BBAN, MSISDN) are not provided to identify an instance of the respective account type (e.g. a savings account), the ASPSP shall include a proprietary ID of the respective account that uniquely identifies the account for this ASPSP.
 */
@ApiModel(description = "In cases where the specifically defined criteria (IBAN, BBAN, MSISDN) are not provided to identify an instance of the respective account type (e.g. a savings account), the ASPSP shall include a proprietary ID of the respective account that uniquely identifies the account for this ASPSP.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class OtherType   {
  @JsonProperty("identification")
  private String identification = null;

  @JsonProperty("schemeNameCode")
  private String schemeNameCode = null;

  @JsonProperty("schemeNameProprietary")
  private String schemeNameProprietary = null;

  @JsonProperty("issuer")
  private String issuer = null;

  public OtherType identification(String identification) {
    this.identification = identification;
    return this;
  }

  /**
   * Proprietary identification of the account.
   * @return identification
  **/
  @ApiModelProperty(required = true, value = "Proprietary identification of the account.")
  @NotNull

@Size(max=35)

  @JsonProperty("identification")
  public String getIdentification() {
    return identification;
  }

  public void setIdentification(String identification) {
    this.identification = identification;
  }

  public OtherType schemeNameCode(String schemeNameCode) {
    this.schemeNameCode = schemeNameCode;
    return this;
  }

  /**
   * An entry provided by an external ISO code list.
   * @return schemeNameCode
  **/
  @ApiModelProperty(value = "An entry provided by an external ISO code list.")

@Size(max=35)

  @JsonProperty("schemeNameCode")
  public String getSchemeNameCode() {
    return schemeNameCode;
  }

  public void setSchemeNameCode(String schemeNameCode) {
    this.schemeNameCode = schemeNameCode;
  }

  public OtherType schemeNameProprietary(String schemeNameProprietary) {
    this.schemeNameProprietary = schemeNameProprietary;
    return this;
  }

  /**
   * A scheme name defined in a proprietary way.
   * @return schemeNameProprietary
  **/
  @ApiModelProperty(value = "A scheme name defined in a proprietary way.")

@Size(max=35)

  @JsonProperty("schemeNameProprietary")
  public String getSchemeNameProprietary() {
    return schemeNameProprietary;
  }

  public void setSchemeNameProprietary(String schemeNameProprietary) {
    this.schemeNameProprietary = schemeNameProprietary;
  }

  public OtherType issuer(String issuer) {
    this.issuer = issuer;
    return this;
  }

  /**
   * Issuer of the identification.
   * @return issuer
  **/
  @ApiModelProperty(value = "Issuer of the identification.")

@Size(max=35)

  @JsonProperty("issuer")
  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    OtherType otherType = (OtherType) o;
    return Objects.equals(this.identification, otherType.identification) &&
    Objects.equals(this.schemeNameCode, otherType.schemeNameCode) &&
    Objects.equals(this.schemeNameProprietary, otherType.schemeNameProprietary) &&
    Objects.equals(this.issuer, otherType.issuer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identification, schemeNameCode, schemeNameProprietary, issuer);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OtherType {\n");

    sb.append("    identification: ").append(toIndentedString(identification)).append("\n");
    sb.append("    schemeNameCode: ").append(toIndentedString(schemeNameCode)).append("\n");
    sb.append("    schemeNameProprietary: ").append(toIndentedString(schemeNameProprietary)).append("\n");
    sb.append("    issuer: ").append(toIndentedString(issuer)).append("\n");
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

