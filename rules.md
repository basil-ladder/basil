---
layout: page
title: Rules
---

Participants
============
* Any of bot already uploaded to [SSCAIT] automatically participates if enabled.
* If you want to opt-out, just drop me a mail: [bytekeeper@mailbox.org](mailto:bytekeeper@mailbox.org).
* If you want to participate but have not uploaded your bot to SSCAIT, drop me a mail and I'll try to add your bot.

Games
=====
* All games are 1v1.
* Starcraft Brood War version 1.16.1 is used.
* *No in-game time limit* is used due to technical limitations.
* *Realtime limit* is 20 minutes - after that a game will be killed and won't count.
* *Draws* are not possible.
* *Speed* setting is LF3 (Normal).
* *Map pool* being used is that of [SSCAIT](https://sscaitournament.com/index.php?action=maps).
* A *crash* counts as a loss, unless both bots crash.
* Every played game is counted as "played", even if it crashes.

Bots
====
* Generally, the rules of [SSCAIT](https://sscaitournament.com/index.php?action=rules) apply.
* `bwapi-data/write` will be copied to your `bwapi-data/read` directory. by default, the `read` directory will not be cleared, when a bot is updated!
* To have the read directory cleared when updating, add `BASIL: RESET` somewhere in the [SSCAIT] description.
* Bots are automatically updated every 6 hours.
* As with [SSCAIT] no other bot author can view or download either the `read` or the `write` folders of your bot.
* All replays can be downloaded by everyone.
* Game, bot and crash logs can also be downloaded by everyone.
* Accessing the internet/network is generally prohibited. (With some noteable exception I won't disclaim)
* Hardware resources: 
  * 1 CPU core of an `AMD Ryzen 7 1700X`.
  * 1 GB of RAM (- memory uses by OS).
  * Up to 100MB of disk space total for your read + write + ai drectories.

[SSCAIT]: https://sscaitournament.com/
