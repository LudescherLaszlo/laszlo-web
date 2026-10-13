# Mr. White — Game Website Specification

*Version 1.0 · Spring Boot (Maven) backend · React + shadcn/ui frontend*

## 1. Overview

A real-time party game played in person, on each player’s own phone. Players sit together and say their clues out loud. The website handles roles and words, speaking order, voting, eliminations, Mr. White’s guess and win detection.

Principles:

- **One game at a time** for the whole site. No rooms, no accounts.
- **The server is the authority.** Rules, randomness and state live on the backend. Clients send commands and render state.
- **Private stays private.** A player’s role and word are only ever sent to that player.
- **Identical main screens.** During play, every living player’s main screen looks the same, so nobody can read a role off someone else’s phone. Secret information lives only inside the toggleable word card.
- **Clues, discussion and Mr. White’s guess happen out loud.** The site never records them, only the verdict.

## 2. Technology and Architecture

**Backend:** Java 17+, Spring Boot 3, Maven. Dependencies: `spring-boot-starter-websocket` (STOMP over WebSocket, SockJS fallback), `spring-boot-starter-data-jpa`, H2 for development or PostgreSQL for production, Flyway or `data.sql` to seed word pairs.

- Game state is held **in memory** in one singleton `GameService`. The database only stores word pairs.
- All commands are processed **serially** (one lock or a single-thread executor), so simultaneous clicks cannot cause races.
- Timers use `TaskScheduler` / `@Scheduled`.

**Channels**

| Channel | Direction | Purpose |
| --- | --- | --- |
| `/app/**` | client → server | commands |
| `/topic/game` | server → all | public state, sent after every change |
| `/user/queue/private` | server → one player | private state: own role and word, judge prompts, flags |
| `/user/queue/errors` | server → one player | rejected commands |

**Identity:** the client sends its `sessionId` in the STOMP CONNECT header. A channel interceptor maps it to the player and sets the principal. The `sessionId` is a secret (UUID v4) and is never included in any broadcast. Only one live connection per session is allowed: a newer connection replaces the older one, which shows “Opened on another device”.

**Frontend:** React + TypeScript + Vite, Tailwind, shadcn/ui, `@stomp/stompjs`. The UI is a pure function of the latest public and private state: the visible screen is chosen from the player’s status and the game phase, so no client-side routing is needed. Mobile-first (360 px wide), light and dark theme.

shadcn components: Button, Input, Card, Badge, Progress, AlertDialog (all confirmations), Dialog, DropdownMenu (menu), ScrollArea, Sonner (toasts), Separator, Skeleton. The wheel is a custom SVG with a CSS rotation transition.

**Public state** contains: phase, round, state version, players (name, alive/spectator, connected, and the role only for eliminated players), speaking order and current speaker, vote counters, and the current screen’s public data. It never contains the role or word of a living player.

**Private state** contains: own role, own word, whether a Mr. White guess is available, judge prompts, own vote and ready flags.

On every connect or reconnect the server sends a full snapshot. Public state carries an increasing `stateVersion`, and clients ignore older versions.

## 3. Rules

### 3.1 Roles

- **Civilians** receive the secret word.
- **Undercovers** receive a slightly different, similar word. They are not told they are Undercover.
- **Mr. White** receives no word.

### 3.2 Role distribution (3–30 players)

The remaining players are always Civilians.

| Players | Undercovers | Mr. Whites |
| --- | --- | --- |
| 3 | 0 | 1 |
| 4–6 | 1 | 1 |
| 7–9 | 2 | 1 |
| 10–12 | 2 | 2 |
| 13–15 | 3 | 2 |
| 16–18 | 3 | 3 |
| 19–21 | 4 | 3 |
| 22–24 | 4 | 4 |
| 25–27 | 5 | 4 |
| 28–30 | 5 | 5 |

### 3.3 Mr. White’s guess

- Every Mr. White has **exactly one guess** at the Civilian word.
- The guess can be used **in two ways**: voluntarily during the Discussion or Voting phase (using the guess button on his word card), or automatically when he is executed.
- A guess is spoken out loud and then judged (see 6.9).
- **Correct guess:** the game ends immediately and all Mr. Whites win.
- **Wrong guess:** that Mr. White is eliminated and his role is revealed.
- A Mr. White who has already used his guess has no guess left. If he is executed later, no guess phase happens and it counts as a failed guess.

### 3.4 Win conditions

The server computes the counts `aliveCiv`, `aliveUC`, `aliveMW` and evaluates the conditions below after every elimination and after every guess verdict. Conditions are checked in this order:

1. **Mr. White wins (guess):** a guess is confirmed correct. All Mr. Whites win.
2. **Civilians win:** `aliveUC = 0` and `aliveMW = 0`.
3. **Undercovers win:** `aliveUC ≥ 1` and `aliveUC ≥ aliveCiv`. Any Mr. White still alive at that moment wins jointly with them. Mr. Whites are not counted on either side of the comparison.
4. **Mr. White wins (survival):** `aliveUC = 0`, `aliveMW ≥ 1` and `aliveMW ≥ aliveCiv`. The living Mr. Whites win.

Conditions 2, 3 and 4 are mutually exclusive. If none applies, the game continues. Whenever 2 or fewer players are alive, one of them always applies, so the game can never stall.

An executed Mr. White’s guess is always resolved **before** conditions 2–4 are evaluated.

## 4. Names, Sessions and Player Status

**Names**

- Trimmed, repeated spaces collapsed, 1–20 characters, Unicode letters, digits, spaces and basic punctuation.
- **Unique, case-insensitive.** A name is reserved by every player the server knows about: lobby, waiting room, in-game, spectators, post-game and temporarily disconnected players.
- A duplicate shows the inline error “That name is already taken. Choose another one.” and no player is created.

**Sessions**

- On a successful join the server returns a `sessionId`, which the client stores in `localStorage` under `mrwhite.sessionId`.
- On every page load the client reconnects with the stored `sessionId`. If it is valid, the player returns to exactly the screen they were on (role, word, phase, pending votes). If it is unknown (for example after a reset), the client clears it and shows the landing page.

**Player status**

| Status | Meaning |
| --- | --- |
| WAITING | in the waiting room because a game is running |
| LOBBY | in the lobby |
| ALIVE | playing |
| SPECTATOR | eliminated, still watching |
| POSTGAME | game finished, has not pressed Return to Lobby yet |

## 5. Global Game State Machine

| Phase | Description | Next |
| --- | --- | --- |
| LOBBY | players gather | ASSIGNMENT |
| ASSIGNMENT | everyone checks their word card | DISCUSSION |
| DISCUSSION | spoken clues, one at a time | VOTING |
| VOTING | all living players vote | EXECUTION, or TIEBREAK on a tie |
| TIEBREAK | revote among tied players | EXECUTION, or WHEEL on a tie |
| WHEEL | server picks the victim, animated everywhere | EXECUTION |
| EXECUTION | result, personalized messages, ready check | REVEAL |
| REVEAL | the eliminated player’s role is shown for 8 seconds | MR\_WHITE\_GUESS, DISCUSSION or VICTORY |
| MR\_WHITE\_GUESS | spoken guess and verdict | REVEAL (wrong guess) or VICTORY |
| VICTORY | winners, words, all roles | LOBBY |

Only one game exists at any time. “Waiting room” is a player status, not a phase.

## 6. Behaviour by Phase

### 6.1 Landing page

A prompt to enter a name. After joining:

- If a game is running (any phase except LOBBY): the player becomes WAITING and sees the **Waiting Room** screen: “Game in progress. Waiting for the current game to end to join the lobby…”
- Otherwise the player joins the lobby, unless it already holds 30 players, in which case the page says “Lobby full (30/30)”.

### 6.2 Lobby

- A live list of all connected players, plus a player count.
- A prominent **Start Game** button, visible to every lobby player, **disabled below 3 players**. A Leave option is in the menu.
- Pressing Start opens an AlertDialog: “Start the game with \[X\] players?” On confirm the client sends the player count it saw. If the real count differs, the server rejects the start and shows the new count for reconfirmation. The first valid confirmation wins and locks the lobby. Late clickers see “The game has already started.”
- Only **connected** players take part. A lobby player who has been disconnected for more than 60 seconds is removed.
- When a game ends and the lobby reopens, waiting-room players are moved into the lobby in join order. If that would exceed 30 players, the extra players stay in the waiting room.

### 6.3 Assignment

- The server picks one word pair (see section 8) and assigns roles at random, strictly following the table in 3.2.
- Each player sees a **word card**, hidden by default (“Tap to reveal”) and toggled by tapping.
  - Civilian: shows the Civilian word.
  - Undercover: shows the similar word, with no mention of being Undercover.
  - Mr. White: shows “You are Mr. White. You have no word.” and a **Guess the word** button, which exists **only while the card is revealed** (see 6.9).
- Every player presses **I’ve seen my word**. A counter shows the number of players ready. When all are ready the game moves to Discussion, round 1.

### 6.4 Discussion

- The server generates a completely random speaking order of all living players. **It is reshuffled every round.** Mr. White can be chosen to go first.
- The screen highlights whose turn it is. The current speaker taps **Done** to advance. Any living player can also tap **Skip speaker** (with confirmation) if the speaker is absent.
- Every living player has an **End Round** button. It opens a confirmation: “Has everyone given a clue?” On confirm, all screens move to Voting.
- Spectators cannot see or press Done, Skip or End Round.
- The main screen is identical for every living player. Only the word card differs.

### 6.5 Voting

- All living players see the list of living players, **excluding themselves**. Tapping a name selects it, and **Confirm vote** locks it. Votes are final and abstaining is not possible.
- A live counter reads “Waiting for players to vote (5/8)…”.
- When the last vote is cast, the result is resolved instantly:
  - A single highest vote count: that player is executed.
  - A tie for first place: go to the tie-break.
- Spectators see the counter but cannot vote.

### 6.6 Tie-break and Wheel of Fortune

- **Tie-break:** a new voting screen where only the tied players are selectable. **All living players vote** (including the tied ones), and nobody can vote for themselves. The same counter and locking rules apply.
- A single highest count executes that player. A second tie goes to the Wheel.
- **Wheel:** the server picks the victim uniformly at random from the players tied in the tie-break and sends the winner, the start time and the duration to every client. All screens play the same spinning animation (about 6 seconds) and land on the server’s choice. Clients never decide the outcome.

### 6.7 Execution

Shown after the victim is decided. For everyone, the executed player’s name is highlighted and per-player vote counts are shown, along with a line explaining any tie-break or wheel that took place.

Personalized messages use the votes of the **first voting round**:

- Executed player: “You got executed.” and the list of who voted for them.
- Surviving player with 1 vote: “\[A\] wants to brutally murder you, but you survived.”
- Surviving player with 2 or more votes: “\[A\], \[B\] and \[C\] want to brutally murder you, but you survived.”
- Surviving player with 0 votes: “Nobody voted for you. Suspiciously safe.”

**Ready check:** every living player and the executed player must press **Ready**. A counter is shown. The phase ends when all have pressed it.

### 6.8 Role reveal and spectator mode

- The server marks the player as eliminated and shows their **role to everyone** for 8 seconds. The same reveal is used for a wrong Mr. White guess.
- The eliminated player becomes a **spectator**:
  - They see the Civilian word and the Undercover word on their screen.
  - They watch live turn order, voting and results, but their vote, Done, Skip, End Round and Ready buttons disappear.
  - **Exception:** an eliminated Mr. White does **not** see the words until the game ends, so that he cannot read the answer off his own screen before or after his guess.
- The eliminated player is removed from the speaking order.
- If the executed player is a Mr. White whose guess is still unused, the game moves to MR\_WHITE\_GUESS. Otherwise it goes straight to the win check.

### 6.9 Mr. White’s guess

**Triggering**

- *Voluntary:* only a **living** Mr. White with an unused guess, and only during **Discussion** or the first **Voting** round. He reveals his word card, taps **Guess the word**, and confirms in an AlertDialog: “Say your guess out loud to the group. You only have one guess. Ready?” The button is not available during Tiebreak, Wheel, Execution or Reveal. Because it exists only on his own revealed card, the main screen stays identical for everyone.
- *Execution:* automatic when an executed Mr. White still has his guess.

When triggered, the guess is marked as used, the game pauses, and every screen shows “\[Name\] is Mr. White and is making a guess!” (his identity is exposed by the guess).

**Judging**

- The Mr. White speaks his guess out loud.
- **Judges** are all Civilians (living or eliminated) and all eliminated Undercovers. Living Undercovers do not judge (seeing the Civilian word would reveal their role) and Mr. Whites do not judge (they do not know the word).
- Each judge sees, privately: “The Civilian word is \[word\]. Did \[Name\] guess it correctly?” with **Yes** and **No** buttons, and a counter of judges who have answered.
- The verdict resolves early as soon as it is decided: **Yes** needs a strict majority of judges. A tie or any outcome where Yes cannot reach a majority counts as **No**.

**Result**

- **Yes:** victory for all Mr. Whites.
- **No:** the Mr. White is eliminated (role revealed). If he was already being executed, nothing further is needed. Then the win check runs. If nobody has won:
  - interrupted **Discussion** resumes with the same round and order, minus the eliminated player (if he was the current speaker, move to the next one);
  - interrupted **Voting** restarts: all cast votes are cleared and everyone votes again with updated candidates.

### 6.10 Next round

If no win condition is met after an elimination, a new round starts: round number +1, a **new random speaking order** of the living players, back to Discussion.

### 6.11 Victory screen

Shown on every screen when a win condition is met or the game is aborted. It displays:

- the winners (Mr. White; Civilians; Undercovers; Undercovers + Mr. White jointly; or “Game aborted by \[Name\]”);
- both secret words;
- every player’s **original role** and whether they survived.

A **Return to Lobby** button is on the screen. See 6.12.

### 6.12 Return to lobby

- Pressing **Return to Lobby** behaves like joining: if no game is running, the player enters the lobby. The first player to press it reopens the lobby and moves all waiting-room players in at that moment, so a new game can be gathered. Players who have not pressed it yet remain in POSTGAME on the victory screen and can press it later.
- If a new game has already started when someone presses it, they join the waiting room instead.
- A POSTGAME player who does not return within 10 minutes of the lobby reopening is removed and their name freed.

### 6.13 Abort and leave

- A small, unobtrusive **End Game** entry is in the menu at all times for every in-game player (living or spectator). It requires an AlertDialog confirmation. If confirmed, the game ends immediately and everyone sees the Victory screen as “Game aborted by \[Name\]”, with words and roles revealed.
- **Leave game** (menu, with confirmation) removes the player. In-game, this counts as an elimination: the role becomes visible in the player list, the current phase adapts (see 7.2) and the win check runs.

## 7. Disconnections, Removal and Inactivity

### 7.1 Connection handling

- Closing the tab or losing connection never changes a player’s role or state. The player list shows a **Disconnected** badge.
- Reconnecting with the same session restores the exact screen, including a locked vote, a ready flag or a pending verdict.

### 7.2 Removing an absent player

A player who has been disconnected for **more than 60 seconds** gets a **Remove** button visible to every other in-game player, with an AlertDialog confirmation. Removal (and Leave) is treated as an elimination, and the current phase adapts:

- **Assignment / Execution ready check:** the player is no longer required.
- **Discussion:** removed from the order; if they were the current speaker, advance to the next.
- **Voting / Tie-break:** all votes are cleared and voting restarts with the remaining players as voters and candidates.
- **Mr. White guess:** a removed judge is dropped from the judge list; if the guessing Mr. White is removed, it counts as a failed guess.
- **Always:** the win check then runs.

### 7.3 Inactivity reset

If the server receives **no player action for 30 minutes** (heartbeats and pings do not count), the whole game resets: all players and sessions are cleared. Connected clients show “The game was reset due to inactivity” and return to the landing page. This also applies to an idle lobby.

### 7.4 Cleanup timers

| Situation | Removed after |
| --- | --- |
| Lobby player disconnected | 60 seconds |
| Waiting-room player disconnected | 10 minutes |
| POSTGAME player not returned | 10 minutes |

In-game players are never removed automatically, only through the Remove / Leave flow.

## 8. Word Pairs

- Table `word_pair(id, word_a, word_b)`, seeded with at least 100 pairs in one language chosen per deployment. There is no admin UI: pairs are added by editing the seed file.
- The server picks a random pair, avoiding the last 10 used. It randomly decides which of the two words is the Civilian word and which is the Undercover word, so pair order never reveals anything.
- In a 3-player game (0 Undercovers) a pair is still drawn, but the Undercover word is unused.

## 9. Commands and Server Validation

| Command | Sent by | Allowed in | Effect |
| --- | --- | --- | --- |
| JOIN {name} | new client | any | validates the name, creates the player, returns the sessionId |
| RECONNECT {sessionId} | client | any | restores the player and sends a snapshot |
| LEAVE | any player | any | see 6.13 |
| START\_GAME {count} | lobby player | LOBBY | validates 3 ≤ players ≤ 30 and the count, starts ASSIGNMENT |
| ASSIGNMENT\_READY | player | ASSIGNMENT | marks the player ready |
| NEXT\_SPEAKER | current speaker | DISCUSSION | advances the highlight |
| SKIP\_SPEAKER | living player | DISCUSSION | advances the highlight |
| END\_ROUND | living player | DISCUSSION | moves to VOTING |
| VOTE {targetId} | living player | VOTING, TIEBREAK | records a final vote (not self, valid candidate) |
| READY | living or executed player | EXECUTION | marks ready |
| DECLARE\_GUESS | living Mr. White | DISCUSSION, VOTING | starts MR\_WHITE\_GUESS |
| GUESS\_VERDICT {correct} | judge | MR\_WHITE\_GUESS | records a judge’s answer |
| REMOVE\_PLAYER {id} | in-game player | any in-game phase | only if the target is disconnected for over 60 s |
| END\_GAME | in-game player | any in-game phase | aborts the game |
| RETURN\_TO\_LOBBY | POSTGAME player | VICTORY or LOBBY | see 6.12 |

Every command is validated on the server for: valid session, player status and role, current phase, and idempotence. A stale or invalid command (for example voting after the round ended) is ignored and the player gets “That action is no longer available.” The client never trusts its own state.

## 10. Edge Cases

- **Refresh during voting:** the vote stays recorded and the screen shows it as locked.
- **Double-click or simultaneous clicks:** only the first valid command counts.
- **Same session on two devices:** the newer connection takes over.
- **A name freed by a reset or removal** can be used by a new player.
- **Two living players left:** never happens without a winner (see 3.4).
- **Dead players sharing words out loud** is a social rule, not enforced by the site.
- **A game with 0 Undercovers** (3 players): the Undercover win condition cannot trigger. Mr. White wins by survival if only one Civilian remains alongside him.
- **Every Mr. White but one is dead** and the last one has already guessed wrongly: not possible, a wrong guess eliminates him.

## 11. Suggested Build Order (for prompting)

1. Maven project, WebSocket/STOMP config, session handshake, in-memory `GameService` skeleton, word-pair table and seeding.
2. Landing page, unique names, `localStorage` session, reconnect snapshot, Lobby and Waiting Room.
3. Start flow, role distribution table, word picking, Assignment screen with the word card.
4. Discussion: reshuffled speaking order, Done, Skip, End Round.
5. Voting, counters, tie-break, server-driven Wheel, Execution screen with personalized messages, Ready check.
6. Reveal, spectator mode, win evaluation (3.4), Victory screen, Return to Lobby.
7. Mr. White guess: button on the word card, judging, resume logic.
8. Disconnection handling, Remove / Leave, End Game, inactivity reset, cleanup timers.
9. Polish: mobile layout, theming, toasts, error messages, a short “How to play” dialog.
