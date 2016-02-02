package net.codejava.fileupload.controller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.codejava.fileupload.dao.DRMExcelSheetReader;
import net.codejava.fileupload.model.PayLoad;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestControllers {
	@Autowired
	private DRMExcelSheetReader dRMExcelSheetReader;
    @RequestMapping(value="/talent/xlsx", method = RequestMethod.POST)
    public void doDownload(HttpServletRequest request,
            HttpServletResponse response,@RequestBody PayLoad payload) throws IOException
            {
    	//System.out.println(payload);
    	//for(Integer id : payload.getPay())
    		//System.out.println("id"+id);
    	Workbook errorlog =  dRMExcelSheetReader.exportFile(payload.getPay());
        // get MIME type of the file
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
           // mimeType = "application/octet-stream";
        Sheet sheet = errorlog.getSheetAt(0);
		Row firstRow = sheet.getRow(0);
       //System.out.println(firstRow.getCell(0).getStringCellValue());
       // System.out.println("MIME type: " + mimeType);

        // set content attributes for the response
        response.setContentType(mimeType);
       
        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("inline; filename=\"%s\"",
                "DRMExportFile.xlsx");
        response.setHeader(headerKey, headerValue);
        //Access-Control-Allow-Headers: Content-Type
       // Access-Control-Allow-Methods: GET, POST, OPTIONS
        //Access-Control-Allow-Origin: *
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Origin", "*");
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
        response.setContentLength(bytes.length);

            outStream.write(bytes);
        

            outStream.flush();
        outStream.close();
            }
 
}
