package net.codejava.fileupload.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "associate_talent_associate_type_join")
public class AssociateTalentAssociateTypeMapping implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4668395719996689606L;
	@Id
  int associate_id;
	@Id
  int talent_id;
	public int getAssocite_types_id() {
		return associte_types_id;
	}
	public void setAssocite_types_id(int associte_types_id) {
		this.associte_types_id = associte_types_id;
	}
	@Id
  int associte_types_id;
public int getAssociate_id() {
	return associate_id;
}
public void setAssociate_id(int associate_id) {
	this.associate_id = associate_id;
}
public int getTalent_id() {
	return talent_id;
}
public void setTalent_id(int talent_id) {
	this.talent_id = talent_id;
}


  
}
