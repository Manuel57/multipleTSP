var tours = tours.Tours;
var colors = ["red", "blue", "green", "orange"];


function draw() {

    var cvs = document.getElementById("cvs");
    var ctx = cvs.getContext("2d");

    var width = tours.width;
    var h = tours.numberOfPoints / width;


    for (var j = 0; j < h; j++) {
        for (var i = 0; i < width; i++) {
            ctx.beginPath();
            ctx.arc(800 / width * i + 800 / width / 2, 800 / h * j + 800 / h / 2, 10, 0, 2 * Math.PI);
            ctx.stroke();
        }
    }
}


function drawTour() {
    var width = tours.width;
    var h = tours.numberOfPoints / width;


    var arr = tours.Tours;

    for (var j = 0; j < arr.length; j++) {
        for (var i = 0; i < arr[j].length - 1; i++) {
            drawLine(800, arr[j][i], arr[j][i + 1], width, colors[j]);
        }
    }


}

function drawLine(totalWidth, pos, pos2, width, color) {
    var cvs = document.getElementById("cvs");
    var ctx = cvs.getContext("2d");
    var partW = totalWidth / width;
    var partH = 800 / (tours.numberOfPoints / tours.width);
    var multHeight1 = Math.floor(pos / width);
    var multHeight2 = Math.floor((pos2) / width);
    var multWidth1 = (pos) % width;
    var multWidth2 = (pos2) % width;

    ctx.beginPath();
    ctx.moveTo(partW * multWidth1 + partW / 2, partH * multHeight1 + partH / 2);
    //ctx.bezierCurveTo(partW * multWidth1 + partW / 2, partH * multHeight1 + partH /2 -20, partW * multWidth2 + partW / 2, partH * multHeight2 + partH / 2-20, partW * multWidth2 + partW / 2, partH * multHeight2 + partH / 2);
    ctx.lineTo(partW * multWidth2 + partW / 2, partH * multHeight2 + partH / 2);
    ctx.strokeStyle = color;
    ctx.stroke();
}