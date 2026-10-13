import { Button } from '../components/ui/button'
import { useNavigate } from 'react-router-dom'

function HomePage() {
  const navigate = useNavigate()

  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-zinc-50">
      <h1 className="text-3xl font-bold">Farkas Ors Online Webshop</h1>
      <p className="text-zinc-500">sunshine and unicorns</p>

      <Button
        variant="default"
        size="lg"
        onClick={() => navigate('/mr-white')}
      >
        Click Me Baby One More Time
      </Button>
    </div>
  )
}

export default HomePage
