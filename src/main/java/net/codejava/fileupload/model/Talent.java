package net.codejava.fileupload.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Table(name = "talent")
public class Talent {
	 @Id
	 @GeneratedValue
	 int id;
	private String first_name;
	private String last_name;
	private int age;
	
	String gender;
	int ethnicity_id;
	
	public String getModifiedby() {
		return modifiedby;
	}
	public void setModifiedby(String modifiedby) {
		this.modifiedby = modifiedby;
	}
	public String getModifiedbycomments() {
		return modifiedbycomments;
	}
	public void setModifiedbycomments(String modifiedbycomments) {
		this.modifiedbycomments = modifiedbycomments;
	}
	public String getCreatedbycomments() {
		return createdbycomments;
	}
	public void setCreatedbycomments(String createdbycomments) {
		this.createdbycomments = createdbycomments;
	}
	public String getCreatedby() {
		return createdby;
	}
	public void setCreatedby(String createdby) {
		this.createdby = createdby;
	}
	String modifiedby;
	String modifiedbycomments;
	String createdbycomments;
	String createdby;
	
	public int getEthnicity_id() {
		return ethnicity_id;
	}
	public void setEthnicity_id(int ethnicity_id) {
		this.ethnicity_id = ethnicity_id;
	}
	public String getFirst_name() {
		return first_name;
	}
	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}
	public String getLast_name() {
		return last_name;
	}
	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}
	public String getTwitter_url() {
		return twitter_url;
	}
	public void setTwitter_url(String twitter_url) {
		this.twitter_url = twitter_url;
	}
	public String getInstagram_url() {
		return instagram_url;
	}
	public void setInstagram_url(String instagram_url) {
		this.instagram_url = instagram_url;
	}
	public String getVine_url() {
		return vine_url;
	}
	public void setVine_url(String vine_url) {
		this.vine_url = vine_url;
	}
	String twitter_url;
	String facebook_url;
	public String getFacebook_url() {
		return facebook_url;
	}
	public void setFacebook_url(String facebook_url) {
		this.facebook_url = facebook_url;
	}
	String instagram_url;
	String vine_url;
	String email;
	String phone;
	String record_id;
	String youtube_url;
	public String getYoutube_url() {
		return youtube_url;
	}
	public void setYoutube_url(String youtube_url) {
		this.youtube_url = youtube_url;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getRecord_id() {
		return record_id;
	}
	public void setRecord_id(String record_id) {
		this.record_id = record_id;
	}
	
	String partner;

	public String getPartner() {
		return partner;
	}
	public void setPartner(String partner) {
		this.partner = partner;
	}
	
	

}
