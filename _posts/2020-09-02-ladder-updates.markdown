---
layout: post
title:  "Changes to the ladder"
categories: basil
---

For starters, a lot of "older" bots have been disabled on [SSCAIT] which carried over to BASIL. I'm not entirely sure about reasons, but
it plays far fewer games than BASIL does and focusing on the more active bots might indeed be preferable.

But that also means competition gets tougher for newcomers. Top bots getting stronger is nice. But, as [MarcoDBAA commented](http://satirist.org/ai/starcraft/blog/archives/962-CoG-2020-results-are-out.html#c29403) quite nicely: "the competition will slowly dry up".

Reaching the top is no easy feat, and requires an enormous amount of work. But playing in the middle or lower tier of the ladder should not discourage people from competing. But those bots a clobbered by the top tier. Instead of focusing on beating the nearby competition one might try to beat those "monsters" (I do love your bots!).

That is were the idea of a tiered ladder came once again (the idea is certainly not new). BASIL now has 2 years of continuous ranking and games between 2 random bots without bias. This was good: It provided many authors with data on their bot's performance vs all opponents. But it lead to extremely high win rates for the top bots and extremely low win rates for the bottom.

Now as a human player, that would be quite discouraging. For that reason human leagues usually match up players with equal strength. It is also a matter of time, as playing all opponents might just take too long to come up with a useful ranking.

Bots on the other hand... given enough resources, they have enough time to play all opponents. They also will not be demotivated by losing a lot or playing enemies that provide no challenge. But, bots are developed by humans. And the effect seems to transfer (at least according to the comments I linked above and the SSCAIT Discord).

This leads us to the changes BASIL will undergo:
* Disabling a bot on SSCAIT will no longer automatically disable it on BASIL - there is a host of good old bots which will provide ample challenge for newcomers and established bots alike.
* Matchmaking now selects opponents with similar ELO with higher likelihood. Your bot will still face far stronger/weaker bots, but not as often as now. This will make your bot's win rate approach 50% instead of 100% or 0%. ELO will still provide info about the general strength, while win rate will provide info about the relative strength in your "tier".
* Temporary rule change: Bots with win rate < 10% for the last 200 games are disabled instead of 15%/100 - this will be reverted once the new match making shows the desired effect
* Tiers: After some discussion, BASIL will try to emulate the ladder of [StarCraft Remastered](https://liquipedia.net/starcraft/StarCraft_Remastered_Ladder) with the ranks *S* and *A* to *F*. That means you can concentrate on climbing ranks without burning yourself at the *S* level (unless you are *S* or *A* level of course). The ranking table will be changed to make these tiers more prominent.
* ELO adjustment: BASIL's average ELO currently is ~2100 which is quite good considering the starting ELO is 2000. As mentioned above, the human ladder should be emulated somewhat, so all ELOs will be adjusted, as if the base-ELO was 1500. Since ELO is simple enough and only the difference in ELO points matters, this is a simple matter of subtracting ~600 from all ELO values. I will do a separate post when this change goes live.

Do you disagree? Or like it? Feel free to email me or join the [Discord chat](https://discord.gg/kt3GSpv)!

[SSCAIT]: https://sscaitournament.com/
