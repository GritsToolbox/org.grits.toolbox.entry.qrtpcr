package org.grits.toolbox.entry.qrtpcr.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="size")
public class Size {
	Integer width;
	Integer height;
	
	public Size() {
	}
	
	public Size(int x, int y) {
		width = x;
		height = y;
	}

	@XmlAttribute
	public Integer getWidth() {
		return width;
	}
	
	public void setWidth(Integer width) {
		this.width = width;
	}
	
	@XmlAttribute
	public Integer getHeight() {
		return height;
	}
	
	public void setHeight(Integer height) {
		this.height = height;
	}

}
