<%@ page import="us.irdev.bedrock.site.Service" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Test</title>
    <link rel="stylesheet" href="../dist/<%= Service.getBedrockVersion() %>/bedrock.css?1"/>
    <link rel="icon" type="image/png" href="../img/icon.png?1"/>
</head>

<body>
<h1>Combobox</h1>
<div class="page-container-div">
    <h2>Result</h2>
    <div class="section-content-div">
        <div>
            <div style="display: inline-block; margin-right: 5px;">The currently selected value of input 1 is:</div>
            <div id="test-output-1" style="color: blue; display: inline-block;"></div>
        </div>
        <div>
            <div style="display: inline-block; margin-right: 5px;">The currently selected value of input 2 is:</div>
            <div id="test-output-2" style="color: blue; display: inline-block;"></div>
        </div>
        <div>
            <div style="display: inline-block; margin-right: 5px;">The currently selected value of input 3 is:</div>
            <div id="test-output-3" style="color: blue; display: inline-block;"></div>
        </div>
    </div>
    <h2>Test</h2>
    <div class="section-content-div">
        <div><input type="text" class="combobox-input" id="test-input-1" placeholder="test 1" onchange="document.getElementById('test-output-1').innerHTML = test1.value;"></div>
        <div style="margin-bottom:5px;">And some text around it for testing.</div>
        <div>
            <input type="text" class="combobox-input" id="test-input-2" placeholder="test 2" onchange="document.getElementById('test-output-2').innerHTML = test2.value;">
            <div style="margin: 5px 0px;">text after the input</div>
        </div>
        <div style="margin-bottom:5px;">And some text around it for testing.</div>
        <div id="test-input-3-parent"></div>
        <div style="margin-bottom:5px;">And some text around it for testing.</div>
    </div>

</div>
</body>
</html>

<script src="../dist/<%= Service.getBedrockVersion() %>/bedrock-debug.js"></script>
<script>
    let options = ["a", "b", "c", "domineering", "profuse",
        "hollow", "caption", "save", "malicious", "marvelous", "mourn", "fabulous", "relieved",
        { value: "hidden", label: "Look" }, { label: "Abe Shinzou", value: "安倍 晋三", alt: "あべしんぞう" },
        "squealing", "secretive", "long-term", "plastic", "wipe", "Person", "unarmed", "calculate",
        "testy", "defective", "quickest", "beneficial", "blade", "gusty"];
    let test1 = Bedrock.ComboBox.new ({ inputElementId: "test-input-1", options: options, value: "360" });
    let test2 = Bedrock.ComboBox.new ({ inputElementId: "test-input-2", options: options });
    let test3 = Bedrock.ComboBox.new ({ inputElementId: "test-input-3", parentElementId: "test-input-3-parent", useRegExp: true, placeholder: "test 3", value: "24", options: options, onchange:function () { document.getElementById("test-output-3").innerHTML = test3.value; } });
</script>
