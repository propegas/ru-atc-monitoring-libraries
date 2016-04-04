package ru.at_consulting.itsm.event_rules;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Created by vgoryachev on 01.04.2016.
 * Package: ru.at_consulting.itsm.
 */
public class RuleInput {

    private List<Statement> statements;

    public RuleInput() {
    }

    public RuleInput(List<Statement> statements) {
        super();
        this.statements = statements;
    }

    @XmlElement(name = "statement")
    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        return "Statements:" + this.getStatements();
    }
}
