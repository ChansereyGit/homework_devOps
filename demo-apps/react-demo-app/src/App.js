import React from 'react';
import './App.css';

function App() {
  const buildInfo = {
    appName: 'React Demo App',
    version: '1.0.0',
    environment: process.env.NODE_ENV || 'development',
    buildTime: new Date().toLocaleString()
  };

  return (
    <div className="App">
      <header className="App-header">
        <div className="container">
          <h1>🚀 Jenkins CI/CD Pipeline Demo</h1>
          <p className="subtitle">ReactJS Application with Docker</p>
          
          <div className="build-info">
            <h2>📦 Build Information</h2>
            <div className="info-grid">
              <div className="info-item">
                <span className="label">Application:</span>
                <span className="value">{buildInfo.appName}</span>
              </div>
              <div className="info-item">
                <span className="label">Version:</span>
                <span className="value">{buildInfo.version}</span>
              </div>
              <div className="info-item">
                <span className="label">Environment:</span>
                <span className="value">{buildInfo.environment}</span>
              </div>
              <div className="info-item">
                <span className="label">Build Time:</span>
                <span className="value">{buildInfo.buildTime}</span>
              </div>
            </div>
          </div>

          <div className="features">
            <h2>✨ Features</h2>
            <ul>
              <li>✅ Jenkins Shared Library Integration</li>
              <li>✅ Multi-stage Docker Build</li>
              <li>✅ SonarQube Code Quality</li>
              <li>✅ Automated Testing</li>
              <li>✅ Docker Hub Push</li>
              <li>✅ Container Deployment</li>
              <li>✅ Telegram Notifications</li>
            </ul>
          </div>

          <div className="status">
            <p>✅ Application is running successfully!</p>
          </div>
        </div>
      </header>
    </div>
  );
}

export default App;
