/**
 * Created by Taurean Scantlebury on 14/03/14.
 * Property of vialab
 */

var currentUser; // stores the current user in the viz
var dur = 900; // time for the transitions
var portNum = 1278; // current port the program runs on from the server

String.prototype.parseURL = function () {return this.replace(/[A-Za-z]+:\/\/[A-Za-z0-9-_]+\.[A-Za-z0-9-_:%&~\?\/.=]+/g,function(url){
    return url.link(url);});};
String.prototype.parseUsername =function () {return this.replace(/[@]+[A-Za-z0-9-_]+/g,function(u){var username=u.replace("@","")
    return u.link("http://twitter.com/"+username);});};
String.prototype.parseHashtag =function () {return this.replace(/[#]+[A-Za-z0-9-_]+/g,function(t){var tag=t.replace("#","%23")
    return t.link("http://search.twitter.com/search?q="+tag);});};

function overlay() {
    el = document.getElementById("overlay");
    el.style.visibility = (el.style.visibility == "visible") ? "hidden" : "visible";
}

function error_overlay() {
    el = document.getElementById("error_overlay");
    el.style.visibility = (el.style.visibility == "visible") ? "hidden" : "visible";
}

function validateForm(){

    var username  = document.getElementById("twitterName").value;
    if (!username){
        d3.select("#error").selectAll("text").remove();
        d3.select("#error").append("text").style("opacity", 0.0).style("color", "red")
            .html( "Please enter a twitter name")
            .transition().duration(dur).style("opacity", 1.0);
    }
    else{
        var r2 = new RegExp('^@?(\\w{1,15})$', 'i');
        if (r2.test(username) == true){
            if (currentUser == username)
                alert("Already viewing user "+ currentUser);
            else{
                d3.select("#error").selectAll("text").remove();
                d3.select("#error").append("text").style("opacity", 0.0)
                    .html( "Loading ...")
                    .transition().duration(dur).style("opacity", 1.0);
                d3.select("#profilepic").classed("spinner", true).style("opacity", 0.0).transition().duration(dur)
                    .style("opacity", 0.9);
                d3.select("#contentcolumn").classed("loading", true);
                d3.select("#leftcolumn").classed("loading", true);
                currentUser = username;
                populate(username); // starts D3 function
            }
        }
        else{
            d3.select("#error").selectAll("text").remove();
            d3.select("#error").append("text").style("opacity", 0.0).style("color", "red")
                .html( "Not a valid twitter name")
                .transition().duration(dur).style("opacity", 1.0);
        }

    }
}

function populate(username){

    // removes all previous drawn d3 elements
    d3.select("#profilepic").selectAll("img").remove();
    d3.select("#info").selectAll("text").remove();
    d3.select("#time").selectAll("text").remove();
    d3.select("#contentcolumn").selectAll("svg").remove();
    d3.select("#pie").selectAll("svg").remove();

    //dimensions of main graph
    var margin = {top: 10, right: 10, bottom: 100, left: 40},
        margin2 = {top: 430, right: 10, bottom: 20, left: 40},
        width = 1000 - margin.left - margin.right,
        height = 500 - margin.top - margin.bottom,
        height2 = 500 - margin2.top - margin2.bottom;

    //parse the date of the tweets [ "created_at":"Sat Dec 21 09:24:14 +0000 2013" ]
    var parseDate = d3.time.format("%a %b %d %H:%M:%S %Z %Y").parse;
    var formatTime = d3.time.format("%A %e %B %Y %I:%M  %p");
    var avgTime = d3.time.format("%H");
    var joined = d3.time.format("%A %e %B %Y");
    var time = 0,
        tPos = 0,
        tNeg = 0,
        tAng = 0,
        tAnti = 0,
        tDis = 0,
        tFear = 0,
        tJoy = 0,
        tSad = 0,
        tSur = 0,
        tTru = 0,
        minR = 0,
        maxR = 3.5,
        minP = 3.5,
        maxP = 6,
        originalclicked = false,
        retweetclicked = false;

    //sets the ranges
    var x = d3.time.scale().range([0, width-10]), //added 10
        x2 = d3.time.scale().range([0, width-10]), // added 10
        y = d3.scale.linear().range([height, 5]),// originally 0, changed to 5
        y2 = d3.scale.linear().range([height2, 0]);

    //defines the axes
    var xAxis = d3.svg.axis().scale(x).orient("bottom").ticks(5),
        xAxis2 = d3.svg.axis().scale(x2).orient("bottom"),
        yAxis = d3.svg.axis().scale(y).orient("left").ticks(8);

    var brush = d3.svg.brush()
        .x(x2)
        .on("brush", brushed);

    // working one
    var area2 = d3.svg.area()
        .interpolate("linear")
        .x(function(d) { return x2(d.date); })
        .y0(height2)
        .y1(function(d) { return y2(d.totalScore); });
    
    var area = d3.svg.area()
        .interpolate("basis")
        .x(function(d) { return x2(d.date); })
        .y0(function(d) { return -d.totalScore / 2; })
        .y1(function(d) { return d.totalScore / 2; });

    var line = d3.svg.line()
        .interpolate("linear")
        .x(function(d){return x(d.date);})
        .y(function(d){return x(d.totalScore);})

    var svg = d3.select("#contentcolumn").append("svg:svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom);

    var profile = d3.select("#profilepic").append("img")
        .attr("class", "circle")
        .attr("width", 100)
        .attr("height", 100)
        .style("opacity",0.0);

    svg.append("defs").append("clipPath")
        .attr("id", "clip")
        .append("rect")
        .attr("width", width)
        .attr("height", height);

    var focus = svg.append("g")
        .attr("class", "focus")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var focus2 = svg.append("g")
        .attr("id", "focus2")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var context = svg.append("g")
        .attr("class", "context")
        .attr("transform", "translate(" + margin2.left + "," + margin2.top + ")");

    var div = d3.select("#contentcolumn").select("#texttip")
        .attr("class", "tooltip")
        .style("opacity", 0);


    var link = "http://vialab.science.uoit.ca:"+portNum+"/twitter/"+username;
     
    d3.json(link, function(error, data) {
        if (error){
            console.warn(error);
            d3.select("#error").selectAll("text").remove();
            d3.select("#error").append("text").style("opacity", 0.0).style("color", "red")
                .html( "Error Occurred. Try Again.")
                .transition().duration(dur).style("opacity", 1.0);
            error_overlay(); // displays an error to the screen
            d3.select("#profilepic").classed("spinner", false);
            d3.select("#contentcolumn").classed("loading", false);
            d3.select("#leftcolumn").classed("loading", false);
            currentUser = "";
        }

        data.forEach(function (d) {
            d.date = parseDate(d.created_at);
            var negative = d.score_negative + d.score_anger + d.score_disgust + d.score_fear + d.score_sadness;
            var postive = d.score_positive + d.score_anticipation + d.score_joy +  d.score_trust + d.score_surprise;
            d.totalScore = postive - negative;
            time += parseInt(avgTime(d.date));
            if(checkRT(d.text)==true)
                d.type = "original";
            else
                d.type = "retweet";
        });

        d3.select("#error").selectAll("text").remove(); // removes the loading indication
        d3.select("#profilepic").classed("spinner", false); // stops the spinning square animation
        d3.select("#contentcolumn").classed("loading", false);
        d3.select("#leftcolumn").classed("loading", false);

        x.domain(d3.extent(data.map(function(d) { return d.date; })));	// set the x domain so be as wide as the range of dates we have.
        y.domain(d3.extent(data.map(function(d) { return d.totalScore; })));	// set the y domain to go from 0 to the maximum value of d.close
        x2.domain(x.domain());
        y2.domain(y.domain());

        // User's Screen name and tweet number
        d3.select("#error").append("text").style("opacity", 0.0)
            .html(userName(data[0].user.screen_name) + "<br>Name: "+ data[0].user.name + "<br>Retrieved "+ data.length+
                " tweets" )
            .transition().duration(dur).style("opacity", 1.0);

        //Profile Pic
        profile.attr("src", data[0].user.profile_image_url_https)
            .transition().duration(dur)
            .style("opacity", 1.0);

        d3.select("#info").append("text").style("opacity", 0.0)
            .html( "<br>Joined Twitter: "+ joined(parseDate(data[0].user.created_at))+"</br>Location: "+ data[0].user.location+
                "</br> Current Followers: "+ data[0].user.followers_count)
            .transition().duration(dur).style("opacity", 1.0);

        // Average time of tweets
        d3.select("#time")
            .append("text")
            .append("h3")
            .style("opacity", 0)
            .html(calAvgTime(parseInt(time/data.length)))
            .transition().duration(dur).style("opacity", 1.0);
			
        // Add the X Axis
        focus.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(xAxis);

        // Add the Y Axis
        focus.append("g")
            .attr("class", "y axis")
            .call(yAxis);

        focus.append("text")
            .attr("text-anchor", "end")
            .attr("y", 6)
            .attr("dy", ".75em")
            .attr("transform", "rotate(-90)")
            .style("opacity", 0.69)
            .text("Sentiment Score");

        focus2.append("text")
            .attr("text-anchor", "end")
            .attr("x", 170).attr("y", 415) //y 427
            .attr("dy", ".75em")
            .text("Slide and select time periods:")
            .order();

         // working one
        context.append("path")
            .datum(data)
            //.attr("class", "area")
            .attr("fill", "steelblue") // this is new and needed
            .attr("d", area2);

        context.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height2 + ")")
            .call(xAxis2);

        context.append("g")
            .attr("class", "x brush")
            .call(brush)
            .selectAll("rect")
            .attr("y", -6)
            .attr("height", height2 + 7);

     
        var tweets = focus2.append("g");
        tweets.attr("clip-path", "url(#clip)");
        tweets.selectAll(".dot")
            .data(data)
            .enter().append("circle")
            .attr("class", "dot")
             //.attr("class", function (d){return moreInfo(d.text);})
            .attr("r", 3.5)
            .attr("cx", function(d) { return x(d.date); })
            .attr("cy", function(d) { return y(d.totalScore);})
            .style("opacity", 0.55)
            .style("fill", function(d){  //checks for (RT:, "@ or RT @) returns red if true, steelblue if false
                if(checkRT(d.text)==true)
                    return "#8F2831";
                else
                    return "#4682B4";
            })
            .attr("class", function (d){
                var string = "";
                if(d.score_positive != 0){
                    string += "positive ";
                    tPos++;
                }
                if(d.score_negative != 0){
                    string += "negative ";
                    tNeg++;
                }
                if(d.score_anger != 0){
                    string += "anger ";
                    tAng++;
                }
                if(d.score_anticipation != 0){
                    string += "anticipation ";
                    tAnti++;
                }
                if(d.score_disgust != 0){
                    string += "disgust ";
                    tDis++;
                }
                if(d.score_fear != 0){
                    string += "fear ";
                    tFear++;
                }
                if(d.score_joy != 0){
                    string += "joy ";
                    tJoy++;
                }
                if(d.score_sadness != 0){
                    string += "sadness ";
                    tSad++;
                }
                if(d.score_surprise != 0){
                    string += "surprise ";
                    tSur++;
                }
                if(d.score_trust != 0){
                    string += "trust ";
                    tTru++;
                }
                if(d.type == "original"){
                    string += "original ";
                }
                if(d.type == "retweet"){
                    string += "retweet ";
                }

                return string;

            })
            .on("mouseover", function(d) {
                div.transition()
                    .duration(500)
                    .style("opacity",0.9);
                d3.select(this).attr('r', maxP).transition().duration(600).style("opacity",1.0).style("cursor", "pointer");
                div.html(formatTime(d.date)+ "</br></br>" + "Positive: "+ d.score_positive + " Negative: "+ d.score_negative +
                        " Anger: " +d.score_anger + " Anticipation: "+d.score_anticipation + " Disgust: " +d.score_disgust +
                        "</br> Fear: "+ d.score_fear + " Joy: " +d.score_joy + " Sadness: "+ d.score_sadness + " Surprise: "+
                        d.score_surprise + " Trust: "+d.score_trust+"</br> Total Score: "+ d.totalScore+ "</br></br>" +
                        (d.text).parseURL().parseUsername().parseHashtag())
                    .style("left", (d3.event.pageX) + "px")
                    .style("top", (d3.event.pageY - 28) + "px");
            })
            .on("mouseout", function(d) {
                div.transition()
                    .duration(500)
                    .style("opacity", 0);
                d3.select(this).transition().duration(500)
                    .attr('r', minP)
                    .style("opacity", 0.55);
            });

        var totalPos = tAnti + tJoy + tPos + tSur + tTru;
        var totalNeg = tAng + tDis + tNeg + tSad +  tFear;
        var totalSen = totalPos + totalNeg;

        var values = {
            "name": "Overall",
            "children": [{
                "label": "Positive Overall",
                "value": totalPos,
                "class": ".positive .anticipation .joy .surprise .trust ",
                "name": "Positive Overall",
                "color": "#004c00",
                "per": ((totalPos/totalSen)*100).toFixed(2),
                "clicked": false,
                "children": [
                    {
                        "label": "Anticipation",
                        "value": tAnti,
                        "class": ".anticipation",
                        "name": "Anticipation",
                        "color": "#bdf134",
                        "per": ((tAnti/totalSen)*100).toFixed(2),
                        "clicked": false
                    },
                    {
                        "label": "Joy",
                        "value": tJoy,
                        "class": ".joy",
                        "name": "Joy",
                        "color": "#ff8c00",
                        "per": ((tJoy/totalSen)*100).toFixed(2),
                        "clicked": false
                    },
                    {
                        "label": "Surprise",
                        "value": tSur,
                        "class": ".surprise",
                        "name": "Surprise",
                        "color": "#CCFF99",
                        "per": ((tSur/totalSen)*100).toFixed(2),
                        "clicked": false
                    },
                    {
                        "label": "Trust",
                        "value": tTru,
                        "class": ".trust",
                        "name": "Trust",
                        "color": "#567A84",
                        "per": ((tTru/totalSen)*100).toFixed(2),
                        "clicked": false
                    },
                    {
                        "label": "Positive",
                        "value": tPos,
                        "class": ".positive",
                        "name": "Positive",
                        "color": "#004c00",
                        "per": ((tPos/totalSen)*100).toFixed(2),
                        "clicked": false
                    }]
            },
                {
                    "label": "Negative Overall",
                    "value": totalNeg,
                    "class": ".negative .anger .disgust .fear .sadness ",
                    "name": "Negative Overall",
                    "color": "#4c0000",
                    "per": ((totalNeg/totalSen)*100).toFixed(2),
                    "clicked": false,
                    "children": [
                        {
                            "label": "Anger",
                            "value": tAng,
                            "class": ".anger",
                            "name": "Anger",
                            "color": "#CC0000",
                            "per":((tAng/totalSen)*100).toFixed(2),
                            "clicked": false
                        },
                        {
                            "label": "Disgust",
                            "value": tDis,
                            "class": ".disgust",
                            "name": "Disgust",
                            "color": "#FFCC99",
                            "per": ((tDis/totalSen)*100).toFixed(2),
                            "clicked": false
                        },
                        {
                            "label": "Fear",
                            "value": tFear,
                            "class": ".fear",
                            "name": "Fear",
                            "color": "#CCCCCC",
                            "per": ((tFear/totalSen)*100).toFixed(2),
                            "clicked": false
                        },
                        {
                            "label": "Sadness",
                            "value": tSad,
                            "class": ".sadness",
                            "name": "Sadness",
                            "color": "#976370",
                            "per": ((tSad/totalSen)*100).toFixed(2),
                            "clicked": false
                        },
                        {
                            "label": "Negative",
                            "value": tNeg,
                            "class": ".negative",
                            "name": "Negative",
                            "color": "#4c0000",
                            "per": ((tNeg/totalSen)*100).toFixed(2),
                            "clicked": false
                        }]
                }]
        };

        //pie chart
        var m = 100,
            r = 100,
            pieH = 210,
            pieW = 210,
            radius = Math.min(pieW, pieH)/2;

        var pieD = d3.select("#pie").append("svg")
            .attr("width", pieW)
            .attr("height", pieH)
            .append("g")
            .attr("transform", "translate(" + pieW/2 + "," + pieH/2 + ")");

        pieD.append("text")
            .attr("dy", "-20")
            .style("text-anchor", "middle")
            .style("opacity", 0)
            .text("Original")
            .attr("class","originalSelected")
            .on("click", function(){

                if(originalclicked == false){

                    d3.select("#contentcolumn").select("svg").selectAll("circle:not(.original)").transition()
                        .duration(500).attr("r", minR);
                    d3.select(this).classed("selected", true);
                    originalclicked = true;
                }
                else if(originalclicked == true){

                    d3.select("#contentcolumn").select("svg").selectAll("circle:not(.original)").transition()
                        .duration(500).attr("r", maxR);
                    d3.select(this).classed("selected", false);
                    originalclicked = false;
                }
            })
            .transition().duration(dur).style("opacity",1.0);

        pieD.append("text")
            .attr("dy", "20")
            .style("text-anchor", "middle")
            .style("opacity", 0)
            .text("Re-Tweets")
            .on("click", function(){

                if(retweetclicked == false){

                    d3.select("#contentcolumn").select("svg").selectAll("circle:not(.retweet)").transition()
                        .duration(500).attr("r", minR);
                    d3.select(this).classed("selected", true);
                    retweetclicked = true;
                }
                else if(retweetclicked == true){

                    d3.select("#contentcolumn").select("svg").selectAll("circle:not(.retweet)").transition()
                        .duration(500).attr("r", maxR);
                    d3.select(this).classed("selected", false);
                    retweetclicked = false;
                }
            })
            .transition().duration(dur).style("opacity",1.0);

        var partition = d3.layout.partition()
            .sort(null)
            .size([2 * Math.PI, radius * radius]) // 2 * Math.PI
            .value(function(d) { return d.value; });

        var arc = d3.svg.arc()
            .startAngle(function(d) { return d.x; })
            .endAngle(function(d) { return d.x + d.dx; })
            .innerRadius(function(d) { return Math.sqrt(d.y)*1.01; })
            .outerRadius(function(d) { return Math.sqrt(d.y + d.dy)*1.01; });

        var pieChart = pieD.datum(values).selectAll("path")
            .data(partition.nodes)
            .enter().append("path")
            .attr("display", function(d) { return d.depth ? null : "none"; }) // hide inner ring
            .attr("d", arc)
            .attr("stroke", "white")
            .attr("stroke-width", 0.7)
            .style("fill", function(d) { return d.color; })
            .style("opacity", 0)
            .on("mouseenter", function(d) {

                d3.select(this).transition().duration(500).style("opacity",1.0);
                div.transition()
                    .duration(500)
                    .style("opacity",0.9);
                div.html(d.label+": " +d.per+"%")
                    .style("left", (d3.event.pageX) + "px")
                    .style("top", (d3.event.pageY - 28) + "px");
            })
            .on("mouseover", function(d) {

                d3.select(this).transition().duration(500).style("opacity",1.0);
                div.transition()
                    .duration(500)
                    .style("opacity",0.9);
                div.html(d.label+": " +d.per+"%")
                    .style("left", (d3.event.pageX) + "px")
                    .style("top", (d3.event.pageY - 28) + "px");
            })
            .on("mouseout", function(d) {
                div.transition()
                    .duration(500)
                    .style("opacity", 0);
                d3.select(this).transition().duration(500).style("opacity",0.6);
            })
            .on("click", function(d){

                if(d.name == "Positive Overall")
                {
                    if(d.clicked == false ){

                        d3.select(this).style("fill", "#7ec0ee"); //'skyblue'
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.positive)").transition()
                            .duration(500).attr("r", minR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.anticipation)").transition()
                            .duration(500).attr("r", minR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.joy)").transition()
                            .duration(500).attr("r", minR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.surprise)").transition()
                            .duration(500).attr("r", minR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.trust)").transition()
                            .duration(500).attr("r", minR);
                        d.clicked = true;

                        //console.log(d.name +" "+ d.clicked);
                    }
                    else if(d.clicked == true){

                        d3.select(this).style("fill", d.color);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.positive)").transition()
                            .duration(500).attr("r", maxR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.anticipation)").transition()
                            .duration(500).attr("r", maxR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.joy)").transition()
                            .duration(500).attr("r", maxR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.surprise)").transition()
                            .duration(500).attr("r", maxR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.trust)").transition()
                            .duration(500).attr("r", maxR);
                        d.clicked = false;
                        //console.log(d.name +" "+ d.clicked);

                        if(originalclicked == true){
                            d3.select("#contentcolumn").select("svg").selectAll("circle:not(.original)").transition()
                                .duration(500).attr("r", minR);
                        }
                        if(retweetclicked == true){
                            d3.select("#contentcolumn").select("svg").selectAll("circle:not(.retweet)").transition()
                                .duration(500).attr("r", minR);
                        }
                    }
                }
                else if(d.name == "Negative Overall")
                {
                    if(d.clicked == false ){

                        d3.select(this).style("fill", "#7ec0ee"); //'skyblue'
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.negative)").transition()
                            .duration(500).attr("r", minR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.anger)").transition()
                            .duration(500).attr("r", minR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.disgust)").transition()
                            .duration(500).attr("r", minR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.fear)").transition()
                            .duration(500).attr("r", minR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.sadness)").transition()
                            .duration(500).attr("r", minR);
                        d.clicked = true;

                        //console.log(d.name +" "+ d.clicked);
                    }
                    else if(d.clicked == true){

                        d3.select(this).style("fill", d.color);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.negative)").transition()
                            .duration(500).attr("r", maxR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.anger)").transition()
                            .duration(500).attr("r", maxR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.disgust)").transition()
                            .duration(500).attr("r", maxR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.fear)").transition()
                            .duration(500).attr("r", maxR);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not(.sadness)").transition()
                            .duration(500).attr("r", maxR);
                        d.clicked = false;
                        //console.log(d.name +" "+ d.clicked);

                        if(originalclicked == true){
                            d3.select("#contentcolumn").select("svg").selectAll("circle:not(.original)").transition()
                                .duration(500).attr("r", minR);
                        }
                        if(retweetclicked == true){
                            d3.select("#contentcolumn").select("svg").selectAll("circle:not(.retweet)").transition()
                                .duration(500).attr("r", minR);
                        }
                    }
                }
                else
                {
                    if(d.clicked == false ){

                        d3.select(this).style("fill", "#7ec0ee"); //'skyblue'
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not("+ d.class+")").transition()
                            .duration(500).attr("r", minR);
                        d.clicked = true;
                        //d3.select(this).classed("action", true);
                        //console.log(d.name +" "+ d.clicked);
                    }
                    else if(d.clicked == true){

                        d3.select(this).style("fill", d.color);
                        d3.select("#contentcolumn").select("svg").selectAll("circle:not("+ d.class+")").transition()
                            .duration(500).attr("r", maxR);
                        d.clicked = false;
                        //console.log(d.name +" "+ d.clicked);
                        //d3.select(this).classed("action", false);
                        if(originalclicked == true){
                                d3.select("#contentcolumn").select("svg").selectAll("circle:not(.original)").transition()
                                    .duration(500).attr("r", minR);
                        }
                        if(retweetclicked == true){
                            d3.select("#contentcolumn").select("svg").selectAll("circle:not(.retweet)").transition()
                                .duration(500).attr("r", minR);
                        }
                    }
                }
            })
            .transition().duration(dur).style("opacity", 0.6);
    });

    function brushed() {
        x.domain(brush.empty() ? x2.domain() : brush.extent());
        focus.select(".x.axis").call(xAxis);
        var tweets = focus2.selectAll("circle")
            .attr("cx", function(d) { return x(d.date); })
            .attr("cy", function(d) { return y(d.totalScore);});
    }

    //Defined Functions
    function calAvgTime(fTime){

        if(fTime >= 00 && fTime <=03)
            return "Midnight Tweeter </br>12am - 3am";

        if(fTime >= 04 && fTime <=08)
            return "Early Morning Tweeter<br/>4am - 8am";

        if(fTime >= 09 && fTime <=12)
            return "Morning Tweeter<br/>9am - 12pm";

        if(fTime >= 13 && fTime <=17)
            return "Afternoon Tweeter<br/>1pm - 5pm";

        if(fTime >= 18 &&fTime <=20)
            return "Evening Tweeter<br/>6pm - 8pm";

        if(fTime >= 21 && fTime <=23)
            return "Night Tweeter<br/>9pm - 11pm";
    }

    function checkRT(input_string){

        var rt = /^((RT @)|(RT:)|("@)).*/g;
        if(rt.test(input_string) == true)
            return true;
    }

    function userName(uname){
        return "User: <a href=http://www.twitter.com/"+uname+" target=_blank>"+uname+ "</a>";
    }

}