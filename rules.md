---
layout: page
title: Rules
---

Participants
============
* Any bot already uploaded to [SSCAIT] automatically participates if enabled.
* If you want to opt-out, just drop me a mail: [bytekeeper@mailbox.org](mailto:bytekeeper@mailbox.org).
* If you want to participate but have not uploaded your bot to SSCAIT, drop me a mail and I'll try to add your bot.

Ranking
=======
* Bots get assigned ranks, similar to those in [SC:R](https://liquipedia.net/starcraft/StarCraft_Remastered_Ladder)
* Within a rank, the place of the bot is determined by its ELO
* If a bot climbs or drops in rank, it will be locked in that rank for the next 200 games - this prevents hysteresis and allows you to adapt to the new challenge. It also means, new bots are *UNRANKED* for their first 200 games.

Games
=====
* All games are 1v1.
* Starcraft Brood War version 1.16.1 is used.
* *No time limit per frame* is used due to technical limitations.
* *Real-time limit* is 30 minutes - after that a game will be killed. Neither bot gets a win or loss. Bots that seem to cause this very often will be disabled. Intenionally causing this timeout to avoid losing will result in the bot being banned.
* *Game-time limit* is 60 minutes - after which the player with the highest ingame kills+razings score will be assigned winner. If the score is identical, the *Realtime limit* rule applies.
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
  * 1 GB of RAM (- memory used by OS).
  * Up to 100MB of disk space total for your `read` + `write` + `ai` directories.
* Special options (applied by adding `BASIL: <option>[,<option>...]`:
  * *RESET* - Clear the directory on the next update (note: this will only delete the first time the new update is received) 
  * *PB-KEY-\<KeyId\>* - Publish the read directory, compressed and encrypted. This will use the key (given by KeyId - without white space) with the given key id (or fingerprint, or long id), downloaded from a public PGP keyserver (pgp.mit.edu). Using that the read directory will be compressed to `7z` file and then encrypted. Read directories are currently published once a day. For example: PB-KEY-ABCD01234 for the key id `ABCD0124`.
  * *PUBLISH-READ* - Publish the read directory, compressed but not encrypted. Please note that anyone can download the file and see what your bot wrote/learned.
  * *MAP-POOL:<Pool>;<Pool>...* - Add this if you want to support additional map pools. Ie. `MAP-POOL:2019Season1` to support the current human ladder map pool besides SSCAIT's pool. Note: Your bot is always expected to work on SSCAIT maps and any map pool you add here.
* Bots get disabled when
  * you request your bot to be disabled
  * they have a win-rate of below 15% in the last 100 games. If you update the bot it will get re-enabled and only the games since the update will count.

[SSCAIT]: https://sscaitournament.com/
