<%@include file="includes/header.jsp" %>
<h1>Bedrock</h1>
<div class="container-div">
    <h2>Admin Console</h2>

    <hr>
    <h3>Lock</h3>
    <div id="lock-form-container"></div>

</div>
<%@include file="includes/footer.jsp" %>

<script>
    const SECRET = Bedrock.Forms.SECRET;
    Bedrock.Forms.new ({
        name: "lock",
        div: "lock-form-container",
        inputs: [
            { name: SECRET, type: SECRET, label: "Secret:", required: true, placeholder: "xxxx1234" }
        ],
        onCompletion: function (form) {
            var responseFunc = function (response) {
                alert ((typeof (response) !== "undefined") ? JSON.stringify (response, null, 4) : "OK (with undefined response)");
                form.reset ();
            };
            Bedrock.ServiceBase.post (form.name, form.getValues (), responseFunc, responseFunc);
        }
    });
</script>
