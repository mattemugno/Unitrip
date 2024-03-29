<%@ page import="dto.Trip" %>
<%@ page import="java.util.List" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileWriter" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="dto.TripList" %>
<%@ page import="java.nio.file.Files" %><%--
  Created by IntelliJ IDEA.
  User: pucci
  Date: 07/04/2022
  Time: 10:59
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <meta name="description" content="" />
    <meta name="author" content="" />
    <title>Favourites</title>
    <link href="<%=request.getContextPath()%>/resources/css/homepage.css" rel="stylesheet" />

</head>
<body>
<nav class="navbar navbar-expand-lg navbar-light bg-light">
    <div class="container px-4 px-lg-5">
        <div style="font-size:20pt" class="navbar-brand" ><%=request.getSession().getAttribute("username")%> | <a href="<%=request.getContextPath()%>/LoginServlet" style="font-size:20pt; text-decoration: none">Logout</a></div>
        <a style="margin-left:1em; font-size:25pt" class="navbar-brand" >Favourites Trips</a>
        <div style="font-size:20pt" class="navbar-brand" ><a href="<%=request.getContextPath()%>/HomepageServlet" style="font-size:20pt; text-decoration: none">Homepage</a></div>
    </div>
</nav>
<!-- Section-->
<section class="py-5">
    <div class="container px-4 px-lg-5 mt-5">
        <div class="row gx-4 gx-lg-5 row-cols-2 row-cols-md-3 row-cols-xl-4 justify-content-center">
            <%
                ArrayList<Trip> tripListFavs = (ArrayList<Trip>) request.getAttribute("tripListFavs");
                if(tripListFavs == null || tripListFavs.size() == 0){
            %>
            <h3>No trips added to favourites<h3>
                    <%
                        } else {
                            for(int i=0; i< tripListFavs.size(); i++){
                                Trip trip = tripListFavs.get(i);
                    %>
                <div style="width:30%; height:20%" class="col mb-5">
                    <div class="card h-100">
                        <div class="card-body p-4">
                            <div class="text-center">
                                <h1 class="fw-bolder"><%= trip.getDestination()%></h1>
                                <form method="post" action="<%=request.getContextPath()%>/FavoriteServlet">
                                    <input  type="hidden" name="trip_name" value="<%=trip.getTripName()%>">
                                    <input  type="hidden" name="username" value="<%=request.getSession().getAttribute("username")%>">
                                    <%if(!trip.getFavorites().contains(request.getSession().getAttribute("username").toString())){%>
                                    <input type="hidden" name="likeButton" value="likeButton">
                                    <input  style="width:50px; height: 50px" type="image" src="<%=request.getContextPath()%>/resources/images/notstar.png">
                                    <%} else{%>
                                    <input type="hidden" name="dislikeButton" value="dislikeButton">
                                    <input  style="width:50px; height: 50px" type="image" src="<%=request.getContextPath()%>/resources/images/star.png">
                                    <%}%>
                                </form>
                                <h3><%=trip.getDate()%></h3>
                                <h3>Founder: <%=trip.getFounder()%></h3>
                                <% if(trip.getParticipants()==null){%>
                                <h3>Booked Seats: 0/<%=trip.getSeats()%></h3>
                                <%} else{%>
                                <h3>Booked Seats: <span id="numSeats_<%=trip.getTripName()%>"><%=trip.getParticipants().size()%></span>/<span><%=trip.getSeats()%></span></h3>
                                <div class="dropdown">
                                    <button class="dropbtn" onclick='document.getElementById("myDropdown_<%=trip.getTripName()%>").classList.toggle("show");'>Participants</button>
                                    <div class="dropdown-content" id="myDropdown_<%=trip.getTripName()%>">
                                        <%if(trip.getParticipants().size()>0){%>
                                        <%for(String user : trip.getParticipants()){%>
                                        <span id="child_<%=user%>"><%=user%></span>
                                        <%}%>
                                        <%} else{%>
                                        <span>No participants at the moment</span>
                                        <%}%>
                                    </div>
                                </div>
                                <%}%>
                                <h3>Remaining time: <h3 id ="time_<%=trip.getTripName()%>"></h3></h3>
                                <script type="text/javascript">
                                    var x = setInterval(function() {

                                        var now = new Date().getTime();
                                        var timeleft = <%=trip.ExpirationDate(trip.getDate())%> - now;

                                        var days = Math.floor(timeleft / (1000 * 60 * 60 * 24));
                                        var hours = Math.floor((timeleft % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
                                        var minutes = Math.floor((timeleft % (1000 * 60 * 60)) / (1000 * 60));
                                        var seconds = Math.floor((timeleft % (1000 * 60)) / 1000);

                                        document.getElementById("time_<%=trip.getTripName()%>").innerHTML = days + "d " + hours + "h "
                                            + minutes + "m " + seconds + "s ";

                                        // If the count down is finished, write some text
                                        if (days < 0) {
                                            clearInterval(x);
                                            document.getElementById("time_<%=trip.getTripName()%>").innerHTML = "EXPIRED";
                                        }
                                    }, 1000)
                                </script>
                            </div>
                            <div class="text-center">
                                <%if(trip.getParticipants().size() < trip.getSeats()){%>
                                <script>var arg_<%=trip.getTripName()%> = '<%=trip.getTripName()%>';</script>
                                <%String nameTrip = trip.getTripName();%>
                                <h3 id="tripNome" type="hidden" style="display:none"><%=trip.getTripName()%></h3>
                                <form method="post" action="<%=request.getContextPath()%>/HomepageServlet">
                                    <input id="TripName" type="hidden" name="trip_name" value="<%=trip.getTripName()%>">
                                    <input id="userLogged" type="hidden" name="username" value="<%=request.getSession().getAttribute("username")%>">
                                    <% if((!trip.getParticipants().contains(request.getSession().getAttribute("username").toString())) && (!request.getSession().getAttribute("username").toString().contains("admin"))){%>
                                    <input id="join" onclick="sendAdd(arg_<%=trip.getTripName()%>)" style="font-size:17pt" name="joinButton" class="btn btn-outline-dark mt-auto" type="submit" value="JOIN TRIP">
                                    <%} else if((trip.getParticipants().contains(request.getSession().getAttribute("username").toString())) && (!request.getSession().getAttribute("username").toString().contains("admin"))){%>
                                    <input onclick="sendSub(arg_<%=trip.getTripName()%>)" style="font-size:17pt" name="leaveButton" class="btn btn-outline-dark mt-auto" type="submit" value="LEAVE TRIP">
                                    <%} else if(request.getSession().getAttribute("username").toString().contains("admin")){%>
                                    <input style="font-size:17pt" name="deleteButton" class="btn btn-outline-dark mt-auto" type="submit" value="DELETE TRIP">
                                    <%}%>
                                </form>
                                <%} else{%>
                                <%if(trip.getParticipants().contains(request.getSession().getAttribute("username").toString())){%>
                                <form method="post" action="<%=request.getContextPath()%>/HomepageServlet">
                                    <input type="hidden" name="trip_name" value="<%=trip.getTripName()%>">
                                    <input type="hidden" name="username" value="<%=request.getSession().getAttribute("username")%>">
                                    <input onclick="sendSub(arg_<%=trip.getTripName()%>)" style="font-size:17pt" name="leaveButton" class="btn btn-outline-dark mt-auto" type="submit" value="LEAVE TRIP">
                                </form>
                                <%} else{%>
                                <h3> No seats available</h3>
                                <%}%>
                                <form method="post" action="<%=request.getContextPath()%>/HomepageServlet">
                                    <input type="hidden" name="trip_name" value="<%=trip.getTripName()%>">
                                    <input type="hidden" name="username" value="<%=request.getSession().getAttribute("username")%>">
                                    <%if(request.getSession().getAttribute("username").toString().contains("admin")){%>
                                    <input style="font-size:17pt" name="deleteButton" class="btn btn-outline-dark mt-auto" type="submit" value="DELETE TRIP">
                                    <%}%>
                                </form>
                                <%}%>
                            </div>
                        </div>
                    </div>
                </div>
                    <%
                            }
                        }
                    %>
        </div>
    </div>
</section>
<!-- Footer-->
<footer class="py-5 bg-dark">
    <div class="container"><p class="m-0 text-center text-white">UniTrip</p></div>
</footer>
<!-- Bootstrap core JS-->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
