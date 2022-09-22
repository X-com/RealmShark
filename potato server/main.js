// Importing the required modules
const WebSocketServer = require('ws');
const port = 6000
const logger = true
// Creating a new websocket server
const wss = new WebSocketServer.Server({ port: port })

const servers = new Map()
const userServer = new Map()

// Creating connection using websocket
wss.on("connection", (ws) => {
        const id = uuidv4();
        if (logger) console.log("new client connected " + id);
    ws.on("message", data => {
        let json = JSON.parse(data)
        if (json.packet == "hero") {
            const server = userServer.get(id)
            server.markers[json.heroId] = json.state
            string = JSON.stringify(json)
            server.subs.forEach((client) => {
                if (client != ws) {
                    client.send(string)
                }
            });
            if (logger) console.log("hero " + id)
        } else if (json.packet == "sub") {
            let server = servers.get(json.ip);

            if (server == null) {
                server = new Server()
                server.map = json.map
                server.seed = json.seed
                server.markers = new Array(92).fill(0)
                server.subs = new Set()
                servers.set(json.ip, server)
            } else if (server.seed != json.seed) {
                server.seed = json.seed
                server.map = json.map
                server.markers.fill(0)
            }
            userServer.set(id, server)
            server.subs.add(ws)

            let send = new Sender()
            send.packet = "subbed"
            send.map = server.map
            send.markers = server.markers

            string = JSON.stringify(send)
            ws.send(string)
            if (logger) console.log("sub " + id)
        } else if (json.packet == "unsub") {
            const server = userServer.get(id)
            if (server != null && server.subs != null) {
                server.subs.delete(ws)
                userServer.delete(id)
                if (logger) console.log("unsub " + id)
            }
        }
    });
    // handling what to do when clients disconnects from server
    ws.on("close", () => {
        const server = userServer.get(id)
        if (server != null && server.subs != null) {
            server.subs.delete(ws)
            userServer.delete(id)
        }
        if (logger) console.log("client disconnected " + id);
    });
    // handling client connection error
    ws.onerror = function () {
        if (logger) console.log("Some Error occurred")
    }
});
console.log("The WebSocket server is running on port " + port);

function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

class Sender {
    packet
    map
    markers
}

class Server {
    map
    seed
    markers
    subs
}