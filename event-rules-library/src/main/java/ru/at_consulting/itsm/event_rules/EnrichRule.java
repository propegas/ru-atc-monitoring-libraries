package ru.at_consulting.itsm.event_rules;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by vgoryachev on 01.04.2016.
 * Package: ru.at_consulting.itsm.
 */

public class EnrichRule {
    private int id;

    private RuleInput ruleInput;
    private RuleOutput ruleOutput;

    public EnrichRule() {

    }

    public EnrichRule(int id, RuleInput input, RuleOutput output) {
        super();
        this.id = id;
        this.ruleInput = input;
        this.ruleOutput = output;
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement
    public RuleInput getRuleInput() {
        return ruleInput;
    }

    public void setRuleInput(RuleInput ruleInput) {
        this.ruleInput = ruleInput;
    }

    @XmlElement
    public RuleOutput getRuleOutput() {
        return ruleOutput;
    }

    public void setRuleOutput(RuleOutput ruleOutput) {
        this.ruleOutput = ruleOutput;
    }

}
