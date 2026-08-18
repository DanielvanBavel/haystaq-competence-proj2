import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import { Browse } from './pages/Browse';
import { TitleDetail } from './pages/TitleDetail';
import { Diagnostics } from './pages/Diagnostics';
import { useViewer } from './ViewerContext';

export function App() {
  const { viewers, current, select } = useViewer();

  return (
    <div className="app">
      <header>
        <div className="brand">StreamForge</div>
        <nav>
          <NavLink to="/aanbod" className={({ isActive }) => (isActive ? 'tab selected' : 'tab')}>Aanbod</NavLink>
          <NavLink to="/diagnose" className={({ isActive }) => (isActive ? 'tab selected' : 'tab')}>Diagnose</NavLink>
        </nav>
        <div className="profile">
          <label>
            Profiel
            <select
              data-testid="viewer-select"
              value={current?.id ?? ''}
              onChange={(event) => select(event.target.value)}
            >
              {viewers.map((viewer) => (
                <option key={viewer.id} value={viewer.id}>
                  {viewer.displayName} ({viewer.region}, {viewer.plan})
                </option>
              ))}
            </select>
          </label>
        </div>
      </header>

      <main>
        <Routes>
          <Route path="/" element={<Navigate to="/aanbod" replace/>}/>
          <Route path="/aanbod" element={<Browse/>}/>
          <Route path="/titel/:slug" element={<TitleDetail/>}/>
          <Route path="/diagnose" element={<Diagnostics/>}/>
        </Routes>
      </main>
    </div>
  );
}
