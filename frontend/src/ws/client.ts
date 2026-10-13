import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'
import type { PrivateState, PublicState } from '../types'

const wsUrl = import.meta.env.VITE_WS_URL || `ws://${window.location.hostname}:8080/ws`
export class GameClient {
  private client: Client
  private subscriptions: StompSubscription[] = []
  private activationTimer: ReturnType<typeof setTimeout> | null = null
  private closed = false
  constructor(sessionId: string, handlers: { publicState: (v: PublicState) => void; privateState: (v: PrivateState) => void; error: (v: { message: string }) => void; connected: () => void; reconnecting: () => void }) {
    this.client = new Client({ brokerURL: wsUrl, reconnectDelay: 1200, connectHeaders: { sessionId }, onConnect: () => { if (this.closed) return; handlers.connected(); this.subscriptions = ['/topic/game', '/user/queue/private', '/user/queue/errors'].map(destination => this.client.subscribe(destination, message => this.handle(destination, message, handlers))); this.send('/app/sync') }, onWebSocketClose: () => { if (!this.closed) handlers.reconnecting() }, onWebSocketError: () => { if (!this.closed) handlers.reconnecting() } })
    this.activationTimer = setTimeout(() => { this.activationTimer = null; if (!this.closed) this.client.activate() }, 0)
  }
  private handle(destination: string, message: IMessage, handlers: { publicState: (v: PublicState) => void; privateState: (v: PrivateState) => void; error: (v: { message: string }) => void }) {
    const value = JSON.parse(message.body)
    if (destination === '/topic/game') handlers.publicState(value)
    else if (destination === '/user/queue/private') handlers.privateState(value)
    else handlers.error(value)
  }
  send(destination: string, body?: object) { if (this.client.connected) this.client.publish({ destination, body: JSON.stringify(body || {}) }) }
  close() { this.closed = true; if (this.activationTimer) clearTimeout(this.activationTimer); this.activationTimer = null; this.subscriptions.forEach(subscription => subscription.unsubscribe()); this.subscriptions = []; void this.client.deactivate() }
}
