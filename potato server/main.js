// Importing the required modules
const WebSocketServer = require('ws');
const port = 6000;
const logger = false;
// Creating a new websocket server
const wss = new WebSocketServer.Server({ port: port });

const servers = new Map();
const userServer = new Map();
const fs = require('fs/promises');

let nextUserId = 0;

// Creating connection using websocket
wss.on("connection", (ws) => {
        const id = nextUserId;
        nextUserId++;
    ws.on("message", data => {
        let json = JSON.parse(data);
        if (json.packet == "hero") {
            const server = userServer.get(id);
            server.markers[json.heroId] = json.state;
            string = JSON.stringify(json);
            server.subs.forEach((client) => {
                if (client !== ws) {
                    client.send(string);
                }
            });
            server.edited = true;
        } else if (json.packet === "sub") {
            let server = servers.get(json.ip);

            if (server === undefined) {
                server = {
                    map: json.map,
                    seed: json.seed,
                    markers: new Array(92).fill(0),
                    subs: new Set(),
                    ip: json.ip,
                    edited: false,
                };
                servers.set(json.ip, server);
            } else if (server.seed !== json.seed) {
                server.ip = json.ip;
                server.seed = json.seed;
                server.map = json.map;
                server.markers.fill(0);
                server.edited = false;
            }
            userServer.set(id, server);
            server.subs.add(ws);

            let send = {
                packet: "subbed",
                map: server.map,
                markers: server.markers,
            };

            write(`sub server:${server.ip} seed:${json.seed} map:${json.map + 1} x:${json.x} y:${json.y}\n`);
            ws.send(JSON.stringify(send));
        } else if (json.packet == "unsub") {
            const server = userServer.get(id);
            if (server !== undefined && server.subs !== undefined) {
                server.subs.delete(ws);
                userServer.delete(id);
                if (server.edited && server.subs.size === 0) {
                    write(`dat server:${server.ip}map:${server.map + 1}seed:${server.seed}m:${server.markers}\n`);
                    server.edited = false;
                }
            }
        }
    });
    // handling what to do when clients disconnects from server
    ws.on("close", () => {
        const server = userServer.get(id);
        if (server !== undefined && server.subs !== undefined) {
            server.subs.delete(ws);
            userServer.delete(id);
        }
    });
    // handling client connection error
    ws.onerror = () => {
        console.log("Some Error occurred");
    }
});
console.log("The WebSocket server is running on port " + port);

const write = async (content) => {
    try {
        await fs.appendFile(__dirname + '/log.txt', content);
    } catch (err) {
        console.log(err);
    }
}