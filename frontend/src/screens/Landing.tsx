import { useState } from 'react'
import type { FormEvent } from 'react'
import { Button } from '../components/ui/button'
import { joinGame } from '../api/join'
export function Landing({ onJoined }: { onJoined: (sessionId: string) => void }) {
  const [name, setName] = useState(''); const [error, setError] = useState(''); const [loading, setLoading] = useState(false)
  async function submit(event: FormEvent) { event.preventDefault(); setError(''); setLoading(true); try { const result = await joinGame(name.trim()); localStorage.setItem('mrwhite.sessionId', result.sessionId); onJoined(result.sessionId) } catch (reason) { setError(reason instanceof Error ? reason.message : 'Unable to join the game.') } finally { setLoading(false) } }
  return <main className="grid min-h-screen place-items-center p-5"><div className="w-full max-w-sm"><div className="mb-10 text-center"><div className="mx-auto mb-5 grid h-20 w-20 place-items-center rounded-3xl bg-primary text-4xl text-primary-foreground shadow-lg">?</div><h1 className="text-4xl font-black tracking-tight">Mr. White</h1><p className="mt-2 text-muted-foreground">A party game of clues and deception</p></div><form onSubmit={submit} className="rounded-2xl border bg-card p-5 shadow-sm"><label className="text-sm font-medium" htmlFor="name">Your name</label><input id="name" autoFocus maxLength={20} value={name} onChange={event => setName(event.target.value)} className="mt-2 h-11 w-full rounded-lg border bg-background px-3 outline-none focus:ring-2 focus:ring-ring" placeholder="Enter your name" />{error && <p className="mt-2 text-sm text-destructive">{error}</p>}<Button type="submit" className="mt-4 h-11 w-full" disabled={!name.trim() || loading}>{loading ? 'Joining...' : 'Join game'}</Button></form></div></main>
}
