package com.gien.gits.worker.events;

/**
 * @deprecated Use {@link com.gien.gits.ontology.event.CloudEvent} instead.
 * The canonical CloudEvent record has been relocated to the operational-ontology module
 * so that both the api and worker apps can share it without circular dependencies.
 */
@Deprecated(forRemoval = true)
public class CloudEvent {
    private final String specversion;
    private final String id;
    private final String source;
    private final String type;
    private final String time;
    private final String subject;
    private final String datacontenttype;
    private final java.util.Map<String, Object> data;

    private CloudEvent(String specversion, String id, String source, String type, String time,
                       String subject, String datacontenttype, java.util.Map<String, Object> data) {
        this.specversion = specversion;
        this.id = id;
        this.source = source;
        this.type = type;
        this.time = time;
        this.subject = subject;
        this.datacontenttype = datacontenttype;
        this.data = data;
    }

    public String specversion() { return specversion; }
    public String id() { return id; }
    public String source() { return source; }
    public String type() { return type; }
    public String time() { return time; }
    public String subject() { return subject; }
    public String datacontenttype() { return datacontenttype; }
    public java.util.Map<String, Object> data() { return data; }
}
