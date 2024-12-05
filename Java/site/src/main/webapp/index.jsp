<%@include file="includes/header.jsp" %>
<h1>Bedrock</h1>
<div class="container-div">
    <h2>About</h2>
    <div class="description-div">
        <p>Bedrock is a tool for quickly building lightweight micro-services in Java using JSON for communications. Bedrock services are designed to run in a Tomcat application server using POST queries for TLS security.</p>
    </div>

    <h2>Developer</h2>
    <div class="description-div">
        <ul>
            <li><a href="interface.jsp">Test Interface</a> - an example of the bedrock service and the service descriptor. </li>
            <li>Find the sources on Github @ <a href="https://github.com/InboundResearch/Bedrock">InboundResearch/Bedrock</a>.</li>
        </ul>
    </div>

    <h2>Documentation</h2>
    <div class="description-div">
        <h3>Java</h3>
        <ul>
            <li><a href="<%= request.getContextPath() %>/dist/<%= Service.getBedrockVersion() %>/apidocs/index.html">JavaDocs</a></li>
        </ul>

        <h3>Javascript</h3>
        <ul>
            <li><a href="<%= request.getContextPath() %>/dist/<%= Service.getBedrockVersion() %>/docs/bedrock/">Bedrock</a></li>
        </ul>

        <p style="margin: 12px 0;">USAGE:</p>
        <pre class="code-pre">&lt;link rel="stylesheet" href="https://bedrock.irdev.us/dist/<%=Service.getBedrockVersion ()%>/bedrock.css"/>

&lt;script type="module">
    import Bedrock from "https://bedrock.irdev.us/dist/<%=Service.getBedrockVersion ()%>/bedrock.mjs";

    // save the imported object to a global context for use in other script nodes
    window.Bedrock = Bedrock;
&lt;/script>

&lt;script type="module">
    const Bedrock = window.Bedrock;
    ...
    Bedrock.ServiceBase. ...
&lt;/script></pre>
    </div>

    <h2>Examples</h2>
    <div class="description-div">
        <ul>
            <li><a href="<%= request.getContextPath() %>/examples/combobox.jsp">ComboBox</a></li>
            <li><a href="<%= request.getContextPath() %>/examples/database">Database</a></li>
            <li><a href="<%= request.getContextPath() %>/examples/forms.jsp">Forms</a></li>
            <li><a href="<%= request.getContextPath() %>/examples/html.jsp">HTML</a></li>
            <li><a href="<%= request.getContextPath() %>/examples/http.jsp">HTTP</a></li>
        </ul>
    </div>

    <h2>Admin</h2>
    <div class="description-div">
        <ul>
            <li><a href="<%= request.getContextPath() %>/log.jsp">Log</a></li>
            <li><a href="<%= request.getContextPath() %>/lock.jsp">Lock</a></li>
        </ul>
    </div>
</div>
<%@include file="includes/footer.jsp" %>
