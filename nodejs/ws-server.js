'use strict';

let WSServer = require('ws').Server;
let wsServer = require('http').createServer();
let app = require('./http-server');
let encoder = new TextEncoder();
let port = process.env.EXPRESS_PORT | 8080;

// Create web socket wsServer on top of a regular http wsServer
let wss = new WSServer({
    server: wsServer
});
wss.binaryType = 'arraybuffer';

// Also mount the app here
wsServer.on('request', app);

wss.on('connection', function connection(ws) {
    ws.on('message', function incoming(message) {
        console.log("Received message");
        let txt = message.toString('utf8');
        console.log(`received: ${txt}`);

        let response = "Hello " + txt;
        let binaryResponse = encoder.encode(response);
        console.log("Sending response", binaryResponse);
        ws.send(binaryResponse);
    });
});

wsServer.listen(port, function () {
    console.log(`http/ws server listening on ${port}`);
});
