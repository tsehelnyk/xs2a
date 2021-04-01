package de.adorsys.psd2.core.payment.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class Xs2aPisPaymentInitiationJson {
    private String endToEndIdentification;
    private String instructionIdentification;
    private String debtorName;
    private Xs2aPisAccountReference debtorAccount;
    private String ultimateDebtor;
    private Xs2aAmount instructedAmount;
    private Xs2aPisAccountReference creditorAccount;
    private String creditorAgent;
    private String creditorAgentName;
    private String creditorName;
    private Xs2aPisAddress creditorAddress;
    private String creditorId;
    private String ultimateCreditor;
    private Xs2aPisPurposeCode purposeCode;
    private Xs2aPisChargeBearer chargeBearer;
    private String remittanceInformationUnstructured;
    private RemittanceInformationStructured remittanceInformationStructured;
    private List<RemittanceInformationStructured> remittanceInformationStructuredArray;
    private LocalDate requestedExecutionDate;
}

