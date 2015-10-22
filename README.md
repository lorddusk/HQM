# HQM
Hardcore Questing Mode


Updated Version to support Player Questfiles
Here is how it works.

1. On player login it will search the HQM folder for a playername.qd file.
  1. If there isnt any it will search for existing playerquest data.
  2. If there is player questdata it will use it.
  3. If there isnt any it will create it.
2. If there is a player quest file. It will load this file into the questdata. Overriding the existing one. For performance reasons it will only do this on the first questdata call.

On Playerlogout and Worldsave it will save the playerdata from players currently online or leaving player into a player file for each player.

Now this will not prevent the files from getting corrupt, but if they corrupt the player questdata is not lost.
you can just delete the hqm file. Restart the server and quest data for each player will load the first time they log in.

<This is not the readme.md you are looking for>
