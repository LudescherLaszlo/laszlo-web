import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'
import type { PrivateState, PublicState } from '../types'

const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'
export class GameClient {
  private client: Client
  private subscriptions: StompSubscription[] = []
  constructor(sessionId: string, handlers: { publicState: (v: PublicState) => void; privateState: (v: PrivateState) => void; error: (v: { message: string }) => void; connected: () => void; reconnecting: () => void }) {
    this.client = new Client({ brokerURL: wsUrl, reconnectDelay: 1200, connectHeaders: { sessionId }, onConnect: () => { handlers.connected(); this.subscriptions = ['/topic/game', '/user/queue/private', '/user/queue/errors'].map(destination => this.client.subscribe(destination, message => this.handle(destination, message, handlers))); this.send('/app/sync') }, onWebSocketClose: handlers.reconnecting, onWebSocketError: handlers.reconnecting })
    this.client.activate()
  }
  private handle(destination: string, message: IMessage, handlers: { publicState: (v: PublicState) => void; privateState: (v: PrivateState) => void; error: (v: { message: string }) => void }) {
    const value = JSON.parse(message.body)
    if (destination === '/topic/game') handlers.publicState(value)
    else if (destination === '/user/queue/private') handlers.privateState(value)
    else handlers.error(value)
  }
  send(destination: string, body?: object) { if (this.client.connected) this.client.publish({ destination, body: JSON.stringify(body || {}) }) }
  close() { this.subscriptions.forEach(subscription => subscription.unsubscribe()); this.client.deactivate() }
}
