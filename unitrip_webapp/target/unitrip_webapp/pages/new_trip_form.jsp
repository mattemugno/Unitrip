<%--
  Created by IntelliJ IDEA.
  User: pucci
  Date: 07/04/2022
  Time: 12:52
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <link href="<%=request.getContextPath()%>/resources/css/newTrip.css" rel="stylesheet" type="text/css"/>
    <title>New trip</title>
</head>
<body>
<%if(request.getSession().getAttribute("status") != null){%>
alert(<%=request.getSession().getAttribute("status")%>);
<%}%>
<div>
    <h1>Compile this form to create a new Trip</h1>
    <form action="<%=request.getContextPath()%>/NewTripServlet" method="post">
        <ul class="form-style-1">
            <li><label>Destination <span class="required">*</span></label><input type="text" name="destination" class="field-divided" placeholder="city" required/> </li>
            <li><label>Trip name <span class="required">*</span></label><input type="text" name="trip_name" class="field-divided" placeholder="name" required/> </li>
            <li>
                <label>Founder<span class="required">*</span> </label>
                <input type="text" name="founder" class="field-long" value="<%=request.getSession().getAttribute("username")%>" disabled/>
            </li>
            <li><label>Available seats <span class="required">*</span></label><input type="number" name="seats" class="field-divided" placeholder="0" required/> </li>
            <li>
                <label>Trip date <span class="required">*</span></label>
                <input type="date" id="trip_date" name="date" class="field-divided" required>
            </li>
            <li>
                <input type="submit" value="Publish" />
            </li>
        </ul>
    </form>
</div>

</body>
</html>
