import { Menu } from 'lucide-react'
import { Button } from './ui/button'
export function AppMenu({ inGame, onHowToPlay, onLeave, onEnd }: { inGame: boolean; onHowToPlay: () => void; onLeave: () => void; onEnd: () => void }) {
  return <details className="relative"><summary className="list-none"><Button variant="ghost" size="icon" aria-label="Open menu"><Menu size={20} /></Button></summary><div className="absolute right-0 z-20 mt-2 w-48 rounded-xl border bg-card p-1 shadow-lg"><button className="w-full rounded-lg p-2 text-left text-sm hover:bg-muted" onClick={onHowToPlay}>How to play</button>{inGame && <button className="w-full rounded-lg p-2 text-left text-sm hover:bg-muted" onClick={onEnd}>End game</button>}<button className="w-full rounded-lg p-2 text-left text-sm text-destructive hover:bg-muted" onClick={onLeave}>Leave game</button></div></details>
}
