# KevBot

## Introduction
Oh golly, this is a terrible project.

I started this in, I think, 2004, when I was a student learning computer science. This code started in Java 1.4, and has been migrated poorly to later versions of Java, while being expanded, refactored and rewritten numerous times in the interim.

Please do not judge me too harshly for this project, as I used it as a sandbox to learn a lot of things, including my own database file format (RAHL and DoubleRAHL), DI framework, various protocols, various architectures, etc. It dabbles in things like expression parsing (yes, KevBot is Turing-complete), sentence generation, trying to be efficient while never trying to be readable. Most of the features that look like wheel reïnvention either didn't exist in 2004, or I wasn't aware of them, or I just wanted to give it a go to learn something.

Maybe one day I can refactor it into something good, but I probably won't.

The good news is that the plugin piece, as well as the math parsing and factoid systems are very robust. As a result, KevBot is quite reliable and extensible.

## Installation/usage

You will need:
* Maven
* Java
* MySQL with a database created, with an admin user and a data user (technically they can be the same user but please no).
* Node and NPM

You will need to make changes in files:
* kevbot.discord.config.json - Your bot's Discord token will need to be applied
* kevbot.properties - Your MySQL connection string will need to be applied, both an admin and data user.

Maven will import:
* MySQL connector
* Jackson serialization libraries

To build, run
* `mvn package` - This creates a directory, target, containing the KevBot jar file.

To setup, run
* `java -classpath target/*jar-with-dependencies.jar ca.welbog.kevbot.Setup`, sets up SQL structures.
* `npm install discord.js` in the discord directory, there's definitely a better way to do this but I haven't bothered to figure it out yet.

To launch, run
* Server: `java -classpath target/*jar-with-dependencies.jar ca.welbog.kevbot.KevBot configuration/kevbot.xml &>> /tmp/kevbot.server.log &`
* Discord client: `scripts/discordclient.sh &>> /tmp/kevbot.discord.log &`

Logs will be located in
* /tmp/kevbot.server.log - server logs
* /tmp/kevbot.discord.log - Discord client logs

## TODOs

What does the future hold for KevBot?

In no particular order:
* Unit testing (the first one was written 2020-12-02 woohoo!)
* Integration testing
* ~~Do an info-dump of KevBot's Documentation objects into the github-provided wiki, ideally with more elaborate examples,~~ and a link to said wiki from KevBot's help messages. Now that there's a wiki, link to that and keep the inline help to just the basics, otherwise some of the help messages are tl;dr.
  * ~~Especially examples of the more involved factoids, like the Hogwarts points, math recursion like `$triangle` and pattern matching like `I $a $b`.~~
* Support Discord features (@, unique user identifiers, usernames with spaces in them, etc)
  * Drop support for JOIN and LEAVE, or modify them heavily for Discord since it works very differently from IRC.
* ~~Move to Maven or some other dependency-management tool (for MySQL in the short term, Spring or something in the longer term)~~
* ~~Move to Spring or some other dependency-injection tool (instead of the shitty one I made myself).~~
* Add security to the KevBot protocol somehow.
* ~~Break the codebase out better (client vs server vs intermediate libraries containing message objects, plugin interfaces, etc).~~
* A feature for spontaneous communication, though I've personally wanted to avoid it. This would be implemented as a separate HTTP URL for polling new messages, by client. This could enable something like someone in IRC sending a message to someone in Discord using the `say` command.
  * The basic idea here would be for the server-side to have a queue of messages, and the client would poll said queue periodically and forward them to wherever it needs to go. This needs a reasonable mechanism for specifying where messages need to go, as well as a server-side map of queues by client.
  * Then, for the different clients, they can process it however is appropriate for that chat system. Discord, for example, probably has an API to call to send a message to a channel, to a specific user, etc.
* Javadoc for at least the most important stuff (possibly auto-generated from the Documentation objects?) ~~(possibly auto-generating part of this README from said Documentation objects?) Nah, that's silly.~~
* Maybe add support for Google Hangouts (or whatever they will inevitably replace it with)?
* Improve Echo support.
* Add SQL transactions & move things off of flat files and into SQL & ~~add setup so that when KevBot detects a table is missing it can create it (or at least a set-up script).~~
  * ~~A general set-up script that can create default configuration, as well as basic default launch scripts for HTTP & Discord.~~
* "Explain" mode that lists responder metadata so the handler chain can be examined & debugged more directly.
* An actual logger instead of stdout.
* Move Discord adapter to Java with something like https://github.com/Discord4J/Discord4J or https://github.com/DV8FromTheWorld/JDA
* Lots of other stuff

## Code structure

Very haphazard.

* discord: discord client
* echo: echo client
* src: server
  * ca/welbog/kevbot: Base KevBot class for launching the server, as well as its core processing loop.
    * client: a base HTTP client that can be used to build specific adapters, if those adapters are Java-based (which at this time none are).
    * communication: base classes for DTOs KevBot uses
    * core: KevBot's core processing loop and interfaces that make it go brrrrr
    * http: Server's HTTP entry point.
    * log: my terrible logging "framework".
    * persist: file- and SQL-based storage utilities.
    * responder: this is where the plugins live, the majority of the user-facing functionality of KevBot lives here.
    * utils: what project is complete without a directory for misfit functions that should be refactored?
* configuration: Spring XML configs. Why Spring XML? Because KevBot instances will want to add/remove plugins, and the order of the plugins matters. Spring XML offers a free way to accomplish this without having to do it myself.
* target: the build directory

## Version history

### 1.x

Mid 2004 to probably 2006ish.

Originally an IRC bot built to handle asynchronous messaging between users who weren't necessarily logged in.
IRC had no persistent messages, so when one user wanted to PM someone who wasn't online, they could not.
KevBot allowed users to store and retrieve messages to/from users.

### 2.x

2006ish to who knows when? Like 2008 or something.

Added InfoBot-like features, especially keeping track of the last thing someone said and where (the `seen` feature), 
crappy math expressions (which were later expanded heavily) and `factoids` which are simple phrases for the bot to look for and
repeat "facts".

### 3.x

2008 or something

Better math expressions, functions, added Markov chaining features, at this time based on the factoid database of strings rather than its own data source.

### 4.x

2010 probably

Added SQL, updated factoids and Markov chaining to use that database. Expanded factoids with metadata (such as who set it and when).
Added per-user Markov statistics for the `m2by` and `guess` features.

### 5.x

Probably 2012 or so.

Recursion, which is the ability for factoids to have subsections that refer to other features.
This required a complete overhaul of KevBot, turning it from a god-class architecture to a set of plugins executed in order (see `kevbot.config`) conforming to a common interface and operating on a message object with metadata to help tell the processor whether it should keep processing or not.

It's at this point that KevBot's factoid and math expressions became Turing-complete, with math becoming properly recursive and using
the `$if` function to cover base cases. The limit is the recursion depth, which exists to prevent KevBot requests from taking too long to process.

### 6.x

2016 or around then

Client-server architecture, which in this case is splitting out the IRC connection part of KevBot from the message processing part.
This relied heavily on the features introduced in 5.x to decouple processing from the channel used to send/receive data.
At this point, there is an HTTP server that does the message processing and an IRC client that connects to IRC, translates IRC into 
the HTTP KevBot protocol, processes the messages, re-translates the KevBot protocol into IRC commands.

2019

Added Discord client, dropped IRC client. The Discord client just kludges Discord's concepts into IRC's rough equivalents, which has a lot of flaws.
The Discord client is a quick & dirty implementation.

2020

Removed IRC stuff, moved a bunch of security-related stuff to files that aren't in git.
Clobbered git history, opened the repo to the public.

### 6.2.x

Late 2020 - Added Maven, refactored code, added setup scripts, added Spring. General focus on bringing engineering to KevBot. Making a codebase to which I might actually want to contribute. 
