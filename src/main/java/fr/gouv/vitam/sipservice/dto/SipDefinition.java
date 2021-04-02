package fr.gouv.vitam.sipservice.dto;

import java.util.List;

/**
 * Very few basic fields to create an archive.
 */
public class SipDefinition {

    private List<String> agencies;
    private String archivalAgreement;

    private String archiveUnitID;
    private String descriptionLevel;
    private String title;
    private String description;

    private String folderPath;

    public List<String> getAgencies() {
        return agencies;
    }

    public void setAgencies(List<String> agencies) {
        this.agencies = agencies;
    }

    public String getArchivalAgreement() {
        return archivalAgreement;
    }

    public void setArchivalAgreement(String archivalAgreement) {
        this.archivalAgreement = archivalAgreement;
    }

    public String getArchiveUnitID() {
        return archiveUnitID;
    }

    public void setArchiveUnitID(String archiveUnitID) {
        this.archiveUnitID = archiveUnitID;
    }

    public String getDescriptionLevel() {
        return descriptionLevel;
    }

    public void setDescriptionLevel(String descriptionLevel) {
        this.descriptionLevel = descriptionLevel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String floderPath) {
        this.folderPath = floderPath;
    }

}
