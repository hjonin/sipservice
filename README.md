# SIP archive generation service

Very basic draft of a service.
Receiving an archive definition, service produces the SIP archive file.

Just launch SipServiceApplication and abuse endpoints.

As process can be very long, it was funny to use jobrunr (default accessible at http://localhost:8000/). Thus, process is asynchronous, you just have to poll waiting for the job to end before downloading file.

A basic postman collection is available in helpers folder.

Also, it's a mess to produce needed jar so you can find dependencies attached. But following this procedure should be ok tweaking pom a bit... https://github.com/ProgrammeVitam/sedatools/blob/master/README.en.md

TODO list is huge if you want to go far
* Documentation. As it's currently very basic, I didn't go that far...
* Architecture and package are not very well separated
* SipDefinition is very basic based on seda lib sample 1. Going into very complicated stuff would probably need DTO objects, Business objects...
* Error management is not implemented at all
* Jobs are supposed to always end successfully :-D 
* Add security...
