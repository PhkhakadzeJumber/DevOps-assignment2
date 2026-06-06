'use client';
import { useState } from 'react';
import { FaUser, FaLock } from 'react-icons/fa';
import { MdEmail } from 'react-icons/md';
import { isAxiosError } from 'axios';
import { registerOrganizer } from '@/lib/api/auth';
import SubmitButton from '@/components/auth/SubmitButton';

export default function RegisterPage() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    if (password.length < 8) {
      setError("Password must be at least 8 characters");
      return;
    }
    if(RegExp("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}$").test(password) === false) {
      setError("Password must contain uppercase, lowercase, and a digit");
      return
    }

    try {
      await registerOrganizer({ username, email, password });
      alert("verification email sent, please verify before logging in");
    } catch (err: unknown) {
      if (isAxiosError<{ message?: string }>(err)) {
        setError(err.response?.data?.message ?? "Registration failed");
        return;
      }
      setError("Registration failed");
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center">
      <form 
        onSubmit={handleSubmit} 
        className="bg-white p-8 rounded-lg shadow-lg w-full max-w-sm"
      >
        <h2 className="text-2xl font-bold mb-6 text-center">Register as organizer</h2>

        {error && <p className="text-red-500 mb-4 text-sm">{error}</p>}

        {/* Username */}
        <div className="relative mb-4">
          <FaUser className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={e => setUsername(e.target.value)}
            className="w-full pl-10 px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-400"
            required
          />
        </div>

        {/* Email */}
        <div className="relative mb-4">
          <MdEmail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={e => setEmail(e.target.value)}
            className="w-full pl-10 px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-400"
            required
          />
        </div>

        {/* Password */}
        <div className="relative mb-4">
          <FaLock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={e => setPassword(e.target.value)}
            className="w-full pl-10 px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-400"
            required
          />
        </div>

        {/* Confirm Password */}
        <div className="relative mb-6">
          <FaLock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
          <input
            type="password"
            placeholder="Confirm Password"
            value={confirmPassword}
            onChange={e => setConfirmPassword(e.target.value)}
            className="w-full pl-10 px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-400"
            required
          />
        </div>

        <SubmitButton str="Register" />
      </form>
    </div>
  );
}
