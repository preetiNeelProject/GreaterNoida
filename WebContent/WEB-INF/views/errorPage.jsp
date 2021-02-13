<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<html>
<head>
    <title>Home</title>
</head>
<body>
    <div>
    <center>
    <div style="    width: 100%;
    background: #0E334A;
    color: white;
    margin: 10px;
    padding: 10px;"><h2>${errorMsg}</h2></div>
    <div><img width="50%"
							src="${pageContext.request.contextPath}/staticResources/images/error.jpg"></div>
    <div> <a href="${pageContext.request.contextPath}/">Back to login page.</a></div>
    </div>
    </center>
</body>
</html>