package fr.gouv.vitam.sipservice.controller;

import fr.gouv.vitam.sipservice.SipConfiguration;
import fr.gouv.vitam.sipservice.dto.SipData;
import fr.gouv.vitam.sipservice.service.SipService;
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

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.substringBefore;

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
     * @param sipData
     * @param response
     * @return SIP Zip archive file
     * @throws IOException
     */
    @PostMapping("/generate-sync")
    public @ResponseBody
    byte[] launchJobSync(@RequestBody SipData sipData, HttpServletResponse response) throws IOException {
        UUID jobId = jobScheduler.enqueue(() -> sipService.createSip(sipData, JobContext.Null)).asUUID();
        try {
            do {
                Thread.sleep(1000);
            } while (jobIsNotDone(jobId));
        } catch (InterruptedException e) {
            log.error("Error while executing sample job", e);
            Thread.currentThread().interrupt();
        }
        if (jobHasFailed(jobId)) {
            return sendError(response);
        }
        return getArchiveFileBytes(response, jobId.toString());
    }

    private boolean jobIsNotDone(UUID jobId) {
        Job job = storageProvider.getJobById(jobId);
        return job.hasState(StateName.ENQUEUED) || job.hasState(StateName.PROCESSING);
    }

    private boolean jobHasFailed(UUID jobId) {
        Job job = storageProvider.getJobById(jobId);
        return job.hasState(StateName.FAILED);
    }

    /**
     * Launches a job in background and returns job id.
     * Processing is Async.
     *
     * @param sipData SIP archive metadata
     * @return a job id (to pool request for job status)
     * @throws IOException
     */
    @PostMapping("/generate-async")
    public String launchJobAsync(@RequestBody SipData sipData) throws IOException {
        JobId jobId = jobScheduler.enqueue(() -> sipService.createSip(sipData, JobContext.Null));
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
    public @ResponseBody
    byte[] downloadArchive(@PathVariable String jobId, HttpServletResponse response) throws IOException {
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

    private byte[] sendError(HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new byte[0];
    }
}
