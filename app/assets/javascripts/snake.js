var ws = new WebSocket("ws://localhost:9000/ws");
var select = null;
var row = 16;
var col = 16;

ws.onmessage = function (evt) {
    var parsedData = JSON.parse(evt.data);
    switch(parsedData.t) {
        case "Players":
            console.log("Got players: " + evt.data);
            var i;
            for(i=select.options.length-1;i>=0;i--)
            {
                select.remove(i);
            }
            parsedData.everybody.forEach(function(item) {
                console.log(item);
                var option = document.createElement("option");
                option.text = item;
                select.add(option);
            });
            break;
        case "Invitation":
            ws.send(JSON.stringify({t: "Connect", player:parsedData.value}));
            break;
        case "Status":
            console.log("STATUS " + parsedData.value);
            break;
        case "Coordinates":
            cleanField();
            apple("snakeYardCell" + parsedData.food.y + "-" + parsedData.food.x);
            parsedData.snakes.forEach(function(snake) {
                snake.forEach(function(node) {
                    on("snakeYardCell" + node.y + "-" + node.x);
                });
            });
            break;
        default:
            console.log(parsedData);
    }
};

ws.onopen = function (evt) {
    console.log("CONNECTED");
};

ws.onclose = function (evt) {
    console.log("DISCONNECTED" + evt.code + evt.reason);
};

window.onbeforeunload = function () {
    ws.onclose = function () {};
    ws.close();
};

function stop() {
    ws.send(JSON.stringify({t: "Stop"}));
    cleanField();
}

function cleanField() {
    for ( i = 0; i < row; i++) {
        for (j = 0; j < col; j++) {
            off("snakeYardCell" + i + "-" + j);
        }
    }
}

function connectToSelectedUser() {
    ws.send(JSON.stringify({t: "CreateGame", player2:select.value}));
}

function findPlayers() {
    select = document.getElementById("playersSelect");
    console.log(select);
    ws.send(JSON.stringify({t: "FindPlayers"}));
}

function on(elementId) {
    changeBackground("#FF0000", elementId);
}

function off(elementId) {
    changeBackground("#bbbbbb", elementId);
}

function apple(elementId) {
    changeBackground("#9ACD32", elementId);
}

function changeBackground(newBackground, elementId) {
    var styleId = document.getElementById(elementId);
    if (styleId === null) {
        console.log(elementId);
    } else {
        styleId.style.background = newBackground;
    }
}

document.onkeydown = function(evt) {
    evt = evt || window.event;
    if (evt.keyCode == '38') {
        ws.send(JSON.stringify({t: "Direction", dir:"Down"}));
    } else if (evt.keyCode == '40') {
        ws.send(JSON.stringify({t: "Direction", dir:"Up"}));
    } else if (evt.keyCode == '37') {
        ws.send(JSON.stringify({t: "Direction", dir:"Left"}));
    } else if (evt.keyCode == '39') {
        ws.send(JSON.stringify({t: "Direction", dir:"Right"}));
    }
};