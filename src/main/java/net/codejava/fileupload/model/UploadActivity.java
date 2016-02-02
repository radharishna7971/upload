package net.codejava.fileupload.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uploadactivity")
public class UploadActivity {
	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;
	
	/**
	 `filename` varchar(255) NOT NULL,
  `creditrecordcount` int(11) DEFAULT NULL,
  `talentrecordcount` int(11) DEFAULT NULL,
  `rolerecordcount` int(11) DEFAULT NULL,
  `genrerecordcount` int(11) DEFAULT NULL,
  `uploadedby` varchar(255) DEFAULT NULL,
  `filetypeselected` varchar(255) DEFAULT NULL,
  `failurecount` int(11) DEFAULT NULL
	 */
   String filename;
   int creditrecordcount;
   int talentrecordcount;
   int rolerecordcount;
   int genrerecordcount;
   String uploadedby;
   String filetypeselected;
   String filestatus;
   String uploaddate;
   long timeinminutes;
   int totalcount;
  
   public int getTotalcount() {
	return totalcount;
}

public void setTotalcount(int totalcount) {
	this.totalcount = totalcount;
}



public long getTimeinminutes() {
	return timeinminutes;
}

public void setTimeinminutes(long timeinminutes) {
	this.timeinminutes = timeinminutes;
}

public String getUploaddate() {
	return uploaddate;
}

public void setUploaddate(String uploaddate) {
	this.uploaddate = uploaddate;
}

public String getFilestatus() {
	return filestatus;
}

public void setFilestatus(String filestatus) {
	this.filestatus = filestatus;
}

public String getFilename() {
	return filename;
}

public void setFilename(String filename) {
	this.filename = filename;
}

public int getCreditrecordcount() {
	return creditrecordcount;
}

public void setCreditrecordcount(int creditrecordcount) {
	this.creditrecordcount = creditrecordcount;
}

public int getTalentrecordcount() {
	return talentrecordcount;
}

public void setTalentrecordcount(int talentrecordcount) {
	this.talentrecordcount = talentrecordcount;
}

public int getRolerecordcount() {
	return rolerecordcount;
}

public void setRolerecordcount(int rolerecordcount) {
	this.rolerecordcount = rolerecordcount;
}

public int getGenrerecordcount() {
	return genrerecordcount;
}

public void setGenrerecordcount(int genrerecordcount) {
	this.genrerecordcount = genrerecordcount;
}

public String getUploadedby() {
	return uploadedby;
}

public void setUploadedby(String uploadedby) {
	this.uploadedby = uploadedby;
}

public String getFiletypeselected() {
	return filetypeselected;
}

public void setFiletypeselected(String filetypeselected) {
	this.filetypeselected = filetypeselected;
}

public int getFailurecount() {
	return failurecount;
}

public void setFailurecount(int failurecount) {
	this.failurecount = failurecount;
}

int failurecount;
   
	
	public int  getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


}
