package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * GrandTotalAmount
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-16T16:54:38.691901+02:00[Europe/Kiev]")

public class GrandTotalAmount   {
  @JsonProperty("description")
  private String description = null;

  @JsonProperty("amount")
  private Amount amount = null;

  public GrandTotalAmount description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Total amount of the instalment including charges, insurance and taxes in addition to the funded amount.
   * @return description
  **/
  @ApiModelProperty(required = true, value = "Total amount of the instalment including charges, insurance and taxes in addition to the funded amount. ")
  @NotNull



  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public GrandTotalAmount amount(Amount amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Get amount
   * @return amount
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("amount")
  public Amount getAmount() {
    return amount;
  }

  public void setAmount(Amount amount) {
    this.amount = amount;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    GrandTotalAmount grandTotalAmount = (GrandTotalAmount) o;
    return Objects.equals(this.description, grandTotalAmount.description) &&
    Objects.equals(this.amount, grandTotalAmount.amount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, amount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GrandTotalAmount {\n");

    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
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

