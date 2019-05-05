---
layout: page
title: Rules
---

Participants
============
* Any bot already uploaded to [SSCAIT] automatically participates if enabled.
* If you want to opt-out, just drop me a mail: [bytekeeper@mailbox.org](mailto:bytekeeper@mailbox.org).
* If you want to participate but have not uploaded your bot to SSCAIT, drop me a mail and I'll try to add your bot.

Games
=====
* All games are 1v1.
* Starcraft Brood War version 1.16.1 is used.
* *No in-game time limit* is used due to technical limitations.
* *Realtime limit* is 30 minutes - after that a game will be killed and the bot with lower time spend per frame (all frames > 0) will be assigned winner.
* *Gametime limit* is 60 minutes - after which the player with the highest ingame kills+razings score will be assigned winner. If the score is identical, the *Realtime limit* rule applies.
* *Draws* are only possible if bots have the same score and the same time spent per frame (almost impossible) - and are not counted as win/loss.
* *Speed* setting is LF3 (Normal).
* *Map pool* being used is that of [SSCAIT](https://sscaitournament.com/index.php?action=maps).
* *Random* race is not possible, random bots will get a random race selected _before_ the game is launched.
* A *crash* counts as a loss, unless both bots crash.
* Every played game is counted as "played", even if it crashes.

Bots
====
* Generally, the rules of [SSCAIT](https://sscaitournament.com/index.php?action=rules) apply.
* `bwapi-data/write` will be copied to your `bwapi-data/read` directory. By default, the `read` directory will not be cleared, when a bot is updated!
* Bots are automatically updated every 6 hours.
* As with [SSCAIT] no other bot author can view or download either the `read` or the `write` folders of your bot.
* All replays can be downloaded by everyone.
* Game, bot and crash logs can also be downloaded by everyone.
* Accessing the internet/network is generally prohibited. (With some notable exception I won't disclaim)
* Hardware resources: 
  * 1 CPU core of an `AMD Ryzen 7 1700X`.
  * 1 GB of RAM (- memory uses by OS).
  * Up to 100MB of disk space total for your `read` + `write` + `ai` directories.
* Special options (applied by adding `BASIL: <option>[,<option>...]`:
  * *RESET* - Clear the directory on the next update (note: this will only delete the first time the new update is received) 
  * *PB-KEY-\<KeyId\>* - Publish the read directory, compressed and encrypted. This will use the key (given by KeyId - without white space) with the given key id (or fingerprint, or long id), downloaded from a public PGP keyserver (pgp.mit.edu). Using that the read directory will be compressed to `7z` file and then encrypted. Read directories are currently published once a day. For example: PB-KEY-ABCD01234 for the key id `ABCD0124`.
  * *PUBLISH-READ* - Publish the read directory, compressed but not encrypted. Please note that anyone can download the file and see what your bot wrote/learned.
  * *MAP-POOL:<Pool>;<Pool>...* - Add this if you want to support additional map pools. Ie. `MAP-POOL:2019Season1` to support the current human ladder map pool besides SSCAIT's pool. Note: Your bot is always expected to work on SSCAIT maps and any map pool you add here.
    

[SSCAIT]: https://sscaitournament.com/
