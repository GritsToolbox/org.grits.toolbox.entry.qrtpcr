package org.grits.toolbox.entry.qrtpcr.model;

import javax.xml.bind.annotation.XmlAttribute;

public class Well {

	Character x;
	Integer y;
	
	public Well() {
	}
	
	public Well(char row, int column) {
		x = row;
		y = column;
	}
	
	@XmlAttribute 
	public Character getX() {
		return x;
	}
	public void setX(Character x) {
		this.x = x;
	}
	
	@XmlAttribute 
	public Integer getY() {
		return y;
	}
	public void setY(Integer y) {
		this.y = y;
	}
	
	@Override
	public String toString() {
		return Character.toString(x) + y;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Well)
			return ((Well) obj).getX().equals(this.getX()) && ((Well)obj).getY().equals(this.getY());
		return false;
	}
	
	@Override
	public int hashCode() {
		return x.hashCode() * 100 + y.hashCode();
	}
	
}
