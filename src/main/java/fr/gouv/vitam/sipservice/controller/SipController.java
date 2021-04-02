package fr.gouv.vitam.sipservice.controller;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobId;
import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import fr.gouv.vitam.sipservice.SipConfiguration;
import fr.gouv.vitam.sipservice.dto.SipDefinition;
import fr.gouv.vitam.sipservice.service.SipService;

@RestController
@RequestMapping("/sip")
public class SipController {

    private static final Logger log = LoggerFactory.getLogger(SipController.class);

    @Autowired
    private SipConfiguration sipConfiguration;

    @Autowired
    private JobScheduler jobScheduler;

    @Autowired
    private StorageProvider storageProvider;

    @Autowired
    private SipService sipService;

    /**
     * Launches a job in background and wait for job to end.
     * Processing is Sync.
     * 
     * @param sip
     * @param response
     * @return SIP Zip archive file
     * @throws IOException
     */
    @PostMapping("/launchSync")
    public @ResponseBody byte[] launchJobSync(@RequestBody SipDefinition sip, HttpServletResponse response) throws IOException {
        UUID jobId = jobScheduler.enqueue(() -> sipService.createSip(sip, JobContext.Null)).asUUID();
        Job jobById = storageProvider.getJobById(jobId);
        StateName jobState = jobById.getState();
        while (!StateName.SUCCEEDED.equals(jobState)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Thread sleep failed", e);
            }
            jobState = storageProvider.getJobById(jobId).getJobState().getName();
        }
        return getArchiveFileBytes(response, jobId.toString());
    }

    /**
     * Launches a job in background and returns job id.
     * Processing is Async.
     * 
     * @param sip SIP archive metadata
     * @return a job id (to pool request for job status)
     * @throws IOException
     */
    @PostMapping("/launchAsync")
    public String launchJobAsync(@RequestBody SipDefinition sip) throws IOException {
        JobId jobId = jobScheduler.enqueue(() -> sipService.createSip(sip, JobContext.Null));
        return jobId.asUUID().toString();
    }

    /**
     * Polling request to get job status.
     * 
     * @param jobId 
     * @return status of the job
     * @throws IOException
     */
    @GetMapping("/status/{jobId}")
    public String pollJob(@PathVariable String jobId) throws IOException {
        Job jobById = storageProvider.getJobById(UUID.fromString(jobId));
        return jobById.getState().name();
    }

    /**
     * To call once polling gives SUCCEEDED status.
     * 
     * @param jobId
     * @param response
     * @return SIP Zip archive file
     * @throws IOException
     */
    @GetMapping("/download/{jobId}")
    public @ResponseBody byte[] downloadArchive(@PathVariable String jobId, HttpServletResponse response) throws IOException {
        return getArchiveFileBytes(response, jobId);
    }

    private byte[] getArchiveFileBytes(HttpServletResponse response, String jobId) throws IOException {
        final String shortId = substringBefore(jobId.toString(), "-");
        File sipFile = sipConfiguration.getDestinationFolder().toPath().resolve(shortId + ".zip").toFile();
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, attachmentHeaderValue(sipFile));
        return Files.readAllBytes(sipFile.toPath());
    }

    private String attachmentHeaderValue(File file) {
        return "attachment; filename=\"" + file.getName() + "\"";
    }

}
