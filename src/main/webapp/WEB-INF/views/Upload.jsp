<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" 
           uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
     "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <title>Social Media Studios</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
  <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
</head>
<body>
    
    <div class="container">
        <div class="page-header">
                <h1>Social Media -  <small>XLSX File to Upload</small></h1>
        </div>
        <div class="row">
            <p class="text-center"></p>
            <form method="post" action="doUpload" enctype="multipart/form-data"  role="form" class="form-horizontal">
                    <div class="form-group">
                            
                            <div class="col-md-3">
                               Created By: <input type="text" name="createdby"/>
                                
                            </div>
                            <div class="col-md-3">
                               Comments: <input type="text" name="comments"/>
                                
                            </div>
                            
                            <div class="col-md-3">
                               Type Of file: <select name="filetype">
  												<option value="Talent">Talent</option>
  												<option value="Credits">Credits</option>
  												<option value="Stacy File">Stacy File</option>
  												<option value="DRM File">DRM File</option>
											</select>
                                
                            </div>
                            
                            
                     </div>
                        
                        <div class="form-group">
                        
                        	<div class="col-md-3">
                                <input type="file" name="fileUpload"/>
                                
                            </div>
                            
                            
                            <div class="col-md-3">
                                <input type="submit" value="Upload" class="btn btn-info" />
                                
                            </div>
                        
                        </div>
                    
                </form> 
        </div>
         <c:if test="${uploadmsg != null}">
        <div style="color:red;" class="row">
       
        <c:out value="${uploadmsg}"/> <a href="download">For downloading failure records click here</a>
        </div>
        </c:if>
        <c:if test="${errormsg != null}">
        <div style="color:red;" class="row">
        <c:out value="${errormsg}"/>
        </div>
        </c:if>
        
        <div>
				<table class="table table-striped table-bordered" style="margin-left: -15px">
				<thead>
					<tr>
						<th width="5%">ID</th>
						<th width="20%">FileName</th>
						<th width="5%">Credit RecordCount</th>
						<th width="5%">Talent Record Count</th>
						<th width="5%">Total Record Count</th>
						<th width="5%">Total Record Loss</th>
						<th width="15%" >File Status</th>
						<th width="5%">Uploading Time In Minutes</th>
						<th width="15%">UploadedBy</th>
						<th width="10%">FileTypeSelected</th>
						<th width="10%">Date </th>
					</tr>
				<thead>
				<tbody>
				<c:forEach var="up" items="${uploadactivity}">
					<tr>
						<td width="5%">${up.id}</td>
						<td width="20%">${up.filename}</td>		
						<td width="5%">${up.creditrecordcount}</td>
						<td width="5%">${up.talentrecordcount}</td>
						<td width="5%">${up.totalcount}</td>
						<td width="5%">${up.failurecount}</td>
						<td width="15%">${up.filestatus}</td>
						<td width="5%">${up.timeinminutes}</td>
						<td width="15%">${up.uploadedby}</td>
						<td width="10%">${up.filetypeselected}</td>
						<td width="10%">${up.uploaddate}</td>
						
					</tr>
					</c:forEach>
				<tbody>
				</table>
        </div>
</div>
</body>
</html>