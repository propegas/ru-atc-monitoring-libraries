package ru.at_consulting.itsm.event_rules;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by vgoryachev on 01.04.2016.
 * Package: ru.at_consulting.itsm.
 */
public class Statement {
    private int id;
    private String fieldName;
    private String fieldValue;

    public Statement() {
    }

    public Statement(int id, String fieldName, String fieldValue) {
        super();
        this.id = id;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlAttribute
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @XmlAttribute
    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    @Override
    public String toString() {
        return String.format("fieldName:%s\nfieldValue:%s", this.getFieldName(), this.getFieldValue());
    }
}
