package net.codejava.fileupload.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "keywords")
public class Keywords {

 @Id
 @GeneratedValue
 private int id;
 private String keyword;
public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public String getKeyword() {
	return keyword;
}
public void setKeyword(String keyword) {
	this.keyword = keyword;
}
 
}
