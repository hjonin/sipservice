package fr.gouv.vitam.sipservice.dto;

import java.util.List;

/**
 * Very few basic fields to create an archive.
 */
public class SipData {
    /* Manifest Header */
    private String comment;
    private String messageIdentifier;
    private String archivalAgreement;

    /* Manifest Footer */
    private String archivalAgency;
    private String transferringAgency;
    private String originatingAgency;
    private String submissionAgency;

    /* Archive Units */
    private List<ArchiveUnitData> archiveUnitDataList;

    public String getComment() {
        return comment;
    }

    public String getMessageIdentifier() {
        return messageIdentifier;
    }

    public String getArchivalAgreement() {
        return archivalAgreement;
    }

    public String getArchivalAgency() {
        return archivalAgency;
    }

    public String getTransferringAgency() {
        return transferringAgency;
    }

    public String getOriginatingAgency() {
        return originatingAgency;
    }

    public String getSubmissionAgency() {
        return submissionAgency;
    }

    public List<ArchiveUnitData> getArchiveUnitDataList() {
        return archiveUnitDataList;
    }
}
