import { useState } from 'react'
import './App.css'
import { Button } from './components/ui/button'

function App() {
  const [count, setCount] = useState(0)

  return (
    <div className="flex flex-col items-center justify-center min-h-screen gap-4 bg-zinc-50">
      <h1 className="text-3xl font-bold">Farkas Ors Online Webshop</h1>
      <p className="text-zinc-500">-berelheto gravirozogep-</p>
      
      <Button variant="default" size="lg">
        Click Me Baby One More Time
      </Button>
    </div>
  )
}

export default App
