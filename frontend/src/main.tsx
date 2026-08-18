import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { App } from './App';
import { ViewerProvider } from './ViewerContext';
import './styles.css';

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <BrowserRouter>
      <ViewerProvider>
        <App/>
      </ViewerProvider>
    </BrowserRouter>
  </React.StrictMode>
);
