package net.codejava.fileupload.dao;

//DRMExcelSheetReader

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.codejava.fileupload.model.Associate;
import net.codejava.fileupload.model.AssociateTalentAssociateTypeMapping;
import net.codejava.fileupload.model.AssociateType;
import net.codejava.fileupload.model.Awards;
import net.codejava.fileupload.model.Company;
import net.codejava.fileupload.model.CreditTalentRoleMapping;
import net.codejava.fileupload.model.Credits;
import net.codejava.fileupload.model.Ethnicity;
import net.codejava.fileupload.model.Genres;
import net.codejava.fileupload.model.Keywords;
import net.codejava.fileupload.model.Role;
import net.codejava.fileupload.model.Talent;
import net.codejava.fileupload.model.TalentAwardCreditMapping;
import net.codejava.fileupload.model.UploadActivity;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * A program demonstrates reading other information of workbook, sheet and cell.
 * @author www.codejava.net
 *
 */
@Repository
public class DRMExcelSheetReader {

	@Autowired
	private SessionFactory sessionFactory;
	
	private static Map<String,String> genresHashMap = new HashMap<String,String>();
	private static Map<String,String> rolesHashMap = new HashMap<String,String>();
	private static Map<String,String> associateHashMap = new HashMap<String,String>();
	
	
	@SuppressWarnings("unchecked")
	@Transactional
	public List<UploadActivity> getActivity(String user)
	{
		Session session = null;
		List<UploadActivity> roles = new ArrayList<UploadActivity>();
		try
		{
			
			//sessionFactory = AppFactory.getSessionFactory();
			session = sessionFactory.getCurrentSession();
			
			roles	 = session.createQuery("from UploadActivity").list();
		return roles;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return roles;
	}
	//public static void main(String[] args) throws IOException {
	@SuppressWarnings("unchecked")
	@Transactional
	
	public  Workbook processfile(InputStream inputStream, String createdby, String comments, String filetype,String filename) throws IOException {
		//String excelFilePath = "Films  (Last 10 years)-5.xlsx";
		//FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
		//InputStream inputStream = excelFilePath.getInputStream();
		System.out.println("in process file");
		XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
		XSSFWorkbook errorbook = new XSSFWorkbook(); 
		//Create a blank spreadsheet
		XSSFSheet spreadsheet = errorbook.createSheet();
		System.out.println("work book created");
		Sheet sheet = workbook.getSheetAt(0);
		Row firstRow = sheet.getRow(0);
		copyRow(spreadsheet, firstRow, 0, "Remarks");
		
		Session session = null;
		try
		{
System.out.println("session factory before created");
			//sessionFactory = AppFactory.getSessionFactory();
			session = sessionFactory.getCurrentSession();
System.out.println("session factory after created");			
			List<Role> roles = session.createQuery("from Role").list();
			
			List<AssociateType> associateType = session.createQuery("from AssociateType").list();
			
			List<Genres> genres = session.createQuery("from Genres").list();
			for(Role rl : roles)
			{
				rolesHashMap.put(rl.getName(), rl.getId()+"");
			}
			for(AssociateType ast : associateType)
			{
				associateHashMap.put(ast.getType(), ast.getId()+"");
			}
			for(Genres gnr : genres)
			{
				genresHashMap.put(gnr.getName(), gnr.getId()+"");
			}
		}
		catch(Exception e)
		{
e.printStackTrace();
		}
		finally
		{
			/**if(session.isOpen())
			session.close();**/
		}
		
		/**Iterator<Cell> it = firstRow.cellIterator();
		boolean creditFile = false;
		boolean talentFile = false;
		while(it.hasNext())
		{
			Cell cell = it.next();
			String name = cell.getStringCellValue();
			if(name.contains("Genre"))
			{
				creditFile = true;
				break;
			}
			else if(name.contains("Age"))
			{
				talentFile = true;
			}
		}
		if(creditFile)
		{
			
			session = processCreditFile(sheet, session, createdby, comments);
		}
		else if(talentFile)
		{
			session = processTalentFile(sheet, session, createdby, comments);
		}
		//int numberOfSheets = workbook.getNumberOfSheets();
		**/
		String errorLogs = null;
		if(filetype.equalsIgnoreCase("Talent"))
		{
			errorLogs = processTalentFile(sheet, session, createdby, comments, filetype, filename, spreadsheet);
		}	
		else if(filetype.equalsIgnoreCase("Credits"))
		{
			errorLogs = processCreditFile(sheet, session, createdby, comments, filetype, filename, spreadsheet);
		}
		else if(filetype.equalsIgnoreCase("Stacy File"))
		{
			errorLogs = processTalentFileNew(sheet, session, createdby, comments, filetype, filename,spreadsheet);
		}
		else if(filetype.equalsIgnoreCase("DRM File"))
		{
			errorLogs = processDRMExportTalentFile(sheet, session, createdby, comments, filetype, filename,spreadsheet);
		}
		inputStream.close();
		return errorbook;
	}

	/**
	 * @param sheet
	 * @param session
	 * @return
	 */
	private String processCreditFile(Sheet sheet, Session session, String createdby, String comments, String filetype, String filename,XSSFSheet spreadsheet) {
		
		int creditcount = 0;
		int talentcount = 0;
		int rolerecordcount = 0;
		int genrerecordcount = 0;
		int uaId = 0;
		StringBuffer sb = new StringBuffer();
		long startTime = new Date().getTime();
		
		try
		{
			session = sessionFactory.getCurrentSession();
			//Transaction tx = session.beginTransaction();
			UploadActivity ua = new UploadActivity();
			ua.setCreditrecordcount(creditcount);
			ua.setFilename(filename);
			ua.setUploadedby(createdby);
			ua.setFiletypeselected(filetype);
			
			ua.setFilestatus("INPROGRESS");
			ua.setUploaddate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
			session.save(ua);
			session.flush();//tx.commit();
			uaId = ua.getId();
			
			//session.close();
			
			
			
			
		}
		catch(Exception e)
		{
			
		}
		
		int rowcount=0;
		String errorText = null;
		for(int start= 1; start < sheet.getLastRowNum();start++)
		{
			errorText = null;
			Row row = sheet.getRow(start);
			if(row == null || row.getCell(0) == null || row.getCell(1) == null)
				continue;
			String name= row.getCell(0).getStringCellValue().trim();
			int recordId = Integer.parseInt(row.getCell(1).getStringCellValue());
			String genre[] = row.getCell(2).getStringCellValue().split(",");
			//mm/dd/yyyy
			String releaseDate = row.getCell(3).getStringCellValue();
			String logLine = row.getCell(4).getStringCellValue();
			if(logLine.length() > 75)
			{
				logLine = logLine.substring(0, 74);
			}
			//pls remove , and $
			String estimatedBudget = row.getCell(5).getStringCellValue();
			estimatedBudget = estimatedBudget.replace("$", "").replace(",", "").trim();
			
			//pls remove , and $
			String usboxOfficeIncome = row.getCell(6).getStringCellValue();
			usboxOfficeIncome = usboxOfficeIncome.replace("$", "").replace(",", "").trim();
			
			String actors[] = row.getCell(7).getStringCellValue().trim().split(",");
			String directors[] = row.getCell(8).getStringCellValue().trim().split(",");
			
			String executiveProducer[] = row.getCell(9).getStringCellValue().trim().split(",");
			String producer[] = row.getCell(10).getStringCellValue().trim().split(",");
			String screenplay[] = row.getCell(11).getStringCellValue().trim().split(",");
			String writer[] = row.getCell(12).getStringCellValue().trim().split(",");
			String recentAwards[] = row.getCell(13).getStringCellValue().trim().split(",");
			//assuming keywords to be paragraph kind
			String keywords = row.getCell(14).getStringCellValue();
			if(keywords!= null && keywords.length()> 45)
			{
				keywords = keywords.substring(0, 44);
			}
			//Transaction tx = null;
			try
			{
				
				
				System.out.println("Inserting Record");
				session = sessionFactory.getCurrentSession();
				//tx = session.beginTransaction();
				Credits cr = null;
				
				
				Criteria creditcrt = session.createCriteria(Credits.class).add(Restrictions.eq("name", name));
				@SuppressWarnings("rawtypes")
				List crtList = creditcrt.list();
				if(crtList == null || crtList.size() > 0)
				{
					cr = ((Credits)(crtList).get(0));
					if(keywords != null && keywords.trim().length() > 0)
					{
					Keywords key = new Keywords();
					key.setKeyword(keywords);
					cr.getKeywords().add(key);
					}
					cr.setModifiedby(createdby);
					cr.setModifiedbycomments(comments);
					if(estimatedBudget != null)
					try
					{
					cr.setEstimatedBudget(Integer.parseInt(estimatedBudget));
					}
					catch(Exception e)
					{
						cr.setEstimatedBudget(0);
					}
					if(usboxOfficeIncome!= null)
					try
					{
					cr.setBox_office_income(Integer.parseInt(usboxOfficeIncome));
					}
					catch(Exception e)
					{
						cr.setBox_office_income(0);
					}
					cr.setLogLine(logLine);
					cr.setRecord_id(recordId);
					
					
					for(String genr: genre)
					{
						genr = genr.trim();
						Criteria crt = session.createCriteria(Genres.class).add(Restrictions.eq("name", genr));
						@SuppressWarnings("rawtypes")
						List genrList = crt.list();
						if(genrList!= null && genrList.size()>0)
						{
							
							boolean ignore = false;
							for(Genres g : cr.getGenres())
							{
								if(g.getName().equalsIgnoreCase(genr))
								{
									ignore = true;
								}
							}
							if(!ignore)
							{
								cr.getGenres().add((Genres)genrList.get(0));
							}
						}
						else
						{
							boolean ignore = false;
							for(Genres g : cr.getGenres())
							{
								if(g.getName().equalsIgnoreCase(genr))
								{
									ignore = true;
								}
							}
							if(ignore)
							{
							Genres gen = new Genres();
							gen.setName(genr);
							cr.getGenres().add(gen);
							}
							//session.save(gen);
						}
					}
					
					session.saveOrUpdate(cr);
				}
				else
				{
					cr = new Credits();
					Keywords key = new Keywords();
					key.setKeyword(keywords);
					cr.setName(name);
					cr.setCreatedby(createdby);
					cr.setCreatedbycomments(comments);
					try
					{
					cr.setEstimatedBudget(Integer.parseInt(estimatedBudget));
					}
					catch(Exception e)
					{
						cr.setEstimatedBudget(0);
					}
					try
					{
					cr.setBox_office_income(Integer.parseInt(usboxOfficeIncome));
					}
					catch(Exception e)
					{
						cr.setBox_office_income(0);
					}
					cr.setLogLine(logLine);
					cr.setRecord_id(recordId);
					cr.getKeywords().add(key);
					++creditcount;
					for(String genr: genre)
					{
						genr = genr.trim();
						Criteria crt = session.createCriteria(Genres.class).add(Restrictions.eq("name", genr));
						@SuppressWarnings("rawtypes")
						List genrList = crt.list();
						if(genrList!= null && genrList.size()>0)
						{
							cr.getGenres().add((Genres)genrList.get(0));
						}
						else
						{
							Genres gen = new Genres();
							gen.setName(genr);
							cr.getGenres().add(gen);
							//session.save(gen);
						}
					}
					session.save(cr);
					errorText = "Possible Error in Actor";
					talentcount = creditroletalentrelation(session, recordId, actors, cr, Integer.parseInt(rolesHashMap.get("Actor")), createdby, comments, talentcount);
					errorText = "Possible Error in Director";
					talentcount = creditroletalentrelation(session, recordId, directors, cr, Integer.parseInt(rolesHashMap.get("Director")), createdby, comments, talentcount);
					errorText = "Possible Error in Executive Producer";
					talentcount = creditroletalentrelation(session, recordId, executiveProducer, cr, Integer.parseInt(rolesHashMap.get("Executive Producer")), createdby, comments, talentcount);
					errorText = "Possible Error in Screenplay";
					talentcount = creditroletalentrelation(session, recordId, screenplay, cr, Integer.parseInt(rolesHashMap.get("Screenplay")), createdby, comments, talentcount);
					errorText = "Possible Error in Producer";
					talentcount = creditroletalentrelation(session, recordId, producer, cr, Integer.parseInt(rolesHashMap.get("Producer")), createdby, comments, talentcount);
					errorText = "Possible Error in Writer";
					talentcount = creditroletalentrelation(session, recordId, writer, cr, Integer.parseInt(rolesHashMap.get("Writer")), createdby, comments, talentcount);
					
				}
				
				//if(tx!= null)
				//{
					session.flush();
					//tx.commit();
				//}
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
				
				sb.append(row.getRowNum()+":"+row.getCell(0).getStringCellValue()+System.getProperty("line.separator"));
				copyRow(spreadsheet, row, ++rowcount, errorText);
				continue;
			}
			finally
			{
				//session.close();
				
			}
			
		}
		
		try
		{
			session = sessionFactory.getCurrentSession();
			//Transaction tx = session.beginTransaction();
			
			UploadActivity ua = (UploadActivity) session.load(UploadActivity.class, uaId);
			ua.setCreditrecordcount(creditcount);
			long currenttime = new Date().getTime();
			ua.setTalentrecordcount(talentcount);
			ua.setRolerecordcount(rolerecordcount);
			ua.setId(uaId);
			ua.setFilename(filename);
			ua.setFilestatus("SUCCESS");
			ua.setTimeinminutes((currenttime-startTime)/(1000*60));
			session.saveOrUpdate(ua);
			session.flush();//tx.commit();
			
			
			
			
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
		return sb.toString();
	}
	
	
	
	
	private static int creditroletalentrelation(Session session, int recordId,
			String[] directors, Credits cr, int roleId, String createdby, String comments, int talentcount) {
		if(directors == null || directors.length == 0)
			return talentcount;
		for(String actor : directors)
		{
			actor = actor.trim();
			String[] namesplit = actor.split(" ");
			String firstname = null;
			String lastname = null;
			if(namesplit.length > 0)
			{
				firstname = namesplit[0].trim();
				if(namesplit.length > 1)
					lastname = namesplit[1].trim();
				
			Criteria crt = session.createCriteria(Talent.class).add(Restrictions.eq("first_name", firstname));
			if(lastname!= null)
				crt.add(Restrictions.eq("last_name", lastname));
			@SuppressWarnings("rawtypes")
			List genrList = crt.list();
			if(genrList!= null && genrList.size()>0)
			{
				Talent  talent = (Talent)genrList.get(0);
				
				CreditTalentRoleMapping ctrm = new CreditTalentRoleMapping();
				ctrm.setTalent_id(talent.getId());
				ctrm.setRole_id(roleId);
				ctrm.setCredit_id(cr.getId());
				talent.setModifiedby(createdby);
				talent.setCreatedbycomments(comments);
				
				Criteria ctm = session.createCriteria(CreditTalentRoleMapping.class)
						.add(Restrictions.eq("talent_id", talent.getId()))
						.add(Restrictions.eq("role_id", roleId))
						.add(Restrictions.eq("credit_id", cr.getId()));
				List ctmlist = ctm.list();
				
				if(ctmlist == null || ctmlist.size() > 0)
				session.save(ctrm);
				
				session.update(talent);
			}
			else
			{
				Talent  talent = new Talent();
				talent.setFirst_name(firstname);
				talent.setLast_name(lastname);
				talent.setRecord_id(recordId+"");
				talent.setCreatedby(createdby);
				talent.setCreatedbycomments(comments);
				++talentcount;
				
				Criteria crt1 = session.createCriteria(Ethnicity.class).add(Restrictions.eq("name", ""));
			    List eth =  crt1.list();
			    if(eth!= null && eth.size()>0)
				{
			    	//talent.setEthnicity((Ethnicity)eth.get(0));
			    	talent.setEthnicity_id(((Ethnicity)eth.get(0)).getId());
				}
			    else
			    {
			    	Ethnicity ethn = new Ethnicity();
			    	ethn.setName("");
			    	session.save(ethn);
			    	//talent.setEthnicity(ethn);
			    	talent.setEthnicity_id(ethn.getId());
			    }
				
				
				session.save(talent);
				CreditTalentRoleMapping ctrm = new CreditTalentRoleMapping();
				ctrm.setTalent_id(talent.getId());
				ctrm.setRole_id(roleId);
				ctrm.setCredit_id(cr.getId());
				session.save(ctrm);
			}
			}
		}
		return talentcount;
	}

	/**
	 * @param sheet
	 * @param session
	 * @return
	 */
	public  String processTalentFile(Sheet sheet, Session session, String createdby, String comments, String filetype, String filename,XSSFSheet spreadsheet) {
		
		int creditcount = 0;
		int talentcount = 0;
		int rolerecordcount = 0;
		int genrerecordcount = 0;
		int totalrecord= 0;
		int namecount = 0;
		int uaId = 0;
		StringBuffer sb = new StringBuffer();
		long startTime = new Date().getTime();
		System.out.println("processTalentFile");
		try
		{
			session = sessionFactory.getCurrentSession();
			//Transaction tx = session.beginTransaction();
			UploadActivity ua = new UploadActivity();
			ua.setCreditrecordcount(creditcount);
			ua.setFilename(filename);
			ua.setUploadedby(createdby);
			ua.setFiletypeselected(filetype);
			
			ua.setFilestatus("INPROGRESS");
			ua.setUploaddate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
			session.save(ua);
			session.flush();//tx.commit();
			uaId = ua.getId();
			//if(session.isOpen())
			//session.close();
			
			
			
			
		}
		catch(Exception e)
		{
			//if(session.isOpen())
				//session.close();
		}
		int rowcount=0;
		String errortext = null;
		for(int start= 1; start < sheet.getLastRowNum();start++)
		{
			errortext = null;
			System.out.println("row no: "+1);
			Row row = sheet.getRow(start);
			try
			{
			if(row == null || row.getCell(0) == null || row.getCell(0).getStringCellValue().trim().length() ==0)
			{
				++namecount;
				if(namecount == 5)
				{
					break;
				}
				continue;
				
			}
			++totalrecord;
			String name= null;
			try
			{
				name = row.getCell(0).getStringCellValue().trim();
			}
			catch(Exception e)
			{
				
			}
			//int recordId = Integer.parseInt(row.getCell(1).getNumericValue());
			String role[] = null;
			try
			{
				role= row.getCell(2).getStringCellValue().split(",");
			}
			catch(Exception e)
			{
				
			}
			String gender = null;
			try
			{
				gender = row.getCell(5).getStringCellValue();
			}
			catch(Exception e)
			{
				
			}
			String attorneyAssoc = null;
			try{
				attorneyAssoc = row.getCell(6).getStringCellValue().trim();
			}
			catch(Exception e)
			{
				
			}
			//String ageString = row.getCell(8).getRichStringCellValue().getString();
			String socialHandlesString = null;
			try
			{
				socialHandlesString = row.getCell(9).getStringCellValue();
			}
			catch(Exception e)
			{
				
			}
			String associateerror = "";
			String agentString = null;
			try{
				agentString = row.getCell(10).getStringCellValue();
			}
			catch(Exception e)
			{
				
			}
			String mostrecentawards = null;
			try{
				mostrecentawards = row.getCell(11).getStringCellValue().trim();
			}
			catch(Exception e)
			{
				
			}
			String ethnicity = null;
			try{
				ethnicity = row.getCell(12).getStringCellValue().trim();
			}
			catch(Exception e)
			{
				
			}
			String managerassoc = null;
			try{
				managerassoc = row.getCell(3).getStringCellValue().trim();
			}
			catch(Exception e)
			{
				
			}
			String contactInfoEmail = null;
			
			try
			{   
				if(row.getCell(13) != null)
				contactInfoEmail= row.getCell(13).getStringCellValue();
			}
			catch(Exception e )
			{
				e.printStackTrace();
				try
				{
				contactInfoEmail= row.getCell(13).getNumericCellValue()+"";
				}
				catch(Exception ex)
				{
					
				}
			}
			if(name == null || name.trim().length() == 0)
				continue;
			System.out.println("name"+name);
			System.out.println("gender"+gender);
			System.out.println("socialHandlesString"+socialHandlesString);
			System.out.println("mostrecentawards"+mostrecentawards);
			System.out.println("ethnicity"+ethnicity);
			System.out.println("contactInfoEmail"+contactInfoEmail);
				
			String[] award_awardtype_credits = null;
			try{
				award_awardtype_credits = mostrecentawards.split(",");
			}
			catch(Exception e)
			{
				
			}
			
			String[] allsocialhandles = null;
			try{
				allsocialhandles = socialHandlesString.split(",");
			}
			catch(Exception e)
			{
				
			}
			String facebook = null;
			String twitter = null;
			if(allsocialhandles != null)
			for(String socialh: allsocialhandles)
			{
				System.out.println("Social :"+socialh);
				if(socialh.contains("Facebook"))
				{
					facebook = socialh.replace("Facebook","").replace("(","").replace(")","").trim();
				}
				else if(socialh.contains("Twitter"))
				{
					twitter = socialh.replace("Twitter","").replace("(","").replace(")","").trim();
				}
			}
			
			
			
			
			int age = 0;
			boolean ageIsAlpha = false;
			/**for(char c: ageString.toCharArray())
			{
				if(Character.isAlphabetic(c))
				{
					ageIsAlpha = true;
					break;
				}
			}
			if(!ageIsAlpha)
			{
				age = Integer.parseInt(ageString.trim());
			}**/
			
			
			//Transaction tx = null;
			try
			{
				session = sessionFactory.getCurrentSession();
				//tx = session.beginTransaction();
				//name = name.trim();
				Talent talent = null;
				String lastName = "";
			    String firstName= "";
			    if(name.split("\\w+").length>1){

			       lastName = name.substring(name.lastIndexOf(" ")+1);
			       firstName = name.substring(0, name.lastIndexOf(' '));
			    }
			     else{
			       firstName = name;
			    }
				Criteria crt = session.createCriteria(Talent.class).add(Restrictions.eq("first_name",firstName )).add(Restrictions.eq("last_name", lastName));
				@SuppressWarnings("rawtypes")
				List talentList = crt.list();
				if(talentList!= null && talentList.size()>0)
				{
					talent = (Talent)talentList.get(0);
					talent.setModifiedby(createdby);
					talent.setModifiedbycomments(comments);
					if(age > 0)
					{
						talent.setAge(age);
					}
					if(gender != null && (gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("Female")))
					{
						talent.setGender(gender);
					}
					if(ethnicity != null && !(ethnicity.isEmpty() || ethnicity.contains("No Race Available")))
					{
						if(ethnicity.contains("Hispanic"))
						{
							ethnicity = "Latino / Hispanic";
						}
						else if(ethnicity.contains("Southeast Asian/Indian"))
						{
							ethnicity = "Southeast Asian/Indian";
						}
						else if(ethnicity.contains("Indian"))
						{
							ethnicity = "Indian";
						}
						//
						Criteria crt1 = session.createCriteria(Ethnicity.class).add(Restrictions.eq("name", ethnicity.trim()));
					    List eth =  crt1.list();
					    if(eth!= null && eth.size()>0)
						{
					    	//talent.setEthnicity((Ethnicity)eth.get(0));
					    	talent.setEthnicity_id(((Ethnicity)eth.get(0)).getId());
						}
					    else
					    {
					    	Ethnicity ethn = new Ethnicity();
					    	ethn.setName(ethnicity);
					    	session.save(ethn);
					    	//talent.setEthnicity(ethn);
					    	talent.setEthnicity_id(ethn.getId());
					    }
					
					}
					if(facebook != null)
					{
						talent.setFacebook_url(facebook);
					}
					if(twitter != null)
					{
						talent.setTwitter_url(twitter);
					}
					if(contactInfoEmail!= null && contactInfoEmail.contains("@"))
					{
						talent.setEmail(contactInfoEmail);
					}
					errortext = "Possible error in Awards/Roles";
					List<Integer> roleCredit= addawards(award_awardtype_credits, talent, role, session,createdby,comments, rolerecordcount, creditcount);
					rolerecordcount = roleCredit.get(0);
					creditcount = roleCredit.get(1);
					session.saveOrUpdate(talent);
					
				}
				else
				{
					talent = new Talent();
					talent.setCreatedby(createdby);
					talent.setCreatedbycomments(comments);
					//session.save(gen);
					talent.setFirst_name(firstName);
					//if(name.split(" ").length > 1)
					talent.setLast_name(lastName);
					talent.setAge(age);
					if(gender!= null && (gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("Female")))
					{
						talent.setGender(gender);
					}
					
					
					Criteria crt1 = session.createCriteria(Ethnicity.class).add(Restrictions.eq("name", ethnicity));
				    List eth =  crt1.list();
				    if(eth!= null && eth.size()>0)
					{
				    	talent.setEthnicity_id(((Ethnicity)eth.get(0)).getId());
					}
				    else
				    {
				    	errortext = "Possible error in Ethnicity";
				    	Ethnicity ethn = new Ethnicity();
				    	ethn.setName(ethnicity);
				    	session.save(ethn);
				    	talent.setEthnicity_id(ethn.getId());
				    }
				    
				    if(facebook != null)
					{
						talent.setFacebook_url(facebook);
					}
					if(twitter != null)
					{
						talent.setTwitter_url(twitter);
					}
					if(contactInfoEmail != null && contactInfoEmail.contains("@"))
					{
						talent.setEmail(contactInfoEmail);
					}
					//session.saveOrUpdate(talent);
					errortext = "Possible error in Awards/Roles";
					List<Integer> roleCredit = addawards(award_awardtype_credits, talent, role, session, createdby, comments, rolerecordcount, creditcount);
					rolerecordcount = roleCredit.get(0);
					creditcount = roleCredit.get(1);
					session.saveOrUpdate(talent);
				}
				errortext = "Possible error in Agent or Ethnicity";
				if(agentString!= null && !agentString.contains("No Agent") && agentString.length() >0)
				{
					associateerror += addAssociate(session, agentString, talent, "Agent");
				}
				errortext = "Possible error in Attorney or Ethnicity";
				if(attorneyAssoc!= null && !attorneyAssoc.contains("No Attorney") && attorneyAssoc.length() >0)
				{
					associateerror += addAssociate(session, attorneyAssoc, talent, "Attorney");
				}
				errortext = "Possible error in Manager Or Ethnicity";
				if(managerassoc != null && !managerassoc.contains("No Manager") && managerassoc.length()  >0)
				{
					associateerror += addAssociate(session, managerassoc, talent, "Manager");
				}
				//if(tx != null)
				//{
					session.flush();
					//tx.commit();
					++talentcount;
				//}
				
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
				//errortext = "Possible error in Agent";
				session.clear();
				sb.append(row.getRowNum()+":"+row.getCell(0).getStringCellValue()+System.getProperty("line.separator"));
				//Row r= spreadsheet.createRow(++rowcount);
				copyRow(spreadsheet, row, ++rowcount, errortext);
				continue;
			}
			finally
			{
				//if(session.isOpen())
				//session.close();
				
			}
			}
			catch(Exception e)
			{
				try
				{
					sb.append(row.getRowNum()+":"+row.getCell(0).getStringCellValue()+System.getProperty("line.separator"));
					
				}
				catch(Exception e1)
				{
				//ignore	
				}
				continue;
			}
			finally
			{
				//if(session.isOpen())
				//session.close();
			}
		}
		try
		{
			session = sessionFactory.getCurrentSession();
			//Transaction tx = session.beginTransaction();
			UploadActivity ua = (UploadActivity) session.load(UploadActivity.class, uaId);
			
			ua.setCreditrecordcount(creditcount);
			long currenttime = new Date().getTime();
			ua.setTalentrecordcount(talentcount);
			ua.setRolerecordcount(rolerecordcount);
			ua.setFailurecount(totalrecord - talentcount);
			ua.setTotalcount(totalrecord);
			ua.setId(uaId);
			ua.setFilename(filename);
			ua.setFilestatus("SUCCESS");
			ua.setTimeinminutes((currenttime-startTime)/(1000*60));
			session.saveOrUpdate(ua);
			session.flush();//tx.commit();
			
			
			
			
			
			
		}
		catch(Exception e)
		{
			session.clear();
			//if(session.isOpen())
			//session.close();
		}
		finally
		{
			//if(session.isOpen())
			//session.close();
		}
		
		
		return sb.toString();
	}

	/**
	 * @param session
	 * @param name
	 * @param agentString
	 * @param talent
	 */
	private String addAssociate(Session session,String agentString,
			Talent talent, String key) {
		String possiblerror = "";
		String[] agents = agentString.split(",");
		int count = 0;
		String companyname = null;
		for(String ag : agents)
		{
			if(ag== null || ag.length() == 0)
				continue;
			if(count == 0)
			{
				count++;
				companyname = ag;
			System.out.println("Agent Company name: "+ companyname);
			continue;
			}
			String firstname = null;
			String lastname = null;
			String mob = null;
			int awardStartIndex =ag.indexOf("(");
			if(ag.contains(" ") && awardStartIndex > -1)
			{
			firstname = ag.substring(0, awardStartIndex -1).trim().split(" ")[0];
			lastname = ag.substring(0, awardStartIndex -1).trim().split(" ")[1];
			mob = ag.substring(awardStartIndex);
			System.out.println("First Name "+firstname);
			System.out.println("Last Name "+lastname);
			
			System.out.println("Mob "+mob);
			}
			else
			{
				firstname = ag.split(" ")[0].trim();
				if(ag.split(" ").length > 1)
				{
				lastname = ag.split(" ")[1].trim();
				}
				else
					lastname="";
			}
				
			Criteria crt1 = session.createCriteria(Associate.class).add(Restrictions.eq("firstName", firstname.trim())).add(Restrictions.eq("lastName", lastname.trim()));
			@SuppressWarnings("rawtypes")
			List assocList = crt1.list();
			if(assocList == null || assocList.size()==0)
			{
				Company comp = null;
				if(companyname != null)
				{
					Criteria crtcomp = session.createCriteria(Company.class).add(Restrictions.eq("name", companyname.trim()));
					@SuppressWarnings("rawtypes")
					List companyList = crtcomp.list();
					if(companyList == null || companyList.size() == 0)
					{
					comp = new Company();
					comp.setName(companyname);
					session.save(comp);
					session.flush();
					}
					else
					{
						comp = (Company)companyList.get(0);
					}
				}
				Associate assoc = new Associate();
				assoc.setFirstName(firstname);
				assoc.setLastName(lastname);
				assoc.setPhone(mob);
				if(comp != null && comp.getId() != 0)
				{
					assoc.setCompany_id(comp.getId()+"");
				}
				session.save(assoc);
				session.flush();
				AssociateTalentAssociateTypeMapping atam = new AssociateTalentAssociateTypeMapping();
				atam.setAssociate_id(assoc.getId());
				atam.setAssocite_types_id(Integer.parseInt(associateHashMap.get(key)));
				atam.setTalent_id(talent.getId());
				session.save(atam);
				session.flush();
			}
			else
			{
				//asscoiate does not chage for life time
				System.out.println("associates exists");
				Company comp = null;
				if(companyname != null)
				{
					Criteria crtcomp = session.createCriteria(Company.class).add(Restrictions.eq("name", companyname.trim()));
					@SuppressWarnings("rawtypes")
					List companyList = crtcomp.list();
					if(companyList == null || companyList.size() == 0)
					{
					comp = new Company();
					comp.setName(companyname);
					session.save(comp);
					session.flush();
					}
					else
					{
						comp = (Company)companyList.get(0);
					}
				}
				Associate assoc = (Associate)assocList.get(0);
				System.out.println("associatess name"+assoc.getFirstName());
				System.out.println("talent name"+talent.getFirst_name());
				if(mob != null)
				assoc.setPhone(mob);
				if(comp != null && comp.getId() != 0)
				{
					assoc.setCompany_id(comp.getId()+"");
				}
				System.out.println("bfore flusg"+key+associateHashMap.get(key));
				session.saveOrUpdate(assoc);
				session.flush();
				System.out.println("after flusg");
				Criteria crtast = session.createCriteria(AssociateTalentAssociateTypeMapping.class).add(Restrictions.eq("associate_id", assoc.getId()))
						.add(Restrictions.eq("associte_types_id", Integer.parseInt(associateHashMap.get(key))))
						.add(Restrictions.eq("talent_id", talent.getId()));
				@SuppressWarnings("rawtypes")
				List astmList = crtast.list();
				System.out.println("after atm relation");
				
				if(astmList== null || astmList.size() == 0)
				{
					System.out.println("join not here");
				AssociateTalentAssociateTypeMapping atam = new AssociateTalentAssociateTypeMapping();
				atam.setAssociate_id(assoc.getId());
				atam.setAssocite_types_id(Integer.parseInt(associateHashMap.get(key)));
				atam.setTalent_id(talent.getId());
				session.save(atam);
				session.flush();
				}
			}
			
			
						
		}
		return possiblerror;
	}
	
	
	/**
	 * @param session
	 * @param name
	 * @param agentString
	 * @param talent
	 */
	private void addNewAssociate(Session session,String name1,String mobile, String phoneNo,String company,
			Talent talent, String key) {
		if(name1.contains("/"))
		{
			company = name1.split("/")[0];
			name1= name1.split("/")[1];
			
		}
		String nameList[] = name1.split(",");
		
		for(String name : nameList)
		{
			
		

		String lastname = "";
	    String firstName= "";
	    if(name.split("\\w+").length>1){

	       lastname = name.substring(name.lastIndexOf(" ")+1);
	       firstName = name.substring(0, name.lastIndexOf(' '));
	    }
	     else{
	       firstName = name;
	    }
		int count = 0;
				if(firstName.trim().length() == 0)
				return;
			Criteria crt1 = session.createCriteria(Associate.class).add(Restrictions.eq("firstName", firstName.trim())).add(Restrictions.eq("lastName", lastname.trim()));
			@SuppressWarnings("rawtypes")
			List assocList = crt1.list();
			if(assocList == null || assocList.size()==0)
			{
				Company comp = null;
				if(company != null && company.length() > 0)
				{
					Criteria crtcomp = session.createCriteria(Company.class).add(Restrictions.eq("name", company.trim()));
					@SuppressWarnings("rawtypes")
					List companyList = crtcomp.list();
					if(companyList == null || companyList.size() == 0)
					{
					comp = new Company();
					comp.setName(company);
					session.save(comp);
					}
					else
					{
						comp = (Company)companyList.get(0);
					}
				}
				Associate assoc = new Associate();
				assoc.setFirstName(firstName);
				assoc.setLastName(lastname);
				assoc.setPhone(mobile);
				if(comp != null && comp.getId() != 0)
				{
					assoc.setCompany_id(comp.getId()+"");
				}
				session.save(assoc);
				AssociateTalentAssociateTypeMapping atam = new AssociateTalentAssociateTypeMapping();
				atam.setAssociate_id(assoc.getId());
				atam.setAssocite_types_id(Integer.parseInt(associateHashMap.get(key)));
				atam.setTalent_id(talent.getId());
				
				
				Criteria atamcrt = session.createCriteria(AssociateTalentAssociateTypeMapping.class).add(Restrictions.eq("associate_id", atam.getAssociate_id())).add(Restrictions.eq("associte_types_id", atam.getAssocite_types_id()))
						.add(Restrictions.eq("talent_id", atam.getTalent_id()));
				List atamExistList = atamcrt.list();
				if(atamExistList.size() == 0)
				session.save(atam);
				
			}
			else
			{
				AssociateTalentAssociateTypeMapping atam = new AssociateTalentAssociateTypeMapping();
				atam.setAssociate_id(((Associate) assocList.get(0)).getId());
				atam.setAssocite_types_id(Integer.parseInt(associateHashMap.get(key)));
				atam.setTalent_id(talent.getId());
				Criteria atamcrt = session.createCriteria(AssociateTalentAssociateTypeMapping.class).add(Restrictions.eq("associate_id", atam.getAssociate_id())).add(Restrictions.eq("associte_types_id", atam.getAssocite_types_id()))
						.add(Restrictions.eq("talent_id", atam.getTalent_id()));
				List atamExistList = atamcrt.list();
				if(atamExistList.size() == 0)
				session.save(atam);
			}
		}
	}
			
			
			
		
	

	/**
	 * @param award_awardtype_credits
	 */
	private static List<Integer> addawards(String[] award_awardtype_credits,Talent talent, String roles[], Session session, String createdby, String comments, int rolecount, int creditcount) {
		List<Awards> awardsList = new ArrayList<Awards>();
		List<String> creditList = new ArrayList<String>();
		List<Integer> rolecreditcounts = new ArrayList<Integer>();
		try
		{
		if(award_awardtype_credits != null)
		for(String award_awardtype_credit:  award_awardtype_credits)
		{
			if(award_awardtype_credit.contains("(") && award_awardtype_credit.contains(")"))
			{
			Awards aw = new Awards();
			int awardStartIndex =award_awardtype_credit.indexOf("(");
			int awardLastIndex =award_awardtype_credit.indexOf(")");
			aw.setAwardtype(award_awardtype_credit.substring(awardStartIndex+1, awardLastIndex));
		    aw.setAwardname(award_awardtype_credit.substring(0, awardStartIndex).trim());
		    if(award_awardtype_credit.substring(awardLastIndex).length() > 6)
		    creditList.add(award_awardtype_credit.substring(awardLastIndex+5).trim());
		    awardsList.add(aw);
			}
		}
		if(roles != null)
		for(String role : roles)
		{
			role = role.trim();
			if(rolesHashMap.get(role)== null)
			{
				Role rol = new Role();
				rol.setName(role);
				
				Criteria rolecrt = session.createCriteria(Role.class).add(Restrictions.eq("name", role));
				List exist = rolecrt.list();
				if(exist == null || exist.size() ==0)
				{
				session.save(rol);
				rolecount++;
				rolesHashMap.put( role, rol.getId()+"");
				}
				
			}
		}
		for(String cr :creditList)
		{
			for(String role : roles)
			{
			CreditTalentRoleMapping ctr = new CreditTalentRoleMapping();
			ctr.setRole_id(Integer.parseInt(rolesHashMap.get(role.trim())));
			ctr.setTalent_id(talent.getId());
			Criteria crt = session.createCriteria(Credits.class).add(Restrictions.eq("name", cr.trim()));
			List cred = crt.list();
			if(cred.size() > 0)
			{
				ctr.setCredit_id(((Credits)cred.get(0)).getId());
			}
			else
			{
				Credits credits = new Credits();
				credits.setName(cr);
				credits.setCreatedby(createdby);
				credits.setCreatedbycomments(comments);
				session.save(credits);
				creditcount++;
				ctr.setCredit_id(credits.getId());
			}
			Criteria crttalentcreditrole = session.createCriteria(CreditTalentRoleMapping.class).add(Restrictions.eq("credit_id", ctr.getCredit_id())).add(Restrictions.eq("talent_id", ctr.getTalent_id())).add(Restrictions.eq("role_id", ctr.getRole_id()));
			List exist = crttalentcreditrole.list();
			if(exist == null || exist.size() ==0)
			session.save(ctr);
			
			}
			
			if(awardsList.size() > 0)
			{
				
				for(Awards award : awardsList)
				{
					session.save(award);
					TalentAwardCreditMapping ctr = new TalentAwardCreditMapping();
					
					ctr.setTalent_id(talent.getId());
					ctr.setAward_id(award.getId());
					Criteria crt = session.createCriteria(Credits.class).add(Restrictions.eq("name", cr.trim()));
					List cred = crt.list();
					if(cred.size() > 0)
					{
						ctr.setCredit_id(((Credits)cred.get(0)).getId());
					}
					else
					{
						Credits credits = new Credits();
						credits.setName(cr);
						credits.setCreatedby(createdby);
						credits.setCreatedbycomments(comments);
						session.save(credits);
						creditcount++;
						ctr.setCredit_id(credits.getId());
					}
					Criteria crttalentcreditrole = session.createCriteria(TalentAwardCreditMapping.class).add(Restrictions.eq("credit_id", ctr.getCredit_id())).add(Restrictions.eq("talent_id", ctr.getTalent_id())).add(Restrictions.eq("award_id", ctr.getAward_id()));
					List exist = crttalentcreditrole.list();
					if(exist == null || exist.size() ==0)
					session.save(ctr);
					
					}
				}
			}
		
		rolecreditcounts.add(rolecount);
		rolecreditcounts.add(creditcount);
		}
		catch(Exception e)
		{
			session.clear();
		}
		return rolecreditcounts;
		}
	
	
	
	
	/**
	 * @param sheet
	 * @param session
	 * @return
	 */
	public  String processTalentFileNew(Sheet sheet, Session session, String createdby, String comments , String filetype, String filename, XSSFSheet spreadsheet) {
		
		int creditcount = 0;
		int talentcount = 0;
		int rolerecordcount = 0;
		int genrerecordcount = 0;
		int uaId = 0;
		StringBuffer sb = new StringBuffer();
		long startTime = new Date().getTime();
		
		try
		{
			session = sessionFactory.getCurrentSession();
			//Transaction tx = session.beginTransaction();
			UploadActivity ua = new UploadActivity();
			ua.setCreditrecordcount(creditcount);
			ua.setFilename(filename);
			ua.setUploadedby(createdby);
			ua.setFiletypeselected(filetype);
			
			ua.setFilestatus("INPROGRESS");
			ua.setUploaddate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
			session.save(ua);
			session.flush();//tx.commit();
			uaId = ua.getId();
			//if(session.isOpen())
			//session.close();
			
			
			
			
		}
		catch(Exception e)
		{
			session.clear();
			e.printStackTrace();
			//if(session.isOpen())
			//session.close();
		}
		
		
		int rowcount =0;
		String errorText = null;
		for(int start= 1; start < sheet.getLastRowNum();start++)
		{
			errorText = null;
			Row row = sheet.getRow(start);
			if(row == null || row.getCell(0) == null || row.getCell(1) == null)
				continue;
			String firstName= row.getCell(0).getStringCellValue().trim();
			String lastName= row.getCell(1).getStringCellValue().trim();
			if(firstName.length() == 0 && lastName.length() == 0)
				continue;
			
			String writtingDirectingpartner = row.getCell(2).getStringCellValue().trim();
			String credits = row.getCell(3).getStringCellValue().trim();
			String pgenre = row.getCell(5).getStringCellValue().trim();
			String sgenre = row.getCell(6).getStringCellValue().trim();
			String creditList[] = credits.split(",");
			String p1genre = row.getCell(7).getStringCellValue().trim();
			String s1genre = row.getCell(8).getStringCellValue().trim();
			String gen = "";
			String roleP = row.getCell(9).getStringCellValue().trim();
			String roleS = row.getCell(10).getStringCellValue().trim();
			String rol = "";
			if(roleP.length() > 0 && roleS.length() > 0)
				rol = roleP+","+roleS;
			else if(roleP.length() > 0)
			{
				rol = roleP;
				
			}
			else
			{
				rol = roleS;
			}
			
			if(pgenre.length() > 0)
			{
				gen = pgenre;
			}
			if(sgenre.length() > 0)
			{ 
				if(gen.length()> 0)
				gen =gen + ","+ sgenre;
				else
				gen = sgenre;
			}
			
			if(p1genre.length() > 0)
			{
				if(gen.length()> 0)
				gen =gen + ","+ p1genre;
				else
				gen = p1genre;
			}
			if(s1genre.length() > 0)
			{ 
				if(gen.length()> 0)
				gen =gen + ","+ s1genre;
				else
				gen = p1genre;
			}
			String genres[] = gen.split(",");
			String roles[] = rol.split(",");
			String phone = row.getCell(11).getStringCellValue().trim();
			String email = row.getCell(12).getStringCellValue().trim();
			String agentname = row.getCell(13).getStringCellValue().trim();
			String agentPhone = row.getCell(14).getStringCellValue().trim();
			String agentmail = row.getCell(15).getStringCellValue().trim();
			String managername = row.getCell(13).getStringCellValue().trim();
			String managerPhone = row.getCell(14).getStringCellValue().trim();
			String managermail = row.getCell(15).getStringCellValue().trim();
			String imdb = row.getCell(15).getStringCellValue().trim();
			String linkedin = row.getCell(15).getStringCellValue().trim();
			String creditsinCommonWithStacy = row.getCell(15).getStringCellValue().trim();
			String creditsincommonwithJohn = row.getCell(15).getStringCellValue().trim();
			//String sourcereferall= row.getCell(15).getStringCellValue().trim();
			//String sourcecomments =row.getCell(15).getStringCellValue().trim();
			//int recordId = Integer.parseInt(row.getCell(1).getNumericValue());
			
			
			//Transaction tx = null;
			try
			{
				session = sessionFactory.getCurrentSession();
				//tx = session.beginTransaction();
				//name = name.trim();
				Talent talent = null;
				Criteria crt = session.createCriteria(Talent.class).add(Restrictions.eq("first_name", firstName)).add(Restrictions.eq("last_name", lastName));
				@SuppressWarnings("rawtypes")
				List talentList = crt.list();
				if(talentList!= null && talentList.size()>0)
				{
					talent = (Talent)talentList.get(0);
					talent.setModifiedby(createdby);
					talent.setModifiedbycomments(comments);
					Talent talent1 = null;
					if(writtingDirectingpartner != null &&  writtingDirectingpartner.trim().length() > 0)
					{
						talent1 = new Talent();
						talent1.setCreatedby(createdby);
						talent1.setCreatedbycomments(comments);
						Criteria crt1 = session.createCriteria(Ethnicity.class).add(Restrictions.eq("name", ""));
					    List eth =  crt1.list();
					    if(eth!= null && eth.size()>0)
						{
					    	talent1.setEthnicity_id(((Ethnicity)eth.get(0)).getId());
						}
					    else
					    {
					    	Ethnicity ethn = new Ethnicity();
					    	ethn.setName("");
					    	session.save(ethn);
					    	talent1.setEthnicity_id(ethn.getId());
					    }
						if(writtingDirectingpartner.split(" ").length > 1)
						{
							talent1.setFirst_name(writtingDirectingpartner.split(" ")[0]);
							talent1.setLast_name(writtingDirectingpartner.split(" ")[1]);
						}
						else
						{
							talent1.setFirst_name(writtingDirectingpartner.split(" ")[0]);
						}
						session.save(talent1);
						++talentcount;
						
					}
					if(talent1!=null)
					talent.setPartner(talent1.getId()+"");
					
						Criteria crt1 = session.createCriteria(Ethnicity.class).add(Restrictions.eq("name", ""));
					    List eth =  crt1.list();
					    if(eth!= null && eth.size()>0)
						{
					    	//talent.setEthnicity((Ethnicity)eth.get(0));
					    	talent.setEthnicity_id(((Ethnicity)eth.get(0)).getId());
						}
					    else
					    {
					    	Ethnicity ethn = new Ethnicity();
					    	ethn.setName("");
					    	session.save(ethn);
					    	//talent.setEthnicity(ethn);
					    	talent.setEthnicity_id(ethn.getId());
					    }
					
					
					session.saveOrUpdate(talent);
					
				}
				else
				{
					Talent talent1 = null;
					if(writtingDirectingpartner != null &&  writtingDirectingpartner.trim().length() > 0)
					{
						talent1 = new Talent();
						talent1.setCreatedby(createdby);
						talent1.setCreatedbycomments(comments);
						Criteria crt1 = session.createCriteria(Ethnicity.class).add(Restrictions.eq("name", ""));
					    List eth =  crt1.list();
					    if(eth!= null && eth.size()>0)
						{
					    	talent1.setEthnicity_id(((Ethnicity)eth.get(0)).getId());
						}
					    else
					    {
					    	Ethnicity ethn = new Ethnicity();
					    	ethn.setName("");
					    	session.save(ethn);
					    	talent1.setEthnicity_id(ethn.getId());
					    }
						if(writtingDirectingpartner.split(" ").length > 1)
						{
							talent1.setFirst_name(writtingDirectingpartner.split(" ")[0]);
							talent1.setLast_name(writtingDirectingpartner.split(" ")[1]);
						}
						else
						{
							talent1.setFirst_name(writtingDirectingpartner.split(" ")[0]);
						}
						session.save(talent1);
						++talentcount;
						
					}
					talent = new Talent();
					talent.setCreatedby(createdby);
					talent.setCreatedbycomments(comments);
					//session.save(gen);
					talent.setFirst_name(firstName);
					talent.setLast_name(lastName);
					if(talent1 != null)
					talent.setPartner(talent1.getId()+"");
					++talentcount;
					
					Criteria crt1 = session.createCriteria(Ethnicity.class).add(Restrictions.eq("name", ""));
				    List eth =  crt1.list();
				    if(eth!= null && eth.size()>0)
					{
				    	talent.setEthnicity_id(((Ethnicity)eth.get(0)).getId());
					}
				    else
				    {
				    	Ethnicity ethn = new Ethnicity();
				    	ethn.setName("");
				    	session.save(ethn);
				    	talent.setEthnicity_id(ethn.getId());
				    }
				    
				   
					session.save(talent);
					
					
				}
				if(email.length() > 0)
				talent.setEmail(email);
				errorText = "Possible error in Agent";
				if(!agentname.contains("No Agent") && agentname.length()==0)
				{
					addNewAssociate(session,agentname,agentmail, agentPhone,null,talent, "Agent");
				
				}
				errorText = "Possible error in Manager";				
				if(!managername.contains("No Manager") && managername.length() ==0)
				{
					addNewAssociate(session,managername,managermail, managerPhone,null,talent, "Manager");
					
				}
				
				for(String cr :creditList)
				{
					cr = cr.trim();
					if(cr.length() ==0)
						continue;
					Credits credits1 = null;
					if(roles.length > 0)
					for(String role : roles)
					{
						role = role.trim();
						if(role == null || role.length() == 0)
							continue;
						
						if(rolesHashMap.get(role.trim()) == null)
						{
							Role role1 = new Role();
							role1.setName(role.trim());
							session.save(role1);
							rolesHashMap.put(role1.getName(), role1.getId()+"");
						}
						
					CreditTalentRoleMapping ctr = new CreditTalentRoleMapping();
					ctr.setRole_id(Integer.parseInt(rolesHashMap.get(role.trim())));
					ctr.setTalent_id(talent.getId());
					Criteria crt1 = session.createCriteria(Credits.class).add(Restrictions.eq("name", cr.trim()));
					List cred = crt1.list();
					if(cred.size() > 0)
					{
						credits1 = ((Credits)cred.get(0));
						ctr.setCredit_id(credits1.getId());
					}
					else
					{
						credits1 = new Credits();
						credits1.setName(cr);
						credits1.setCreatedby(createdby);
						credits1.setCreatedbycomments(comments);
						session.save(credits1);
						++creditcount;
						session.flush();
						ctr.setCredit_id(credits1.getId());
					}
					Criteria crttalentcreditrole = session.createCriteria(CreditTalentRoleMapping.class).add(Restrictions.eq("credit_id", ctr.getCredit_id())).add(Restrictions.eq("talent_id", ctr.getTalent_id())).add(Restrictions.eq("role_id", ctr.getRole_id()));
					List exist = crttalentcreditrole.list();
					if(exist == null || exist.size() ==0)
					session.save(ctr);
					
					}
					
					if(credits1 == null)
					{
						credits1 = new Credits();
						credits1.setName(cr);
						credits1.setCreatedby(createdby);
						credits1.setCreatedbycomments(comments);
						++creditcount;
						session.save(credits1);
						session.flush();
					}
					
					for(String genr: genres)
					{
						genr = genr.trim();
						if(genr == null || genr.length() ==0)
							continue;
						Criteria crt2 = session.createCriteria(Genres.class).add(Restrictions.eq("name", genr));
						@SuppressWarnings("rawtypes")
						List genrList = crt2.list();
						if(genrList!= null && genrList.size()>0)
						{
							
							boolean ignore = false;
							if(credits1.getGenres()!= null)
							for(Genres g : credits1.getGenres())
							{
								if(g.getName().equalsIgnoreCase(genr))
								{
									ignore = true;
								}
							}
							if(!ignore)
							{
								credits1.getGenres().add((Genres)genrList.get(0));
							}
						}
						else
						{
							boolean ignore = false;
							for(Genres g : credits1.getGenres())
							{
								if(g.getName().equalsIgnoreCase(genr))
								{
									ignore = true;
								}
							}
							if(ignore)
							{
							Genres gen1 = new Genres();
							gen1.setName(genr);
							credits1.getGenres().add(gen1);
							}
							//session.save(gen);
						}
					}
					

				}
				
				//if(tx != null)
				//{
					session.flush();
					//tx.commit();
					
				//}
				
			}
			
			catch(Exception e)
			{
				session.clear();
				e.printStackTrace();
				
				sb.append(row.getRowNum()+":"+row.getCell(0).getStringCellValue()+System.getProperty("line.separator"));
				copyRow(spreadsheet, row, ++rowcount, errorText);
				continue;
			}
			finally
			{
				//if(session.isOpen())
				//session.close();
				
			}
			
		}
		
		try
		{
			session = sessionFactory.getCurrentSession();
			//Transaction tx = session.beginTransaction();
			UploadActivity ua = (UploadActivity) session.load(UploadActivity.class, uaId);
			
			ua.setCreditrecordcount(creditcount);
			long currenttime = new Date().getTime();
			ua.setTalentrecordcount(talentcount);
			ua.setRolerecordcount(rolerecordcount);
			ua.setId(uaId);
			ua.setFilename(filename);
			ua.setFilestatus("SUCCESS");
			System.out.println("Time in mili seconds"+ (currenttime-startTime));
			ua.setTimeinminutes((currenttime-startTime)/(1000*60));
			session.saveOrUpdate(ua);
			session.flush();//tx.commit();
			//if(session.isOpen())
			//session.close();
			
			
			
			
		}
		catch(Exception e)
		{
			session.clear();
			e.printStackTrace();
			//if(session.isOpen())
			//session.close();
		}
		
		return sb.toString();
	}
	
	
	private static void copyRow(XSSFSheet worksheet, Row sourceRow, int destinationRowNum, String associateerror ) {
        // Get the source / new row
        XSSFRow newRow = worksheet.getRow(destinationRowNum);
       // HSSFRow sourceRow = worksheet.getRow(sourceRowNum);

        // If the row exist in destination, push down all rows by 1 else create a new row
        if (newRow != null) {
            worksheet.shiftRows(destinationRowNum, worksheet.getLastRowNum(), 1);
        } else {
            newRow = worksheet.createRow(destinationRowNum);
        }

        // Loop through source columns to add to new row
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            // Grab a copy of the old/new cell
            Cell oldCell = sourceRow.getCell(i);
            XSSFCell newCell = newRow.createCell(i);

            // If the old cell is null jump to next cell
            if (oldCell == null) {
                newCell = null;
                continue;
            }

           /** // Copy style from old cell and apply to new cell
           // HSSFCellStyle newCellStyle = worksheet.createCellStyle();
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
            ;
            newCell.setCellStyle(newCellStyle);**/

            // If there is a cell comment, copy
            if (oldCell.getCellComment() != null) {
                newCell.setCellComment(oldCell.getCellComment());
            }

            // If there is a cell hyperlink, copy
            if (oldCell.getHyperlink() != null) {
                newCell.setHyperlink(oldCell.getHyperlink());
            }

            // Set the cell data type
            newCell.setCellType(oldCell.getCellType());

            // Set the cell data value
            switch (oldCell.getCellType()) {
                case Cell.CELL_TYPE_BLANK:
                    newCell.setCellValue(oldCell.getStringCellValue());
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    newCell.setCellValue(oldCell.getBooleanCellValue());
                    break;
                case Cell.CELL_TYPE_ERROR:
                    newCell.setCellErrorValue(oldCell.getErrorCellValue());
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    newCell.setCellFormula(oldCell.getCellFormula());
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    newCell.setCellValue(oldCell.getNumericCellValue());
                    break;
                case Cell.CELL_TYPE_STRING:
                    newCell.setCellValue(oldCell.getRichStringCellValue());
                    break;
            }
            
           
        }
        
        if(associateerror!= null && associateerror.length() > 0)
        {
        	XSSFCell cell = newRow.createCell(sourceRow.getLastCellNum());
        	cell.setCellValue(associateerror);
        }

        
    }
	
	
	
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly= true)
	
	public  Workbook exportFile( List<Integer> ids) throws IOException {
		Map<String,String> genresHashMap = new HashMap<String,String>();
		Map<String,String> rolesHashMap = new HashMap<String,String>();
		Map<String,String> associateHashMap = new HashMap<String,String>();
		XSSFWorkbook errorbook = new XSSFWorkbook(); 
		//Create a blank spreadsheet
		XSSFSheet spreadsheet = errorbook.createSheet();
		Row row = spreadsheet.createRow((short) 0);
		// Create a cell and put a value in it.
		Cell cell = row.createCell(0);
		cell.setCellValue("First Name");
		
		Cell ce = row.createCell(1);
		ce.setCellValue("Last Name");
		
		Cell cell1 = row.createCell(2);
		cell1.setCellValue("Age");
		
		Cell cell2 = row.createCell(3);
		cell2.setCellValue("Gender");
		
		Cell cell3 = row.createCell(4);
		cell3.setCellValue("Roles");
		
		Cell cell4 = row.createCell(5);
		cell4.setCellValue("Ethnicity");
		
		Cell cell5 = row.createCell(6);
		cell5.setCellValue("Genres");
		
		Cell cell61 = row.createCell(7);
		cell61.setCellValue("Agent");
		
		/**Cell cell62 = row.createCell(7);
		cell62.setCellValue("Agent Email");
		
		Cell cell63 = row.createCell(8);
		cell63.setCellValue("Agent Phone");
		
		Cell cell64 = row.createCell(9);
		cell64.setCellValue("Agent Company");**/
		
		
		Cell cell611 = row.createCell(8);
		cell611.setCellValue("Attorney");
		
		/**Cell cell621 = row.createCell(11);
		cell621.setCellValue("Attorney Email");
		
		Cell cell631 = row.createCell(12);
		cell631.setCellValue("Attorney Phone");
		
		Cell cell641 = row.createCell(13);
		cell641.setCellValue("Attorney Company");**/
		
		
		
		Cell cell6111 = row.createCell(9);
		cell6111.setCellValue("Manager");
		
		/**Cell cell6211 = row.createCell(15);
		cell6211.setCellValue("Manager Email");
		
		Cell cell6311 = row.createCell(16);
		cell6311.setCellValue("Manager Phone");
		
		Cell cell6411 = row.createCell(17);
		cell6411.setCellValue("Manager Company");**/
		
		
		
		Cell cell61111 = row.createCell(10);
		cell61111.setCellValue("Publicist");
		
		/**Cell cell62111 = row.createCell(19);
		cell62111.setCellValue("Publicist Email");
		
		Cell cell63111 = row.createCell(20);
		cell63111.setCellValue("Publicist Phone");
		
		Cell cell64111 = row.createCell(21);
		cell64111.setCellValue("Publicist Company");**/
		
		
		Cell cell10 = row.createCell(11);
		cell10.setCellValue("Email");
		
		Cell cell11 = row.createCell(12);
		cell11.setCellValue("Phone");
		
		Cell cell12 = row.createCell(13);
		cell12.setCellValue("Facebook");
		
		Cell cell13 = row.createCell(14);
		cell13.setCellValue("Twitter");
		
		Cell cell14 = row.createCell(15);
		cell14.setCellValue("InstaGram");
		
		Cell cell15 = row.createCell(16);
		cell15.setCellValue("Vine Url");
		
		Cell cell16 = row.createCell(17);
		cell16.setCellValue("You Tube URL");
		
		Session session = null;
		try
		{

			//sessionFactory = AppFactory.getSessionFactory();
			session = sessionFactory.getCurrentSession();
		
			List<Role> roles = session.createQuery("from Role").list();
			
			List<AssociateType> associateType = session.createQuery("from AssociateType").list();
			
			List<Genres> genres = session.createQuery("from Genres").list();
			for(Role rl : roles)
			{
				rolesHashMap.put( rl.getId()+"", rl.getName());
			}
			for(AssociateType ast : associateType)
			{
				associateHashMap.put( ast.getId()+"", ast.getType());
			}
			for(Genres gnr : genres)
			{
				genresHashMap.put( gnr.getId()+"", gnr.getName());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			/**if(session.isOpen())
			session.close();**/
		}
		
		try
		{

			//sessionFactory = AppFactory.getSessionFactory();
			List<Talent> talents = null;
			try
			{
			session = sessionFactory.getCurrentSession();
			talents = session.createCriteria(Talent.class).add(Restrictions.in("id", ids)).list();
			}
			catch(Exception e)
			{
			e.printStackTrace();	
			}
			finally
			{
			/**if(session.isOpen())
				session.close();**/
			}
			int count =1;
			if(talents != null)
			for(Talent talent :talents)
			{
				
				Row row1 = spreadsheet.createRow(count);
				++count;
				// Create a cell and put a value in it.
				Cell cel = row1.createCell(0);
				cel.setCellValue(talent.getFirst_name());
				
				Cell c = row1.createCell(1);
				c.setCellValue(talent.getLast_name());
				
				Cell cel1 = row1.createCell(2);
				cel1.setCellValue(talent.getAge());
				
				Cell cel2 = row1.createCell(3);
				cel2.setCellValue(talent.getGender());
				
				Cell cel3 = row1.createCell(4);
				session = sessionFactory.getCurrentSession();
				
				List<CreditTalentRoleMapping> ctrlist = session.createCriteria(CreditTalentRoleMapping.class).add(Restrictions.eq("talent_id",talent.getId())).list();
				StringBuilder role = new StringBuilder();
				StringBuilder geners = new StringBuilder();
				for(CreditTalentRoleMapping ctr:  ctrlist)
				{
					Credits cr = ((Credits) session.get(Credits.class, ctr.getCredit_id()));
					if(cr == null)
						continue;
					role.append(rolesHashMap.get(ctr.getRole_id()+""));
					role.append(" for ");
					role.append(cr.getName());
					role.append(",");
					if(cr.getGenres() != null)
					{
						for(Genres gen :cr.getGenres() )
						{
							geners.append(gen.getName());
							geners.append(" for ");
							geners.append(cr.getName());
							geners.append(",");
						}
					}
				}
				cel3.setCellValue(role.toString());
				Cell cel44 = row1.createCell(5);
				try
				{
				Ethnicity eth = (Ethnicity) session.load(Ethnicity.class, talent.getEthnicity_id());
				cel44.setCellValue(eth.getName());
				}
				catch(Exception e)
				{
					cel44.setCellValue("");
				}
				Cell cel4 = row1.createCell(6);
				cel4.setCellValue(geners.toString());
				
				
				try
				{
				List<AssociateTalentAssociateTypeMapping> atsTypeList = session.createCriteria(AssociateTalentAssociateTypeMapping.class).add(Restrictions.like("talent_id", talent.getId()))
						.list();
				Associate agent = null;
				String agentString = "";
				Company agentCompany = null;
				Associate attorney = null;
				
				String attorneyString = "";
				Company attorneyCompany = null;
				Associate manager = null;
				
				String managerString = "";
				Company managerCompany =  null;
				Associate publicist = null;
				
				Company publicistCompany = null;
				String publicistString = "";
				if(atsTypeList != null && atsTypeList.size() > 0)
				{
					System.out.println("asscoiate present for talent "+talent.getFirst_name());
					for(AssociateTalentAssociateTypeMapping atsType :  atsTypeList)
					{
						System.out.println(atsType);
						Associate assoc = ((Associate) session.get(Associate.class, atsType.getAssociate_id()));
						if(assoc == null)
							continue;
						
						System.out.println("Assoc object"+ assoc.getFirstName()+atsType.getAssocite_types_id()+"");
						System.out.println(associateHashMap.get(atsType.getAssocite_types_id()+""));
						if(associateHashMap.get(atsType.getAssocite_types_id()+"") != null && associateHashMap.get(atsType.getAssocite_types_id()+"").equals("Agent"))
						{
							agent = assoc;
							System.out.println(agent.getFirstName());
							if(agent.getCompany_id()!= null)
							agentCompany = ((Company) session.get(Company.class, Integer.parseInt(agent.getCompany_id())));
							
							if(agentString.length() == 0)
							{
								agentString = assoc.getFirstName()!= null?assoc.getFirstName():"";
								agentString +=" "+ (assoc.getLastName()!= null?assoc.getLastName():"");	
								agentString +=","+(assoc.getEmail()!=null?assoc.getEmail():"");
								agentString +=","+(assoc.getPhone()!=null?assoc.getPhone():"");
								agentString +=","+((agentCompany!= null && agentCompany.getName()!=null) ? agentCompany.getName() :"") + ";";
							}
							else
							{
								agentString += assoc.getFirstName()!= null?assoc.getFirstName():"";
								agentString +=" "+ (assoc.getLastName()!= null?assoc.getLastName():"");	
								agentString +=","+(assoc.getEmail()!=null?assoc.getEmail():"");
								agentString +=","+(assoc.getPhone()!=null?assoc.getPhone():"");
								agentString +=","+((agentCompany!= null && agentCompany.getName()!=null)?agentCompany.getName():"") + ";";
							}
						}
						else if(associateHashMap.get(atsType.getAssocite_types_id()+"") != null && associateHashMap.get(atsType.getAssocite_types_id()+"").equals("Attorney"))
						{
							attorney = assoc;
							if(attorney.getCompany_id()!= null)
							attorneyCompany = ((Company) session.get(Company.class, Integer.parseInt(attorney.getCompany_id())));
							
							if(attorneyString.length() == 0)
							{
								attorneyString = assoc.getFirstName()!= null?assoc.getFirstName():"";
								attorneyString +=" "+ (assoc.getLastName()!= null?assoc.getLastName():"");	
								attorneyString +=","+(assoc.getEmail()!=null?assoc.getEmail():"");
								attorneyString +=","+(assoc.getPhone()!=null?assoc.getPhone():"");
								attorneyString +=","+((attorneyCompany!= null && attorneyCompany.getName()!=null)?attorneyCompany.getName():"") + ";";
							}
							else
							{
								attorneyString += assoc.getFirstName()!= null?assoc.getFirstName():"";
								attorneyString +=" "+ (assoc.getLastName()!= null?assoc.getLastName():"");	
								attorneyString +=","+(assoc.getEmail()!=null?assoc.getEmail():"");
								attorneyString +=","+(assoc.getPhone()!=null?assoc.getPhone():"");
								attorneyString +=","+((attorneyCompany!= null && attorneyCompany.getName()!=null)?attorneyCompany.getName():"") + ";";
							}
							
						}
						else if(associateHashMap.get(atsType.getAssocite_types_id()+"") != null && associateHashMap.get(atsType.getAssocite_types_id()+"").equals("Manager"))
						{
							System.out.println("manager loop enetrs");
							//manager = assoc;
							System.out.println("Assoc object bef"+ assoc.getFirstName()+atsType.getAssocite_types_id()+"");
							if(assoc.getCompany_id()!= null)
							managerCompany = ((Company) session.get(Company.class, Integer.parseInt(assoc.getCompany_id())));
							System.out.println("Assoc object 12"+ assoc.getFirstName()+atsType.getAssocite_types_id()+"");
							if(managerString.length() == 0)
							{
							managerString = assoc.getFirstName()!= null?assoc.getFirstName():"";
							managerString +=" "+ (assoc.getLastName()!= null?assoc.getLastName():"");	
							managerString +=","+(assoc.getEmail()!=null?assoc.getEmail():"");
							managerString +=","+(assoc.getPhone()!=null?assoc.getPhone():"");
							managerString +=","+((managerCompany!= null && managerCompany.getName()!=null) ? managerCompany.getName():"") + ";";
							}
							else
							{
								managerString += assoc.getFirstName()!= null?assoc.getFirstName():"";
								managerString +=" "+ (assoc.getLastName()!= null?assoc.getLastName():"");	
								managerString +=","+(assoc.getEmail()!=null?assoc.getEmail():"");
								managerString +=","+(assoc.getPhone()!=null?assoc.getPhone():"");
								managerString +=","+((managerCompany!= null && managerCompany.getName()!=null) ? managerCompany.getName():"") + ";";
							}
						   System.out.println("manager string"+ managerString);
						}
						else if(associateHashMap.get(atsType.getAssocite_types_id()+"") != null && associateHashMap.get(atsType.getAssocite_types_id()+"").equals("Publicist"))
						{
							publicist = assoc;
							if(publicist.getCompany_id()!= null)
							publicistCompany = ((Company) session.get(Company.class, Integer.parseInt(publicist.getCompany_id())));
							
							if(publicistString.length() == 0)
							{
								publicistString = assoc.getFirstName()!= null?assoc.getFirstName():"";
								publicistString +=" "+ (assoc.getLastName()!= null?assoc.getLastName():"");	
								publicistString +=","+(assoc.getEmail()!=null?assoc.getEmail():"");
								publicistString +=","+(assoc.getPhone()!=null?assoc.getPhone():"");
								publicistString +=","+((publicistCompany!= null && publicistCompany.getName()!=null) ? publicistCompany.getName():"") + ";";
							}
							else
							{
								publicistString += assoc.getFirstName()!= null?assoc.getFirstName():"";
								publicistString +=" "+ (assoc.getLastName()!= null?assoc.getLastName():"");	
								publicistString +=","+(assoc.getEmail()!=null?assoc.getEmail():"");
								publicistString +=","+(assoc.getPhone()!=null?assoc.getPhone():"");
								publicistString +=","+((publicistCompany!= null && publicistCompany.getName()!=null) ? publicistCompany.getName():"") + ";";
							}
								
						}
					}
				}
				
				Cell agentCell = row1.createCell(7);
				
					agentCell.setCellValue(agentString);
				
				
				Cell attorneyCell = row1.createCell(8);
				
					attorneyCell.setCellValue(attorneyString)	;
				
					
				Cell managerCell = row1.createCell(9);
				
					
					System.out.println("Manger"+managerString);
					managerCell.setCellValue(managerString)	;
				
				
				
				Cell publicistCell = row1.createCell(10);
				
					publicistCell.setCellValue(publicistString)	;
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				//agent
				
				
				Cell cel9 = row1.createCell(11);
				cel9.setCellValue(talent.getEmail());
				
				Cell cel10 = row1.createCell(12);
				cel10.setCellValue(talent.getPhone());
				
				Cell cel11 = row1.createCell(13);
				cel11.setCellValue(talent.getFacebook_url());
				
				Cell cel12 = row1.createCell(14);
				cel12.setCellValue(talent.getTwitter_url());
				
				//instagram_url
				Cell cel13 = row1.createCell(15);
				cel13.setCellValue(talent.getInstagram_url());
				
				//vine_url
				Cell cel14 = row1.createCell(16);
				cel14.setCellValue(talent.getVine_url());
				
				Cell cel15 = row1.createCell(17);
				cel15.setCellValue(talent.getYoutube_url());
				/**if(session.isOpen())
					session.close();**/
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			/**if(session.isOpen())
			session.close();**/
		}
		
		
		return errorbook;
	}
	
	
	
	/**
	 * @param sheet
	 * @param session
	 * @return
	 */
	public  String processDRMExportTalentFile(Sheet sheet, Session session, String createdby, String comments, String filetype, String filename,XSSFSheet spreadsheet) {
		
		int creditcount = 0;
		int talentcount = 0;
		int rolerecordcount = 0;
		//int genrerecordcount = 0;
		int totalrecord= 0;
		int namecount = 0;
		int uaId = 0;
		StringBuffer sb = new StringBuffer();
		long startTime = new Date().getTime();
		System.out.println("processTalentFile");
		try
		{
			session = sessionFactory.getCurrentSession();
			//session.setAutoCommit(true);
			//Transaction tx = session.beginTransaction();
			UploadActivity ua = new UploadActivity();
			ua.setCreditrecordcount(creditcount);
			ua.setFilename(filename);
			ua.setUploadedby(createdby);
			ua.setFiletypeselected(filetype);
			
			ua.setFilestatus("INPROGRESS");
			ua.setUploaddate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
			session.save(ua);
			session.flush();
			//tx.commit();
			uaId = ua.getId();
			
			
			
			
			
		}
		catch(Exception e)
		{
			session.clear();
			e.printStackTrace();
		}
		int rowcount=0;
		String errortext = null;
		for(int start= 1; start < sheet.getLastRowNum();start++)
		{
			errortext = null;
			System.out.println("row no: "+1);
			Row row = sheet.getRow(start);
			try
			{
			if(row == null || row.getCell(0) == null || row.getCell(0).getStringCellValue().trim().length() ==0)
			{
				++namecount;
				if(namecount == 5)
				{
					break;
				}
				continue;
				
			}
			++totalrecord;
			String firstName= null;
			try
			{
				firstName = row.getCell(0).getStringCellValue().trim();
			}
			catch(Exception e)
			{
				
			}
			
			String lastName= null;
			try
			{
				lastName = row.getCell(1).getStringCellValue().trim();
			}
			catch(Exception e)
			{
				
			}
			
			String age= null;
			try
			{
				age = row.getCell(2).getStringCellValue().trim();
			}
			catch(Exception e)
			{
				
			}
			
			String gender = null;
			try
			{
				gender = row.getCell(3).getStringCellValue();
			}
			catch(Exception e)
			{
				
			}
			//int recordId = Integer.parseInt(row.getCell(1).getNumericValue());
			String role[] = null;
			try
			{
				role= row.getCell(4).getStringCellValue().split(",");
			}
			catch(Exception e)
			{
				
			}
			
			String ethnicity = null;
			try{
				ethnicity = row.getCell(5).getStringCellValue().trim();
			}
			catch(Exception e)
			{
				
			}
			
			String genre[] = null;
			try
			{
				genre= row.getCell(6).getStringCellValue().split(",");
			}
			catch(Exception e)
			{
				
			}
			
			String agentAssoc[] = null;
			try{
				agentAssoc = row.getCell(7).getStringCellValue().trim().split(";");
			}
			catch(Exception e)
			{
				
			}
			
			String attorneyAssoc[] = null;
			try{
				attorneyAssoc = row.getCell(8).getStringCellValue().trim().split(";");
			}
			catch(Exception e)
			{
				
			}
			
			String managerAssoc[] = null;
			try{
				managerAssoc = row.getCell(9).getStringCellValue().trim().split(";");
			}
			catch(Exception e)
			{
				
			}
			
			String publicistAssoc[] = null;
			try{
				publicistAssoc = row.getCell(10).getStringCellValue().trim().split(";");
			}
			catch(Exception e)
			{
				
			}

			
			String associateerror = "";
			
			String contactInfoEmail = null;
			
			try
			{
				contactInfoEmail= row.getCell(11).getStringCellValue();
			}
			catch(Exception e )
			{
				e.printStackTrace();
				
			}
			
			String contactInfoPhone = null;
			
			try
			{
				contactInfoPhone= row.getCell(12).getStringCellValue();
			}
			catch(Exception e )
			{
				e.printStackTrace();
				
			}
			
			String faceBook = null;
			
			try
			{
				faceBook= row.getCell(13).getStringCellValue();
			}
			catch(Exception e )
			{
				
				
			}
			
			String twitter = null;
			
			try
			{
				twitter= row.getCell(14).getStringCellValue();
			}
			catch(Exception e )
			{
				
				
			}
			
			String instagram = null;
			
			try
			{
				instagram= row.getCell(15).getStringCellValue();
			}
			catch(Exception e )
			{
				
				
			}
			
			String vineurl = null;
			
			try
			{
				vineurl= row.getCell(16).getStringCellValue();
			}
			catch(Exception e )
			{
				
				
			}
			if(firstName == null || firstName.trim().length() == 0)
				continue;
			System.out.println("first name"+firstName);
			
			System.out.println("last name"+lastName);
			System.out.println("gender"+gender);
			
			System.out.println("ethnicity"+ethnicity);
			System.out.println("contactInfoEmail"+contactInfoEmail);
				
			
			
			
			//Transaction tx = null;
			try
			{
				session = sessionFactory.getCurrentSession();
				//tx = session.beginTransaction();
				//name = name.trim();
				Talent talent = null;
				
				
				Criteria crt = session.createCriteria(Talent.class).add(Restrictions.eq("first_name",firstName )).add(Restrictions.eq("last_name", lastName));
				@SuppressWarnings("rawtypes")
				List talentList = crt.list();
				if(talentList!= null && talentList.size()>0)
				{
					talent = (Talent)talentList.get(0);
					talent.setModifiedby(createdby);
					talent.setModifiedbycomments(comments);
					try
					{
					if(Integer.parseInt(age) > 0)
					{
						talent.setAge(Integer.parseInt(age));
					}
					}
					catch(Exception e)
					{
						//ignore
					}
					if(gender != null && (gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("Female")))
					{
						talent.setGender(gender);
					}
					if(ethnicity != null && !(ethnicity.isEmpty() || ethnicity.contains("No Race Available")))
					{
						if(ethnicity.contains("Hispanic"))
						{
							ethnicity = "Latino / Hispanic";
						}
						else if(ethnicity.contains("Southeast Asian/Indian"))
						{
							ethnicity = "Southeast Asian/Indian";
						}
						else if(ethnicity.contains("Indian"))
						{
							ethnicity = "Indian";
						}
						//
						Criteria crt1 = session.createCriteria(Ethnicity.class).add(Restrictions.eq("name", ethnicity.trim()));
					    List eth =  crt1.list();
					    if(eth!= null && eth.size()>0)
						{
					    	//talent.setEthnicity((Ethnicity)eth.get(0));
					    	talent.setEthnicity_id(((Ethnicity)eth.get(0)).getId());
						}
					    else
					    {
					    	Ethnicity ethn = new Ethnicity();
					    	ethn.setName(ethnicity);
					    	session.save(ethn);
					    	//talent.setEthnicity(ethn);
					    	talent.setEthnicity_id(ethn.getId());
					    }
					
					}
					if(faceBook != null)
					{
						talent.setFacebook_url(faceBook);
					}
					if(twitter != null)
					{
						talent.setTwitter_url(twitter);
					}
					if(contactInfoEmail!= null && contactInfoEmail.contains("@"))
					{
						talent.setEmail(contactInfoEmail);
					}
					 if(faceBook != null)
						{
							talent.setFacebook_url(faceBook);
						}
						
						
						if(instagram!=null && instagram.length() > 0)
						{
							talent.setInstagram_url(instagram);
						}
						if(vineurl!=null && vineurl.length() > 0)
						{
							talent.setVine_url(vineurl);
						}
						if(contactInfoPhone!=null && contactInfoPhone.length() > 0)
						{
							talent.setPhone(contactInfoPhone);
						}
						
					errortext = "Possible error in Roles";
					
					
					errortext = "Possible error in Agent or Ethnicity";
					if(agentAssoc!= null &&  agentAssoc.length >0)
					{
						associateerror += addAssociate(session, agentAssoc, talent, "Agent");
					}
					errortext = "Possible error in Attorney or Ethnicity";
					if(attorneyAssoc!= null && attorneyAssoc.length >0)
					{
						associateerror += addAssociate(session, attorneyAssoc, talent, "Attorney");
					}
					errortext = "Possible error in Manager Or Ethnicity";
					if(managerAssoc != null && managerAssoc.length  >0)
					{
						associateerror += addAssociate(session, managerAssoc, talent, "Manager");
					}
					errortext = "Possible error in Publicist";
					if(publicistAssoc != null && publicistAssoc.length  >0)
					{
						associateerror += addAssociate(session, publicistAssoc, talent, "Publicist");
					}
					session.saveOrUpdate(talent);
					
				}
				else
				{
					talent = new Talent();
					talent.setCreatedby(createdby);
					talent.setCreatedbycomments(comments);
					//session.save(gen);
					talent.setFirst_name(firstName);
					
					talent.setLast_name(lastName);
					try
					{
					if(Integer.parseInt(age) >0)
					talent.setAge(Integer.parseInt(age));
					}
					catch(Exception e)
					{
						
					}
					if(gender!= null && (gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("Female")))
					{
						talent.setGender(gender);
					}
					
					
					Criteria crt1 = session.createCriteria(Ethnicity.class).add(Restrictions.eq("name", ethnicity));
				    List eth =  crt1.list();
				    if(eth!= null && eth.size()>0)
					{
				    	talent.setEthnicity_id(((Ethnicity)eth.get(0)).getId());
					}
				    else
				    {
				    	errortext = "Possible error in Ethnicity";
				    	Ethnicity ethn = new Ethnicity();
				    	ethn.setName(ethnicity);
				    	session.save(ethn);
				    	talent.setEthnicity_id(ethn.getId());
				    }
				    
				    if(faceBook != null)
					{
						talent.setFacebook_url(faceBook);
					}
					if(twitter != null)
					{
						talent.setTwitter_url(twitter);
					}
					if(contactInfoEmail != null && contactInfoEmail.contains("@"))
					{
						talent.setEmail(contactInfoEmail);
					}
					if(instagram!=null && instagram.length() > 0)
					{
						talent.setInstagram_url(instagram);
					}
					
					if(vineurl!=null && vineurl.length() > 0)
					{
						talent.setVine_url(vineurl);
					}
					if(contactInfoPhone!=null && contactInfoPhone.length() > 0)
					{
						talent.setPhone(contactInfoPhone);
					}
					session.saveOrUpdate(talent);
					errortext = "Possible error in Roles";
					
					session.saveOrUpdate(talent);
				}
				errortext = "Possible error in Agent";
				if(agentAssoc!= null &&  agentAssoc.length >0)
				{
					associateerror += addAssociate(session, agentAssoc, talent, "Agent");
				}
				errortext = "Possible error in Attorney";
				if(attorneyAssoc!= null && attorneyAssoc.length >0)
				{
					associateerror += addAssociate(session, attorneyAssoc, talent, "Attorney");
				}
				errortext = "Possible error in Manager";
				if(managerAssoc != null && managerAssoc.length  >0)
				{
					associateerror += addAssociate(session, managerAssoc, talent, "Manager");
				}
				errortext = "Possible error in Publicist";
				if(publicistAssoc != null && publicistAssoc.length  >0)
				{
					associateerror += addAssociate(session, publicistAssoc, talent, "Publicist");
				}
				//if(tx != null)
				//{
					session.flush();
					//session.comm
					//tx.commit();
					++talentcount;
				//}
				
			
			errortext = "Possible error in Role And Credit";
			addUpdateRoleAndCredit(session,role,talent);
					
			errortext = "Possible error in Genre And Credit";		
			addUpdateGenreAndCredit(session, genre,talent) ;
			}
			catch(Exception e)
			{
				session.clear();
				e.printStackTrace();
				//errortext = "Possible error in Agent";
				
				sb.append(row.getRowNum()+":"+row.getCell(0).getStringCellValue()+System.getProperty("line.separator"));
				//Row r= spreadsheet.createRow(++rowcount);
				copyRow(spreadsheet, row, ++rowcount, errortext);
				continue;
			}
			
			}
			catch(Exception e)
			{
				try
				{
					sb.append(row.getRowNum()+":"+row.getCell(0).getStringCellValue()+System.getProperty("line.separator"));
					
				}
				catch(Exception e1)
				{
				//ignore	
				}
				continue;
			}
			
		}
		try
		{
			session = sessionFactory.getCurrentSession();
			//Transaction tx = session.beginTransaction();
			UploadActivity ua = (UploadActivity) session.load(UploadActivity.class, uaId);
			
			ua.setCreditrecordcount(creditcount);
			long currenttime = new Date().getTime();
			ua.setTalentrecordcount(talentcount);
			ua.setRolerecordcount(rolerecordcount);
			ua.setFailurecount(totalrecord - talentcount);
			ua.setTotalcount(totalrecord);
			ua.setId(uaId);
			ua.setFilename(filename);
			ua.setFilestatus("SUCCESS");
			ua.setTimeinminutes((currenttime-startTime)/(1000*60));
			session.saveOrUpdate(ua);
			session.flush();
			//tx.commit();	
		}
		catch(Exception e)
		{
			session.clear();
			e.printStackTrace();
		}	
		return sb.toString();
	}

	/**
	 * @param session
	 * @param name
	 * @param agentString
	 * @param talent
	 */
	private String addAssociate(Session session,String[] agentString,
			Talent talent, String key) {
		String possiblerror = "";
		
		String companyname = null;
		String agentName = null;
		String agentMobNo = null;
		String agentEmail = null;
		for(String ag : agentString)
		{
			if(ag== null || ag.trim().length() == 0)
				continue;
			
				companyname = ag.split(",")[3];
				agentName = ag.split(",")[0];
				 String lastName = "";
				    String firstName= "";
				    if(agentName.split("\\w+").length>1){

				       lastName = agentName.substring(agentName.lastIndexOf(" ")+1);
				       firstName = agentName.substring(0, agentName.lastIndexOf(' '));
				    }
				     else{
				       firstName = agentName;
				    }
				agentEmail = ag.split(",")[1];
				agentMobNo = ag.split(",")[2];
				
			Criteria crt1 = session.createCriteria(Associate.class).add(Restrictions.eq("firstName", firstName.trim())).add(Restrictions.eq("lastName", lastName.trim()));
			@SuppressWarnings("rawtypes")
			List assocList = crt1.list();
			if(assocList == null || assocList.size()==0)
			{
				Company comp = null;
				if(companyname != null)
				{
					Criteria crtcomp = session.createCriteria(Company.class).add(Restrictions.eq("name", companyname.trim()));
					@SuppressWarnings("rawtypes")
					List companyList = crtcomp.list();
					if(companyList == null || companyList.size() == 0)
					{
					comp = new Company();
					comp.setName(companyname);
					session.save(comp);
					session.flush();
					}
					else
					{
						comp = (Company)companyList.get(0);
					}
				}
				Associate assoc = new Associate();
				assoc.setFirstName(firstName);
				assoc.setLastName(lastName);
				assoc.setPhone(agentMobNo);
				assoc.setEmail(agentEmail);
				if(comp != null && comp.getId() != 0)
				{
					assoc.setCompany_id(comp.getId()+"");
				}
				session.save(assoc);
				session.flush();
				AssociateTalentAssociateTypeMapping atam = new AssociateTalentAssociateTypeMapping();
				atam.setAssociate_id(assoc.getId());
				atam.setAssocite_types_id(Integer.parseInt(associateHashMap.get(key)));
				atam.setTalent_id(talent.getId());
				session.save(atam);
				session.flush();
			}
			else
			{
				
				//asscoiate does not chage for life time
				Associate assoc = (Associate)assocList.get(0);
				System.out.println("associatess name"+assoc.getFirstName());
				System.out.println("talent name"+talent.getFirst_name());
				if(agentMobNo!=null && agentMobNo.length() > 0)
				assoc.setPhone(agentMobNo);
				if(agentEmail!=null && agentEmail.length() > 0)
				assoc.setEmail(agentEmail);
				
				Company comp = null;
				if(companyname != null)
				{
					Criteria crtcomp = session.createCriteria(Company.class).add(Restrictions.eq("name", companyname.trim()));
					@SuppressWarnings("rawtypes")
					List companyList = crtcomp.list();
					if(companyList == null || companyList.size() == 0)
					{
					comp = new Company();
					comp.setName(companyname);
					session.save(comp);
					session.flush();
					}
					else
					{
						comp = (Company)companyList.get(0);
					}
				}
				if(comp != null && comp.getId() != 0)
				{
					assoc.setCompany_id(comp.getId()+"");
				}
				
				session.saveOrUpdate(assoc);
				session.flush();
				System.out.println("after flusg");
				Criteria crtast = session.createCriteria(AssociateTalentAssociateTypeMapping.class).add(Restrictions.eq("associate_id", assoc.getId()))
						.add(Restrictions.eq("associte_types_id", Integer.parseInt(associateHashMap.get(key))))
						.add(Restrictions.eq("talent_id", talent.getId()));
				@SuppressWarnings("rawtypes")
				List astmList = crtast.list();
				System.out.println("after atm relation");
				
				if(astmList== null || astmList.size() == 0)
				{
					System.out.println("join not here");
				AssociateTalentAssociateTypeMapping atam = new AssociateTalentAssociateTypeMapping();
				atam.setAssociate_id(assoc.getId());
				atam.setAssocite_types_id(Integer.parseInt(associateHashMap.get(key)));
				atam.setTalent_id(talent.getId());
				session.save(atam);
				session.flush();
				}
			}
			
			
						
		}
		return possiblerror;
	}
	
	
	/**
	 * @param session
	 * @param name
	 * @param agentString
	 * @param talent
	 */
	private String addUpdateRoleAndCredit(Session session,String[] roleString,
			Talent talent) {
		String possiblerror = "";
		
		String role = null;
		String credits = null;
		
		for(String roleCredit : roleString)
		{
			if(roleCredit == null || roleCredit.trim().length() == 0 || !roleCredit.trim().contains(" for "))
				continue;
			role = roleCredit.split(" for ")[0];
			credits	= roleCredit.split(" for ")[1];
			Criteria crt1 = session.createCriteria(Credits.class).add(Restrictions.eq("name", credits.trim()));
			@SuppressWarnings("rawtypes")
			List credit = crt1.list();
			if(credit == null || credit.size()==0)
			{
				String roleId = rolesHashMap.get(role.trim());
				if(roleId == null)
				{
					Role rle = new Role();
					rle.setName(role.trim());
					session.save(rle);
					session.flush();
					roleId = rle.getId()+"";
				}
				Credits cr = new Credits();	
				cr.setName(credits.trim());
				session.save(cr);
				session.flush();
				CreditTalentRoleMapping ctrm = new CreditTalentRoleMapping();
				ctrm.setCredit_id(cr.getId());
				ctrm.setTalent_id(talent.getId());
				ctrm.setRole_id(Integer.parseInt(roleId));
				session.save(ctrm);
				session.flush();
			}
			else
			{
				
				String roleId = rolesHashMap.get(role.trim());
				if(roleId == null)
				{
					Role rle = new Role();
					rle.setName(role.trim());
					session.save(rle);
					session.flush();
					roleId = rle.getId()+"";
				}
				Credits cr = (Credits)credit.get(0);
				Criteria crt = session.createCriteria(CreditTalentRoleMapping.class).add(Restrictions.eq("credit_id", cr.getId()))
						.add(Restrictions.eq("role_id", Integer.parseInt(roleId)))
						.add(Restrictions.eq("talent_id", talent.getId()));
				List creditTalentRoleMappingLust = crt.list();
				if(creditTalentRoleMappingLust == null && creditTalentRoleMappingLust.size() == 0)
				{
					CreditTalentRoleMapping ctrm = new CreditTalentRoleMapping();
					ctrm.setCredit_id(cr.getId());
					ctrm.setTalent_id(talent.getId());
					ctrm.setRole_id(Integer.parseInt(roleId));
					session.save(ctrm);
					session.flush();
				}
				
				
			}
			
			
						
		}
		return possiblerror;
	}
	
	
	
	
	/**
	 * @param session
	 * @param name
	 * @param agentString
	 * @param talent
	 */
	private String addUpdateGenreAndCredit(Session session,String[] genreString,
			Talent talent) {
		String possiblerror = "";
		
		String genre = null;
		String credits = null;
		
		for(String genreCredit : genreString)
		{
			if(genreCredit == null || genreCredit.trim().length() == 0 || !genreCredit.trim().contains(" for "))
				continue;
			genre = genreCredit.split(" for ")[0];
			credits	= genreCredit.split(" for ")[1];
			Criteria crt1 = session.createCriteria(Credits.class).add(Restrictions.eq("name", credits.trim()));
			@SuppressWarnings("rawtypes")
			List credit = crt1.list();
			if(credit == null || credit.size()==0)
			{
				String genreId = genresHashMap.get(genre.trim());
				Genres gnre = null;
				if(genreId == null)
				{
					gnre = new Genres();
					gnre.setName(genre.trim());
					//session.save(gnre);
					//session.flush();
					//genreId = gnre.getId()+"";
				
				Credits cr = new Credits();	
				cr.setName(credits.trim());
				cr.getGenres().add(gnre);
				session.save(cr);
				session.flush();
				}
				else
				{
					Criteria cr = session.createCriteria(Genres.class).add(Restrictions.eq("name", genre.trim()));
					gnre = (Genres)cr.list().get(0);
					Credits cr1 = new Credits();	
					cr1.setName(credits.trim());
					//if(!cr1.getGenres().contains(gnre))
					cr1.getGenres().add(gnre);
					session.saveOrUpdate(cr1);
					session.flush();
				}
				
			}
			else
			{
				Credits cr  = (Credits)credit.get(0);
				String genreId = genresHashMap.get(genre.trim());
				Genres gnre = null;
				if(genreId == null)
				{
					gnre = new Genres();
					gnre.setName(genre.trim());
					cr.getGenres().add(gnre);
				session.saveOrUpdate(cr);
				session.flush();
				}
				else
				{
					Criteria crteri = session.createCriteria(Genres.class).add(Restrictions.eq("name", genre.trim()));
					gnre = (Genres)crteri.list().get(0);
					
					if(!cr.getGenres().contains(gnre))
						cr.getGenres().add(gnre);
					session.saveOrUpdate(cr);
					session.flush();
				}	
				
			}
			
			
						
		}
		return possiblerror;
	}

	

}
