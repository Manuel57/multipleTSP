function start() {
    let form = document.forms.namedItem("formSettings");
    let rows = form["rows"].value;
    let cols = form["cols"].value;

    var cvs = document.getElementById("cvs");
    var ctx = cvs.getContext("2d");

    ctx.fillStyle = "white";
    ctx.fillRect(0, 0, 800 * rows, 800 * cols);

    getMatrix(parseInt(rows), parseInt(cols));
}

function getMatrix(rows, cols) {

    let tours = [[], []];
    console.log(rows);
    if (rows % 2 == 0 || cols % 2 == 0)
        return null;

    let m = Math.min(rows, cols);
    let n = rows + cols - m;


    for (let i = 0; i < n - 1; i++) {
        tours[0].push([i, i + 1]);
        i % 2 == 0 && i > 0 ? tours[0].push([map(n, 1, i), map(n, 1, i + 1)]) : 0;
    }

    console.log(tours);


    for (let idx = 0; idx < m - 2; idx++) {
        tours[1].push([map(n, idx, 0), map(n, idx + 1, 0)]);
        tours[0].push([map(n, idx, n - 1), map(n, idx + 1, n - 1)]);
        tours[0].push([map(n, idx + 1, n - 2), map(n, idx + 2, n - 2)]);
    }
    tours[1].push([map(n, m - 2, 0), map(n, m - 1, 0)]);
    tours[0].push([map(n, m - 2, n - 1), map(n, m - 1, n - 1)]);

    tours[0].push([map(n, m - 1, n - 2), map(n, m - 1, n - 1)]);


    tours[0].push([map(n, 0, 0), map(n, 1, 2)]);
    tours[1].push([map(n, 0, 0), map(n, 1, 1)]);

    calculateMovesSalespersonOne(m, n, tours);

    draw(m, n);
    drawTour(m, n, tours);

}

function map(n, row, col) {
    return (row) * n + col;
}


function calculateMovesSalespersonOne(m, n, tours) {
    let g = ((m * n) + 1) / 2;
    let b = g - (g % 2) - 1;

    let column = n - 3;
    let d = b - (n - 1) - (n - 3) - (2 * (m - 1)) - 1;

    // tours[0].push([map(n, 0, 0), map(n, 1, 2)]);

    let x;
    let leftMostColumn = [];
    let bottomMostRow = [];
    for (let i = 0; i < m; i++) {
        leftMostColumn.push(n - 2);
    }
    for (let i = 0; i < n; i++) {
        bottomMostRow.push(1);
    }
    console.log("-----------");
    while (d > 0) {
        console.log("2 -----------");
        x = 0;

        while (x < m - 2 && d > 0) {
            tours[0].push([map(n, x + 1, column), map(n, x + 2, column)]);
            tours[0].push([map(n, x + 1, column - 1), map(n, x + 2, column - 1)]);
            d -= 2;
            x++;
            console.log(x);
            leftMostColumn[x + 1] = column - 1;
        }
        bottomMostRow[column] = x + 1;
        bottomMostRow[column - 1] = x + 1;
        if (d > 0)
            tours[0].push([map(n, x + 1, column), map(n, x + 1, column - 1)]);
        column -= 2;
    }
    if (n !== 5 && m !== 5) {
        if (x % 2 == 0) {
            tours[0].push([map(n, x + 1, column + 2), map(n, x + 2, column + 2)]);
            tours[0].push([map(n, x + 1, column + 1), map(n, x + 2, column + 1)]);
            tours[0].push([map(n, x + 2, column + 1), map(n, x + 2, column + 2)]);
            leftMostColumn[x + 2] = column + 1;

            bottomMostRow[column + 1] = x + 2;
            bottomMostRow[column + 2] = x + 2;
        } else {
            tours[0].push([map(n, x + 1, column + 1), map(n, x + 1, column + 2)]);
            tours[0].push([map(n, 1, column), map(n, 2, column)]);
            tours[0].push([map(n, 1, column - 1), map(n, 2, column - 1)]);
            tours[0].push([map(n, 2, column), map(n, 2, column - 1)]);
            bottomMostRow[column] = 2;
            bottomMostRow[column - 1] = 2;

            column -= 2;
        }
    }
    console.log(column);
    console.log("leftmost col: ", leftMostColumn);
    console.log("bottommost row: ", bottomMostRow);

    while (column > 2) {
        tours[0].push([map(n, 1, column), map(n, 1, column - 1)]);
        column -= 2;
    }

    for (let i = 0; i < leftMostColumn[m - 1] - 1; i++) {
        tours[1].push([map(n, m - 1, i), map(n, m - 1, i + 1)]);
    }

    for (let i = 3; i < leftMostColumn[m - 1]; i++) {
        for (let j = m - 2; j > bottomMostRow[i] + 1; j--) {
            tours[1].push([map(n, j, i), map(n, j - 1, i)]);
        }
        if (i % 2 == 1) {
            tours[1].push([map(n, m - 2, i), map(n, m - 2, i - 1)]);
        } else {
            tours[1].push([map(n, bottomMostRow[i] + 1, i), map(n, bottomMostRow[i - 1] + 1, i - 1)]);

        }
    }
    tours[1].push([map(n, m - 1, leftMostColumn[m - 1] - 1), map(n, m - 2, leftMostColumn[m - 2] - 1)]);

    tours[1].push([map(n, 1, 1), map(n, 2, 2)]);

    for (let i = 2; i < m - 1; i++) {
        tours[1].push([map(n, i, 1), map(n, i, 2)]);
        tours[1].push([map(n, i, 1 + i % 2), map(n, i + 1, 1 + i % 2)]);
    }
    tours[1].pop();
}

var colors = ["red", "blue", "green", "orange"];


function draw(m, n) {

    var cvs = document.getElementById("cvs");
    var ctx = cvs.getContext("2d");

    var width = n;
    var h = (m * n) / width;


    for (var j = 0; j < h; j++) {
        for (var i = 0; i < width; i++) {
            ctx.beginPath();
            ctx.arc(800 / width * i + 800 / width / 2, 800 / h * j + 800 / h / 2, 10, 0, 2 * Math.PI);
            ctx.stroke();
        }
    }
}


function drawTour(m, n, tours) {
    var width = n;
    var h = (m * n) / width;


    for (var j = 0; j < tours.length; j++) {
        for (var i = 0; i < tours[j].length; i++) {
            drawLine(800, tours[j][i][0], tours[j][i][1], width, colors[j], m, n);
        }
    }


}

function drawLine(totalWidth, pos, pos2, width, color, m, n) {
    var cvs = document.getElementById("cvs");
    var ctx = cvs.getContext("2d");
    var partW = totalWidth / width;
    var partH = 800 / ((m * n) / n);
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


