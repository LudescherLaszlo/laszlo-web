import { Button } from './ui/button'
export function ConfirmDialog({ open, title, children, onConfirm, onCancel }: { open: boolean; title: string; children: React.ReactNode; onConfirm: () => void; onCancel: () => void }) {
  if (!open) return null
  return <div className="fixed inset-0 z-50 grid place-items-center bg-black/50 p-4"><div className="w-full max-w-sm rounded-2xl border bg-card p-5 shadow-xl"><h2 className="text-lg font-semibold">{title}</h2><div className="mt-2 text-sm text-muted-foreground">{children}</div><div className="mt-5 flex justify-end gap-2"><Button variant="ghost" onClick={onCancel}>Cancel</Button><Button onClick={onConfirm}>Confirm</Button></div></div></div>
}
