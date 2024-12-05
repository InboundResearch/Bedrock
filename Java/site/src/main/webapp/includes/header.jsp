<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="us.irdev.bedrock.site.Service" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/img/icon.png?v=1"/>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/dist/<%= Service.getBedrockVersion() %>/bedrock.css"/>
    <meta name="viewport" content="width=920,initial-scale=1,user-scalable=yes"/>
    <title>Bedrock v.<%=Service.getBedrockVersion ()%></title>
</head>
<body>
