// Include the Alexa SDK v2
const Alexa = require("ask-sdk-core");
const http = require("http")
const config = require(args[0]);

const port = config.port;
const host = config.host;
const path = config.path;

var responsePhrase = "Hello, something went wrong.";

// The "LaunchRequest" intent handler - called when the skill is launched
const LaunchRequestHandler = {
  canHandle(handlerInput) {
    return handlerInput.requestEnvelope.request.type === "LaunchRequest" ||
      handlerInput.requestEnvelope.request.type === "IntentRequest";
  },
  handle(handlerInput) {

    console.log("Trying to call KevBot over HTTP");
    const request = {
      medium: "ALEXA" + ":" + "TODO",
      channel: "ALEXA" + "-CHANNEL",
      sender: "ALEXA" + "-USER",
      message: "m2",
      nickname: "KevBot",
      mode: "STANDARD",
      type: "MESSAGE",
    };
    
    const requestString = JSON.stringify(request);
    const options = {
      host: host,
      port: port,
      path: path,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(requestString),
      },
    };
    
    const handler = handleResponse.bind(null, handlerInput);
    var httpRequest = http.request(options, function(res) {
      res.setEncoding('utf8');
      res.on('data', handler);
    });
    httpRequest.setTimeout(2000);
    httpRequest.write(requestString);
    httpRequest.end();
    

    // Speak out the speechText via Alexa
    return handlerInput.responseBuilder.speak(responsePhrase).getResponse();
  }
};

function handleResponse(handlerInput, responseChunk) {
  if (!responseChunk) { return; }
  console.log("Got here 1");
  console.log(responseChunk)
  console.log("Got here 2");;
  const response = JSON.parse(responseChunk);
  console.log("<REPLY>" + response.body);
  const type = response.type;
  responsePhrase = "I'm sorry, I don't have anything matching that input.";
  if (type == "ACTION" || type == "MESSAGE") {
    responsePhrase = response.body;
  }
}

// Register the handlers and make them ready for use in Lambda
exports.handler = Alexa.SkillBuilders.custom()
  .addRequestHandlers(LaunchRequestHandler)
  .lambda();

