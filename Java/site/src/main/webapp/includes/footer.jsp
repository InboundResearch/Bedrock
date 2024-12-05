<%@ page import="us.irdev.bedrock.site.Service" %>
<div class="content-center footer">Built with <a class="footer-link" href="https://bedrock.irdev.us">Bedrock</a> v.<%= Service.getBedrockVersion() %></div>
</body>
</html>

<script type="module">
    // set up the global Bedrock object
    import Bedrock from "<%= request.getContextPath() %>/dist/<%= Service.getBedrockVersion() %>/bedrock.mjs";
    window.Bedrock = Bedrock;

    // set up the information we'll use in cookies
    Bedrock.Cookie.set ("context-path", "<%= request.getContextPath() %>/");
    Bedrock.Cookie.set ("full-context-path", location.origin + "<%= request.getContextPath() %>/");

    // a simple function to set the icon in the top right corner
    let addImageIcon = function () {

        // XXX this SHOULD be Bedrock code

        let h1s = Array.from (document.getElementsByTagName ("h1"));
        for (let h1 of h1s) {
            let div = document.createElement ("div");
            div.classList.add("header-wrapper");
            let root = h1.parentNode;
            root.insertBefore (div, h1);
            root.removeChild (h1);
            let internalDiv = document.createElement ("div");
            internalDiv.classList.add ("header-title");
            div.appendChild (internalDiv);
            internalDiv.appendChild (h1);

            let anchorDiv = document.createElement("div");
            anchorDiv.classList.add ("header-image");

            let anchorVersion = document.createElement("div");
            anchorVersion.innerHTML = "v.<%= Service.class.getPackage ().getImplementationVersion () %>";
            anchorVersion.classList.add ("header-version");
            anchorDiv.appendChild(anchorVersion);

            let a = document.createElement ("a");
            a.href = Bedrock.Cookie.get ("context-path");
            a.title = "Home";
            let img = document.createElement ("img");
            img.src = Bedrock.Cookie.get ("context-path") + "img/icon.png";
            img.height = 48;

            a.appendChild(img);
            anchorDiv.appendChild(a);
            div.appendChild (anchorDiv);
            return;
        }
        setTimeout (addImageIcon, 0.1);
    };

    setTimeout (addImageIcon, 0.1);
</script>
