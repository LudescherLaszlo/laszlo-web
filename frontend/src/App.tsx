import { useEffect, useMemo, useState } from 'react'
import { ConfirmDialog } from './components/ConfirmDialog'
import { GameLayout } from './components/GameLayout'
import { useGameStore } from './store/gameStore'
import { Landing } from './screens/Landing'
import { Lobby } from './screens/Lobby'
import { WaitingRoom } from './screens/WaitingRoom'
import { Assignment } from './screens/Assignment'
import { Discussion } from './screens/Discussion'
import { Voting } from './screens/Voting'
import { WheelScreen } from './screens/WheelScreen'
import { Execution } from './screens/Execution'
import { Reveal } from './screens/Reveal'
import { Guess } from './screens/Guess'
import { Victory } from './screens/Victory'

function App() {
  const { sessionId, publicState, privateState, serverOffset, connect, reset, send } = useGameStore()
  const [startConfirm, setStartConfirm] = useState(false)
  const [pendingRemoval, setPendingRemoval] = useState<string | null>(null)
  useEffect(() => { if (sessionId) connect(sessionId) }, [sessionId, connect])
  useEffect(() => {
    const handlePageHide = () => useGameStore.getState().client?.close()
    const handlePageShow = (event: PageTransitionEvent) => {
      if (event.persisted) {
        const currentSessionId = useGameStore.getState().sessionId
        if (currentSessionId) useGameStore.getState().connect(currentSessionId)
      }
    }
    window.addEventListener('pagehide', handlePageHide)
    window.addEventListener('pageshow', handlePageShow)
    return () => {
      window.removeEventListener('pagehide', handlePageHide)
      window.removeEventListener('pageshow', handlePageShow)
    }
  }, [])
  const names = useMemo(() => new Map(publicState?.players.map(player => [player.id, player.name]) || []), [publicState])
  if (!sessionId) return <Landing onJoined={connect} />
  if (!privateState || !publicState) return <main className="grid min-h-screen place-items-center p-5 text-sm text-muted-foreground">Connecting to the game...</main>
  const command = (destination: string, body?: object) => send(destination, body)
  const remove = (id: string) => setPendingRemoval(id)
  const onRemove = () => { if (pendingRemoval) command('/app/remove-player', { playerId: pendingRemoval }); setPendingRemoval(null) }
  const phaseScreen = () => {
    if (privateState.me.status === 'WAITING') return <WaitingRoom />
    if (privateState.me.status === 'POSTGAME') return <Victory state={publicState} onReturn={() => command('/app/return-to-lobby')} />
    if (publicState.phase === 'LOBBY') return <Lobby state={publicState} privateState={privateState} onStart={() => setStartConfirm(true)} onRemove={remove} />
    if (publicState.phase === 'ASSIGNMENT') return <Assignment state={publicState} privateState={privateState} onReady={() => command('/app/assignment-ready')} onGuess={() => command('/app/declare-guess')} />
    if (publicState.phase === 'DISCUSSION') return <Discussion key={`discussion-${publicState.round}`} state={publicState} privateState={privateState} onDone={() => command('/app/next-speaker')} onSkip={() => command('/app/skip-speaker')} onEnd={() => command('/app/end-round')} onGuess={() => command('/app/declare-guess')} />
    if (publicState.phase === 'VOTING' || publicState.phase === 'TIEBREAK') return <Voting state={publicState} privateState={privateState} onVote={targetId => command('/app/vote', { targetId })} />
    if (publicState.phase === 'WHEEL') return <WheelScreen state={publicState} privateState={privateState} offset={serverOffset} />
    if (publicState.phase === 'EXECUTION') return <Execution state={publicState} privateState={privateState} onReady={() => command('/app/ready')} />
    if (publicState.phase === 'REVEAL') return <Reveal state={publicState} offset={serverOffset} />
    if (publicState.phase === 'MR_WHITE_GUESS') return <Guess state={publicState} privateState={privateState} onVerdict={correct => command('/app/guess-verdict', { correct })} />
    return <Victory state={publicState} onReturn={() => command('/app/return-to-lobby')} />
  }
  return <GameLayout state={publicState} onLeave={() => { command('/app/leave'); reset() }} onEnd={() => command('/app/end-game')}><div className="mb-5 flex items-center gap-2 text-sm text-muted-foreground"><span className="flex-1">{privateState.me.name}</span><span>{publicState.players.length} players</span></div>{phaseScreen()}<ConfirmDialog open={startConfirm} title={`Start the game with ${publicState.players.filter(player => player.status === 'LOBBY').length} players?`} onCancel={() => setStartConfirm(false)} onConfirm={() => { setStartConfirm(false); command('/app/start', { expectedCount: publicState.players.filter(player => player.status === 'LOBBY').length }) }}>Everyone is ready to play?</ConfirmDialog><ConfirmDialog open={pendingRemoval !== null} title={`Remove ${names.get(pendingRemoval || '') || 'this player'}?`} onCancel={() => setPendingRemoval(null)} onConfirm={onRemove}>This disconnected player will be eliminated.</ConfirmDialog></GameLayout>
}

export default App
