import { createBrowserRouter } from 'react-router-dom'
import HomePage from '../pages/HomePage'
import MrWhiteMainPage from '../pages/MrWhiteMainPage'
import NotFoundPage from '../pages/NotFoundPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <HomePage />,
  },
  {
    path: '/mr-white',
    element: <MrWhiteMainPage />,
  },
  {
    path: '*',
    element: <NotFoundPage />,
  },
])
