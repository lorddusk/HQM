# HQM
Hardcore Questing Mode


Updated Version to support Player Questfiles
Here is how it works.

1. On player login it will search the HQM questdata instance for data for the player.
  1. If there isnt any it will search for an existing player.questdata file.
  2. If there is a player.questdata file it will use that questdata and restore it and put it in the hashmap.
  3. If there isnt any it will create new questdata as before.
2. If there is questdata for the player already, nothing will change.

On Playerlogout and Worldsave it will save the playerdata from players currently online or leaving player into a player file for each player.

Now this will not prevent the files from getting corrupt, but if they corrupt the player questdata is not lost.
you can just delete the hqm file. Restart the server and quest data for each player will load the first time they log in.

<This is not the readme.md you are looking for>
