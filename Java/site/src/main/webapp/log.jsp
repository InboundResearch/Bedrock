<%@include file="/includes/header.jsp" %>
<h1>Bedrock</h1>
<div class="container-div">
    <h2>Server Log</h2>
    <hr>
    <h3>Entries</h3>
    <div style="margin-left:15px; margin-bottom: 10px;">
        <div id="bedrock-database-container"></div>
        <div id="bedrock-database-display"></div>
    </div>
    <hr>
    <h3>Detail</h3>
    <div class="container-div" style="margin-left:15px; margin-bottom: 10px;"><pre id="response-container"></pre></div>
</div>
<%@include file="/includes/footer.jsp" %>

<style>
    div[id="bedrock-database-container"] {
        display: inline-block;
        font-family: sans-serif;
        text-align: left;
        padding: 4px 0px;
        width: 100%;
        padding-top: 0;
    }

    div[id="bedrock-database-display"] {
        height: 500px;
        border: solid;
        border-width: 1px;
        border-radius: 2px;
        border-color: #bbb;
    }

    div[id="bedrock-record-display"] {
        margin-top: 5px;
        font-size: 12px;
    }

    .bedrock-combobox-paged-display-hover {
        color: white;
    }

    .bedrock-paged-display-hover {
        color: white;
        background-color: #aaa;
    }
</style>
<script type="module">
    const Bedrock = window.Bedrock;

    const LOG_FILE = "log-file";
    const TIMESTAMP = "timestamp";
    const DATE = "date";
    const TIME = "time";

    let convertTimestampRecords = function (records) {
        let pad = function (num, digits) {
            return num.toString ().padStart (digits, '0');
        };

        for (let record of records) {
            if (TIMESTAMP in record) {
                // example: 2024-12-05 19:27:48.119
                const dateTimeString = record[TIMESTAMP].replace(" ", "T");
                let date = new Date (dateTimeString);
                record[DATE] = date.getFullYear() + "-" + pad (date.getMonth() + 1, 2) + "-" +  pad (date.getDate(), 2);
                record[TIME] = pad (date.getHours(), 2) + ":" + pad (date.getMinutes(), 2) + ":" + pad (date.getSeconds(), 2) + "." + pad (date.getMilliseconds(), 3);
                record[TIMESTAMP] = date.getTime();
            }
        }
    };

    Bedrock.ServiceBase.post (LOG_FILE, { "line-count" : 100 }, function (records) {
        convertTimestampRecords(records);

        // sort the records as an example
        let CF = Bedrock.CompareFunctions;
        records = Bedrock.DatabaseOperations.Sort.new ({
            fields: [
                { name: TIMESTAMP, descending: true, type: CF.NUMERIC }
            ]
        }).perform (records);

        // build the database filter
        Bedrock.Database.Container.new ({
            database: records,
            filterValues: [{ field: "level" }],
            onUpdate: function (db) {
                Bedrock.PagedDisplay.Table.new ({
                    container: "bedrock-database-display",
                    records: db,
                    select: [
                        { name: DATE, displayName: "Date", width: 0.075 },
                        { name: TIME, displayName: "Time", width: 0.085 },
                        { name: "level", displayName: "Level", width: 0.0625 },
                        { name: "method", displayName: "Method", width: 0.35 },
                        { name: "message", displayName: "Message", width: 0.4 }
                    ],
                    onclick: function (record) {
                        let jsonLines = JSON.stringify(record, null, 4).split ("\n");
                        let newJsonLines = [];
                        for (let jsonLine of jsonLines) {
                            //     "message": "configuration path: /Users/brettonw/bin/apache-tomcat-9.0.33/webapps/tsg-mtds-
                            const maxLineLength = 90;
                            if (jsonLine.length > maxLineLength) {
                                jsonLine = jsonLine.split(' ').map((value, index, array) => {
                                    if (!array.currentLineLength) {array.currentLineLength = 0}
                                    array.currentLineLength += value.length+1;
                                    if (array.currentLineLength > maxLineLength) {
                                        array.currentLineLength = value.length;
                                        return "\n" + value;
                                    }
                                    return value;
                                }).join(' ');
                            }
                            newJsonLines.push (jsonLine);
                        }
                        jsonLines = newJsonLines.join ("\n");
                        document.getElementById("response-container").innerHTML = jsonLines;
                        return true;
                    }
                }).makeTableWithHeader ();
            }
        });
    });
</script>
