# React Demo App

Simple React application for testing Jenkins CI/CD pipeline with shared library.

## Features

- React 18
- Modern UI with gradient background
- Build information display
- Docker multi-stage build
- Jenkins pipeline integration

## Local Development

```bash
npm install
npm start
```

## Build

```bash
npm run build
```

## Test

```bash
npm test
```

## Docker Build

```bash
docker build -t chanserey/react-demo-app:latest .
```

## Jenkins Pipeline

This project includes a Jenkinsfile that uses the shared library for:
- Building Docker images
- Pushing to Docker Hub
- Deploying containers
- Telegram notifications
