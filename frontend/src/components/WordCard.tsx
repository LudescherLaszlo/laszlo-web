import { useState } from 'react'
import { Button } from './ui/button'
import type { PrivateState } from '../types'

export function WordCard({ privateState, onGuess }: { privateState: PrivateState; onGuess: () => void }) {
  const [revealed, setRevealed] = useState(false)
  return <div className="rounded-2xl border bg-card p-6 text-center shadow-sm">
    <button className="min-h-32 w-full rounded-xl bg-muted p-5 text-lg font-semibold" onClick={() => setRevealed(value => !value)}>
      {revealed ? privateState.role === 'MR_WHITE' ? <>You are Mr. White.<br /><span className="text-sm font-normal">You have no word.</span></> : privateState.word : <span className="text-muted-foreground">Tap to reveal</span>}
    </button>
    {revealed && privateState.role === 'MR_WHITE' && privateState.can.declareGuess && <Button className="mt-4 w-full" onClick={onGuess}>Guess the word</Button>}
  </div>
}
