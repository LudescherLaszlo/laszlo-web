import { Circle, UserMinus } from 'lucide-react'
import { Button } from './ui/button'
import type { Player } from '../types'
export function PlayerList({ players, removable, onRemove }: { players: Player[]; removable: string[]; onRemove: (id: string) => void }) {
  return <div className="space-y-2">{players.map(player => <div key={player.id} className={`flex items-center gap-3 rounded-lg border p-3 ${player.status === 'SPECTATOR' ? 'opacity-60' : ''}`}>
    <Circle className={player.connected ? 'fill-emerald-500 text-emerald-500' : 'text-muted-foreground'} size={10} /><span className="flex-1">{player.name}</span>
    {player.role && player.status === 'SPECTATOR' && <span className="text-xs text-muted-foreground">{player.role.replace('_', ' ')}</span>}
    {removable.includes(player.id) && <Button variant="ghost" size="sm" onClick={() => onRemove(player.id)}><UserMinus size={15} /></Button>}
  </div>)}</div>
}
