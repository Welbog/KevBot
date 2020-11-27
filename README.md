# KevBot

## Introduction
Oh golly, this is a terrible project.

I started this in, I think, 2004, when I was a student learning computer science. This code started in Java 1.4, and has been migrated poorly to later versions of Java, while being expanded, refactored and rewritten numerous times in the interim.

Please do not judge me too harshly for this project, as I used it as a sandbox to learn a lot of things, including my own database file format (RAHL and DoubleRAHL), DI framework, various protocols, various architectures, etc. It dabbles in things like expression parsing (yes, KevBot is Turing-complete), sentence generation, trying to be efficient while never trying to be readable. Most of the features that look like wheel reïnvention either didn't exist in 2004, or I wasn't aware of them, or I just wanted to give it a go to learn something.

Maybe one day I can refactor it into something good, but I probably won't.

The good news is that the plugin piece, as well as the math parsing and factoid systems are very robust. As a result, KevBot is quite reliable and extensible.

## TODOs

What does the future hold for KevBot?

In no particular order:
* Unit testing
* Integration testing
* Support Discord features (@, unique user identifiers, usernames with spaces in them, etc)
* Move to Maven or some other dependency-management tool (for MySQL in the short term, Spring or something in the longer term)
* Move to Spring or some other dependency-injection tool (instead of the shitty one I made myself).
* Add security to the KevBot protocol somehow.
* Break the codebase out better (client vs server vs intermediate libraries containing message objects, plugin interfaces, etc).
* A feature for spontaneous communication, though I've personally wanted to avoid it. This would be implemented as a separate HTTP URL for polling new messages, by client. This could enable something like someone in IRC sending a message to someone in Discord using the `say` command.
* Javadoc for at least the most important stuff (possibly auto-generated from the Documentation objects?) (possibly auto-generating part of this README from said Documentation objects?)
* Lots of other stuff

## Code structure

Very haphazard.

* discord: discord client
* echo: echo client
* src: server
  * ca/welbog/kevbot: Base KevBot class for launching the server, as well as its core processing loop.
    * client: various clients, none of which do anything
    * communication: base classes for DTOs KevBot uses, as well as a bare HTTP client that knows how to talk to the server.
    * configuration: my terrible DI "framework".
    * http: Server's HTTP entry point.
    * log: my terrible logging "framework".
    * persist: file- and SQL-based storage utilities.
    * responder: this is where the plugins live, the majority of the user-facing functionality of KevBot lives here.
    * service: another part of the terrible DI "framework".
    * utils: what project is complete without a directory for misfit functions that should be refactored?

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
