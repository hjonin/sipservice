package fr.gouv.vitam.sipservice.service;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.File;

import fr.gouv.vitam.sipservice.dto.ArchiveUnitData;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.jobs.context.JobDashboardProgressBar;
import org.jobrunr.jobs.context.JobRunrDashboardLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.gouv.vitam.sipservice.SipConfiguration;
import fr.gouv.vitam.sipservice.dto.SipData;
import fr.gouv.vitam.tools.sedalib.inout.SIPBuilder;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

@Service
public class SipService {

    private static final Logger log = LoggerFactory.getLogger(SipService.class);

    private static final JobRunrDashboardLogger jobLogger = new JobRunrDashboardLogger(log);

    @Autowired
    private SipConfiguration sipConfiguration;

    /**
     * This method is based on the sample example provided in seda lib fr.gouv.vitam.tools.sedalibsamples.Sample1.
     * It's a very basic example to enrich.
     * 
     * @param sipData basic fields to create an archive
     * @param context jobruner job context
     * @return a generated SI archive file
     */
    @Job(name = "SIP archive creation", retries = 1)
    public File createSip(SipData sipData, JobContext context) {
        JobDashboardProgressBar progressBar = context.progressBar(5);
        String jobId = context.getJobId().toString();
        String shortId = substringBefore(jobId, "-");
        SEDALibProgressLogger progressLogger = new SEDALibProgressLogger(jobLogger, SEDALibProgressLogger.OBJECTS_GROUP);
        jobLogger.info("Starting archive creation");
        final String filename = sipConfiguration.getDestinationFolder().toPath()
                .resolve(shortId + ".zip").toFile().getAbsolutePath();
        try (SIPBuilder sb = new SIPBuilder(filename, progressLogger)) {
            sb.setComment(sipData.getComment());
            progressBar.increaseByOne();
            sb.setMessageIdentifier(sipData.getMessageIdentifier());
            progressBar.increaseByOne();
            sb.setAgencies(sipData.getArchivalAgency(), sipData.getTransferringAgency(), sipData.getOriginatingAgency(), sipData.getSubmissionAgency());
            progressBar.increaseByOne();
            sb.setArchivalAgreement(sipData.getArchivalAgreement());
            progressBar.increaseByOne();
            for (ArchiveUnitData archiveUnitData: sipData.getArchiveUnitDataList()) {
                if (archiveUnitData.getPath() == null) {
                    sb.createRootArchiveUnit(archiveUnitData.getArchiveUnitID(), archiveUnitData.getDescriptionLevel(), archiveUnitData.getTitle(), archiveUnitData.getDescription());
                } else {
                    sb.addDiskSubTree(archiveUnitData.getArchiveUnitID(), archiveUnitData.getPath());
                }
                progressBar.increaseByOne();
            }
            sb.generateSIP();
            progressBar.increaseByOne();
        } catch (SEDALibException e) {
            log.error("Failed producing SIP archive {}", filename, e);
        }
        jobLogger.info("Ended archive creation: {}", filename);
        return new File(filename);
    }

}
