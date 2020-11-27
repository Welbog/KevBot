const Discord = require("discord.js");
const http = require("http");
const args = process.argv.slice(2);
const config = require(args[0]);

const port = config.port;
const host = config.host;
const path = config.path;
const token = config.token;
var ready = false;
var client = null;

// Some channels default to noisy mode, all other channels are addressing-required mode.
var channelToStatusMap = config.channelAddressingDefaults;

function getChannelName(message) {
	return message.guild.name + ":" + message.channel.name;
}

function toggleMode(message) {
	if (isPrivateMessage(message)) { return; }
	var currentMode = getMode(message);
	var channelName = getChannelName(message);
	if (currentMode == "STANDARD") {
		channelToStatusMap[channelName] = "QUIET";
	}
	else {
		channelToStatusMap[channelName] = "STANDARD";
	}
}

function getMode(message) {
	if (isPrivateMessage(message)) {
		return "STANDARD";
	}
	var name = getChannelName(message);
	if (!channelToStatusMap[name]) {
		channelToStatusMap[name] = "QUIET"; // Default to quiet
	}
	return channelToStatusMap[name];
}

function isPrivateMessage(message) {
	return !message.guild;
}

function reconnect() {
	if (!ready) {
		if (client != null) {
			console.log("Reconnecting to Discord");
			client.destroy();
    }
		client = new Discord.Client({autoReconnect:false});
		initializeKevBotClient(client);
		client.login(token);
		client.setTimeout(reconnect,180000); // 3 minutes
	}
}
 
function initializeKevBotClient(client) {
	client.on("ready", () => {
		ready = true;
		console.log("Discord adapter online.");
	});

	client.on("error", (e) => {
		ready = false;
		console.log(e);
	});

	client.on("unhandledRejection", (reason, p) => {
		ready = false;
		console.log("Promise: " + p);
		console.log(reason);
		reconnect();
	});

	client.on("warn", console.warn);
	 
	client.on("message", (message) => {
		//console.log(message);

		try {
				if (message.author.username == "KevBot") { return; }

			var channelName = message.author.username;
			var guildName = "Private"
			if (message.guild) {
				channelName = "#" + message.channel.name;
				guildName = message.guild.name;
			}

			const request = {
				medium: "DISCORD" + ":" + guildName,
				channel: channelName,
				sender: message.author.username,
				message: message.content,
				nickname: "KevBot",
				mode: getMode(message),
				type: "MESSAGE", // TODO: Actions are indicated with _XYZ_ in Discord
			};

			console.log(guildName + ":" + channelName + " <" + request.sender + "> " + request.message);

			const requestString = JSON.stringify(request);

			const options = {
				host: host,
				port: 1237,
				path: path,
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					'Content-Length': Buffer.byteLength(requestString)
				}
			};

			const handler = handleResponse.bind(null, message);

			var httpRequest = http.request(options, function(res) {
				res.setEncoding('utf8');
				res.on('data', handler);
			});
			httpRequest.setTimeout(5000);
			httpRequest.write(requestString);
			httpRequest.end();
		}
		catch (e) {
			console.log(e);
		}
	});
}

function handleResponse(message, responseChunk) {
	if (!responseChunk) { return; }
	const response = JSON.parse(responseChunk);
	console.log("<REPLY> " + response.body);
	var lines = [];
	if (response.body) {
		lines = response.body.replace(/\*/g,'\\*').replace(/`/g,'\\`').split("\n");
	}
	const type = response.type;
	if (type == "ACTION") {	
		lines.forEach(function(line) { message.channel.send(line); });
	} 
	else if (type == "JOIN") {
		// TODO
	}
	else if (type == "LEAVE") {
		// TODO
	}
	else if (type == "KEEP_PROCESSING") {
		// Do nothing
	}
	else if (type == "MESSAGE") {
		lines.forEach(function(line) { message.channel.send(line); });
	}
	else if (type == "MODE") {
		toggleMode(message);
	}
	else if (type == "NICKNAME") {
		// TODO
	}
	else if (type == "QUIT") {
		// TODO
	}
	else if (type == "STOP_PROCESSING") {
		// Do nothing
	}
	else {
		// Do nothing
	}
}

reconnect();
