package net.codejava.fileupload.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.JoinColumn;
import javax.persistence.TemporalType;
import javax.persistence.Column;
import javax.persistence.Temporal;



@Entity
@Table(name = "credits")
public class Credits {
 @Id
 @GeneratedValue
 int id;
 int record_id;
 //credit_id;genere_id;
 @ManyToMany(cascade = {CascadeType.ALL})
 @JoinTable(name="credits_genres_join", 
             joinColumns={@JoinColumn(name="credit_id")}, 
             inverseJoinColumns={@JoinColumn(name="genre_id")})
 Set<Genres> genres = new HashSet<Genres>();
 
 @ManyToMany(cascade = {CascadeType.ALL})
 @JoinTable(name="credits_keywords_join", 
             joinColumns={@JoinColumn(name="credit_id")}, 
             inverseJoinColumns={@JoinColumn(name="keyword_id")})
 Set<Keywords> keywords = new HashSet<Keywords>();
 
 
 public Set<Keywords> getKeywords() {
	return keywords;
}
public void setKeywords(Set<Keywords> keywords) {
	this.keywords = keywords;
}
/**
public Set<CreditTalentRoleMapping> getCreditTalentRoleMapping() {
	return creditTalentRoleMapping;
}
public void setCreditTalentRoleMapping(
		Set<CreditTalentRoleMapping> creditTalentRoleMapping) {
	this.creditTalentRoleMapping = creditTalentRoleMapping;
}
@ManyToMany(cascade = {CascadeType.ALL})
 @JoinTable(name="credits_talent_role_join", 
             joinColumns={@JoinColumn(name="id")}, 
             inverseJoinColumns={@JoinColumn(name="talent_id"), @JoinColumn(name="role_id")})
 Set<CreditTalentRoleMapping> creditTalentRoleMapping = new HashSet<CreditTalentRoleMapping>();
 **/
 public Set<Genres> getGenres() {
	return genres;
}
public void setGenres(Set<Genres> genres) {
	this.genres = genres;
}
public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public int getRecord_id() {
	return record_id;
}
public void setRecord_id(int record_id) {
	this.record_id = record_id;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public Date getRelease_date() {
	return release_date;
}
public void setRelease_date(Date release_date) {
	this.release_date = release_date;
}
public String getLogLine() {
	return logLine;
}
public void setLogLine(String logLine) {
	this.logLine = logLine;
}
public int getEstimatedBudget() {
	return estimatedBudget;
}
public void setEstimatedBudget(int estimatedBudget) {
	this.estimatedBudget = estimatedBudget;
}
public int getBox_office_income() {
	return box_office_income;
}
public void setBox_office_income(int box_office_income) {
	this.box_office_income = box_office_income;
}
String name;
 Date release_date;
 String logLine;
 int estimatedBudget;
 int box_office_income;
 String createdbycomments;
 String modifiedbycomments;
 String modifiedby;
 String createdby;


public String getCreatedbycomments() {
	return createdbycomments;
}
public void setCreatedbycomments(String createdbycomments) {
	this.createdbycomments = createdbycomments;
}
public String getModifiedbycomments() {
	return modifiedbycomments;
}
public void setModifiedbycomments(String modifiedbycomments) {
	this.modifiedbycomments = modifiedbycomments;
}
public String getModifiedby() {
	return modifiedby;
}
public void setModifiedby(String modifiedby) {
	this.modifiedby = modifiedby;
}
public String getCreatedby() {
	return createdby;
}
public void setCreatedby(String createdby) {
	this.createdby = createdby;
}
 
}
