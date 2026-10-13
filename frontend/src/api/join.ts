const base = import.meta.env.VITE_API_BASE || 'http://localhost:8080'
export async function joinGame(name: string): Promise<{ sessionId: string; playerId: string }> {
  const response = await fetch(`${base}/api/join`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ name }) })
  const payload = await response.json().catch(() => ({ message: 'Unable to join the game.' }))
  if (!response.ok) throw new Error(payload.message || 'Unable to join the game.')
  return payload
}
