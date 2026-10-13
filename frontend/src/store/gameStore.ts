import { create } from 'zustand'
import type { PrivateState, PublicState } from '../types'
import { GameClient } from '../ws/client'

interface GameStore { sessionId: string | null; publicState: PublicState | null; privateState: PrivateState | null; serverOffset: number; reconnecting: boolean; client: GameClient | null; setPublic: (value: PublicState) => void; connect: (sessionId: string) => void; send: (destination: string, body?: object) => void; reset: () => void }
export const useGameStore = create<GameStore>((set, get) => ({
  sessionId: localStorage.getItem('mrwhite.sessionId'), publicState: null, privateState: null, serverOffset: 0, reconnecting: false, client: null,
  setPublic: value => { const current = get().publicState; if (current && value.stateVersion < current.stateVersion) return; set({ publicState: value, serverOffset: value.serverTime - Date.now() }) },
  connect: sessionId => {
    get().client?.close()
    const client = new GameClient(sessionId, { publicState: value => { if (value.phase) get().setPublic(value) }, privateState: value => set({ privateState: value, reconnecting: false }), error: value => { if (value.message === 'INVALID_SESSION') { localStorage.removeItem('mrwhite.sessionId'); set({ sessionId: null, publicState: null, privateState: null }) } else window.dispatchEvent(new CustomEvent('mrwhite:error', { detail: value.message })) }, connected: () => set({ reconnecting: false }), reconnecting: () => set({ reconnecting: true }) })
    set({ sessionId, client })
  },
  send: (destination, body) => get().client?.send(destination, body),
  reset: () => { get().client?.close(); localStorage.removeItem('mrwhite.sessionId'); set({ sessionId: null, publicState: null, privateState: null, client: null }) },
}))
