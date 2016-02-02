package net.codejava.fileupload.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "awards")
public class Awards {
	@Id
	 @GeneratedValue
	 int id;
	String awardname;
	String awardtype;
	public int getId() {
		return id;
	}
	public String getAwardtype() {
		return awardtype;
	}
	public void setAwardtype(String awardtype) {
		this.awardtype = awardtype;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAwardname() {
		return awardname;
	}
	public void setAwardname(String awardname) {
		this.awardname = awardname;
	}

}
