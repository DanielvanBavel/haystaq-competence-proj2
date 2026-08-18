import { createContext, ReactNode, useContext, useEffect, useState } from 'react';
import { api } from './api';
import { Viewer } from './types';

interface ViewerState {
  viewers: Viewer[];
  current: Viewer | null;
  select: (id: string) => void;
}

const Context = createContext<ViewerState>({ viewers: [], current: null, select: () => undefined });

export function ViewerProvider({ children }: { children: ReactNode }) {
  const [viewers, setViewers] = useState<Viewer[]>([]);
  const [currentId, setCurrentId] = useState<string | null>(null);

  useEffect(() => {
    api.get<Viewer[]>('/viewers')
      .then((list) => {
        setViewers(list);
        setCurrentId((existing) => existing ?? list[0]?.id ?? null);
      })
      .catch(() => setViewers([]));
  }, []);

  const current = viewers.find((viewer) => viewer.id === currentId) ?? null;

  return (
    <Context.Provider value={{ viewers, current, select: setCurrentId }}>
      {children}
    </Context.Provider>
  );
}

export function useViewer() {
  return useContext(Context);
}
