package ru.at_consulting.itsm.event_rules;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by vgoryachev on 01.04.2016.
 * Package: ru.at_consulting.itsm.
 */
@XmlRootElement(name = "configuration")
public class EventRules {

    private int version;
    private List<EnrichRule> enrichRules;

    public EventRules() {

    }

    public EventRules(List<EnrichRule> enrichRules) {
        super();
        this.enrichRules = enrichRules;
    }

    @XmlElement
    public List<EnrichRule> getEnrichRules() {
        return enrichRules;
    }

    public void setEnrichRules(List<EnrichRule> enrichRules) {
        this.enrichRules = enrichRules;
    }

    @XmlAttribute
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
