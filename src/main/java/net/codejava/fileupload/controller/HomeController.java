package net.codejava.fileupload.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.codejava.fileupload.dao.DRMExcelSheetReader;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * Handles requests for the file upload page.
 */
@Controller
public class HomeController {
	@Autowired
	private DRMExcelSheetReader dRMExcelSheetReader;
	
	private String filePath = "/opts";
	/**
     * Size of a byte buffer to read/write file
     */
    private static final int BUFFER_SIZE = 4096;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String showUploadForm(HttpServletRequest request, ModelMap map) {
		map.put("uploadactivity", dRMExcelSheetReader.getActivity(null));
		request.getSession().removeAttribute("errorlog");
		return "Upload";
	}
	
    @RequestMapping(value = "/doUpload", method = RequestMethod.POST)
    public String handleFileUpload(HttpServletRequest request,ModelMap map,
            @RequestParam CommonsMultipartFile[] fileUpload) throws Exception {
         
    	String createdby = request.getParameter("createdby");
    	String comments = request.getParameter("comments");
    	String filetype = request.getParameter("filetype");
    	Workbook errorlogs = null;
    	boolean invalidFormat = false;
        if (fileUpload != null && fileUpload.length > 0) {
            for (CommonsMultipartFile aFile : fileUpload){
                 if(aFile == null || aFile.getOriginalFilename() == null)
                	 continue;
                 System.out.println("Saving file: " + aFile.getOriginalFilename());
               
                 try
                 {
                 errorlogs = dRMExcelSheetReader.processfile(aFile.getInputStream(), createdby, comments, filetype, aFile.getOriginalFilename());     
            
                 }
                 catch(Exception e)
                 {
                	 invalidFormat = true;
                 }
        }
        }
            if(invalidFormat)
            {
            	map.put("uploadactivity", dRMExcelSheetReader.getActivity(null));
            	map.put("errormsg", "Invalid File Format.");
            }
            else
            {
            	request.getSession().setAttribute("errorlog", errorlogs);
            	map.put("uploadactivity", dRMExcelSheetReader.getActivity(null));
            	map.put("uploadmsg", "File succesfully uploaded.");
            }
        return "Upload";
    }	
    
    /**
    * Method for handling file download request from client
    */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
   public void doDownload(HttpServletRequest request,
           HttpServletResponse response) throws IOException {

       
    	Workbook errorlog =  (Workbook)request.getSession().getAttribute("errorlog");
       // get MIME type of the file
       String mimeType = "application/vnd.openxml";
          // mimeType = "application/octet-stream";
      
       System.out.println("MIME type: " + mimeType);

       // set content attributes for the response
       response.setContentType(mimeType);
       //response.setContentLength((int) downloadFile.length());

       // set headers for the response
       String headerKey = "Content-Disposition";
       String headerValue = String.format("attachment; filename=\"%s\"",
               "errorlogs.xlsx");
       response.setHeader(headerKey, headerValue);

       // get output stream of the response
       OutputStream outStream = response.getOutputStream();


       // write bytes read from the input stream into the output stream
       ByteArrayOutputStream bos = new ByteArrayOutputStream();
       try {
    	   errorlog.write(bos);
       } finally {
           bos.close();
       }
       byte[] bytes = bos.toByteArray();
           outStream.write(bytes);
       

       
       outStream.close();

   
}
}
