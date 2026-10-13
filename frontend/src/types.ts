export type Phase = 'LOBBY' | 'ASSIGNMENT' | 'DISCUSSION' | 'VOTING' | 'TIEBREAK' | 'WHEEL' | 'EXECUTION' | 'REVEAL' | 'MR_WHITE_GUESS' | 'VICTORY'
export type Role = 'CIVILIAN' | 'UNDERCOVER' | 'MR_WHITE'
export type PlayerStatus = 'WAITING' | 'LOBBY' | 'ALIVE' | 'SPECTATOR' | 'POSTGAME'

export interface Player { id: string; name: string; status: PlayerStatus; connected: boolean; disconnectedSince: number | null; role: Role | null }
export interface PublicState {
  stateVersion: number; serverTime: number; phase: Phase; round: number; minPlayers: number; maxPlayers: number
  players: Player[]; speakingOrder: string[]; currentSpeakerId: string | null
  vote: { voteRound: 1 | 2; candidateIds: string[]; votedCount: number; totalVoters: number } | null
  ready: { readyCount: number; totalRequired: number } | null
  execution: { executedPlayerId: string; voteCounts: Record<string, number>; resolvedBy: 'VOTE' | 'TIEBREAK' | 'WHEEL' } | null
  wheel: { candidateIds: string[]; winnerId: string; startAt: number; durationMs: number } | null
  reveal: { playerId: string; role: Role; endsAt: number; reason: 'EXECUTED' | 'WRONG_GUESS' } | null
  guess: { playerId: string; judgesAnswered: number; judgesTotal: number } | null
  victory: { winnerType: 'MR_WHITE' | 'CIVILIANS' | 'UNDERCOVERS' | 'UNDERCOVERS_AND_MR_WHITE' | 'ABORTED'; abortedBy: string | null; civilianWord: string; undercoverWord: string; players: { id: string; name: string; originalRole: Role; survived: boolean }[] } | null
  notice: { id: number; text: string } | null
}
export interface PrivateState {
  me: { id: string; name: string; status: PlayerStatus }; role: Role | null; word: string | null
  civilianWord: string | null; undercoverWord: string | null; myVoteTargetId: string | null; executionMessage: string | null
  judge: { guessingPlayerId: string; civilianWord: string; answered: boolean } | null
  removablePlayerIds: string[]
  can: { start: boolean; assignmentReady: boolean; done: boolean; skipSpeaker: boolean; endRound: boolean; vote: boolean; ready: boolean; declareGuess: boolean; judge: boolean }
}
