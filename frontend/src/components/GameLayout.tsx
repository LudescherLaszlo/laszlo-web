import { useEffect, useState } from 'react'
import { AppMenu } from './AppMenu'
import { ConnectionBanner } from './ConnectionBanner'
import { ConfirmDialog } from './ConfirmDialog'
import { HowToPlayDialog } from './HowToPlayDialog'
import { useGameStore } from '../store/gameStore'
import type { PublicState } from '../types'
export function GameLayout({ state, children, onLeave, onEnd }: { state: PublicState; children: React.ReactNode; onLeave: () => void; onEnd: () => void }) {
  const reconnecting = useGameStore(store => store.reconnecting); const [howToPlay, setHowToPlay] = useState(false); const [confirm, setConfirm] = useState<'leave' | 'end' | null>(null); const [toast, setToast] = useState<string | null>(null)
  useEffect(() => { const handler = (event: Event) => { setToast((event as CustomEvent<string>).detail) }; window.addEventListener('mrwhite:error', handler); return () => window.removeEventListener('mrwhite:error', handler) }, [])
  useEffect(() => { if (!state.notice) return; setToast(state.notice.text); const timer = window.setTimeout(() => setToast(null), 4500); return () => window.clearTimeout(timer) }, [state.notice?.id])
  return <><ConnectionBanner reconnecting={reconnecting} /><div className="mx-auto min-h-screen max-w-lg px-4 pb-8"><header className="flex h-16 items-center justify-between"><span className="font-black tracking-tight">MR. WHITE</span><AppMenu inGame={state.phase !== 'LOBBY' && state.phase !== 'VICTORY'} onHowToPlay={() => setHowToPlay(true)} onLeave={() => setConfirm('leave')} onEnd={() => setConfirm('end')} /></header><main>{children}</main></div>{toast && <div role="status" className="fixed bottom-5 left-1/2 z-40 w-[calc(100%-2rem)] max-w-sm -translate-x-1/2 rounded-xl border bg-card px-4 py-3 text-sm shadow-lg">{toast}</div>}<HowToPlayDialog open={howToPlay} onClose={() => setHowToPlay(false)} /><ConfirmDialog open={confirm !== null} title={confirm === 'end' ? 'End game?' : 'Leave game?'} onCancel={() => setConfirm(null)} onConfirm={() => { const action = confirm; setConfirm(null); if (action === 'end') onEnd(); else onLeave() }}>{confirm === 'end' ? 'Everyone will see the game as aborted.' : 'You will leave this game. This cannot be undone.'}</ConfirmDialog></>
}
