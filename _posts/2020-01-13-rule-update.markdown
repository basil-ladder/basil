---
layout: post
title:  "Basil's first birthday and a rule update"
categories: basil
---


I'm a bit late, Basil already is 15 months old. It's well established in the Starcraft bot community (which you should join if you didn't already).
May it live for many moons to come.

Also - a small rule update:
Wall-clock timeouts no longer lead to wins or losses. The detection was never working properly, due to client bots prior to BWAPI 4.4 not being timed correctly.
That should prevent faulty bots from getting wins they don't deserve. It opens the way for potential abuse, but many bot authors actively monitor their bot's games - so it should be noticed.