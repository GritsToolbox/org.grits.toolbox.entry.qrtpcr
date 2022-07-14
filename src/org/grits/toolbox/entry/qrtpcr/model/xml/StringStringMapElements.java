package org.grits.toolbox.entry.qrtpcr.model.xml;

import javax.xml.bind.annotation.XmlElement;

public class StringStringMapElements {
    @XmlElement
    public String key;
    @XmlElement
    public String value;

    @SuppressWarnings("unused")
    private StringStringMapElements() {
    } //Required by JAXB

    public StringStringMapElements(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
