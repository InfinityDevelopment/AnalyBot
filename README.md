# AnalyBot

AnalyBot (An-Alley-Bot, its a play on the word analysis) is a Discord bot made with the Javacord library designed to grab interesting statistics from a server, throw them in a txt file, and spit them back out to the user. It can also broadcast the stats of a specific suer, as well as send messages for the bot owner (that's a little easter egg). The commands are:
- A-help (Regular User) - Sends a help message in direct message
- A-pullstats (Regular User) - Sends user statistics of the user who sends this command in the channel it was sent in.
- A-txtgen (Server Administrator) - Generates the initial join txt and sends it to the administrator. This txt can keep being requested, as of the current there is no rate limit.
- A-sendmsg (Bot Owner) - Sends any and all text after the command from the bot, and deletes the origin message. Aa fun command string to try would be "A-sendmsg A-pullstats", which pulls up the bot's stats.
Soon I'm planning on making a Discord Role that is the requesite for using the A-txtgen command, but thats a "coming soon" sort of deal.
