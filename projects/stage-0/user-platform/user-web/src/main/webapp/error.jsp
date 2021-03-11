<head>
    <jsp:directive.include
            file="/WEB-INF/jsp/prelude/include-head-meta.jspf"/>
    <title>SUCCESS</title>
</head>

<body>

<div class="container-lg">
    出错了
    <br/>
    <dev>
        <%=response.getHeader("error")%>
    </dev>
    <br/>
</div>

<div class="container-lg">
    <input type="button" value="回首页" onclick="window.location.href = '/index.jsp'">
</div>

</body>