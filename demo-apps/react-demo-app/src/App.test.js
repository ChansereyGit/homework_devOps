import { render, screen } from '@testing-library/react';
import App from './App';

test('renders Jenkins CI/CD Pipeline Demo heading', () => {
  render(<App />);
  const headingElement = screen.getByText(/Jenkins CI\/CD Pipeline Demo/i);
  expect(headingElement).toBeInTheDocument();
});

test('renders build information section', () => {
  render(<App />);
  const buildInfoElement = screen.getByText(/Build Information/i);
  expect(buildInfoElement).toBeInTheDocument();
});

test('renders features section', () => {
  render(<App />);
  const featuresElement = screen.getByText(/Features/i);
  expect(featuresElement).toBeInTheDocument();
});

test('renders success status', () => {
  render(<App />);
  const statusElement = screen.getByText(/Application is running successfully/i);
  expect(statusElement).toBeInTheDocument();
});
