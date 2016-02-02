package net.codejava.fileupload.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


@Entity
@Table(name = "talent_award_credit_join")
public class TalentAwardCreditMapping implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	
	int credit_id;
	@Id
	int talent_id;
	@Id
	int award_id;
	

	
	public int getAward_id() {
		return award_id;
	}
	public void setAward_id(int award_id) {
		this.award_id = award_id;
	}
	public int getCredit_id() {
		return credit_id;
	}
	public void setCredit_id(int credit_id) {
		this.credit_id = credit_id;
	}
	public int getTalent_id() {
		return talent_id;
	}
	public void setTalent_id(int talent_id) {
		this.talent_id = talent_id;
	}
	
	

}
