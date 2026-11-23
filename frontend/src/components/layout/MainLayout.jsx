import { Outlet } from 'react-router-dom';
import Header from './Header.jsx';
import Footer from './Footer.jsx';

export default function MainLayout() {
  return (
    <div className="min-h-screen flex flex-col bg-background text-on-surface">
      <Header />
      <main className="flex-1 max-w-7xl w-full mx-auto px-space-lg py-space-md flex flex-col space-y-space-lg">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}
