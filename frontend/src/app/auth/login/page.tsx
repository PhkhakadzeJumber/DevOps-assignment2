'use client';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { MdEmail } from 'react-icons/md';
import { FaLock } from 'react-icons/fa';
import { isAxiosError } from 'axios';
import { login } from '@/lib/api/auth';
import GoogleLoginButton from '@/components/auth/GoogleLoginButton';
import { useAuth } from '@/context/AuthContext';
import SubmitButton from '@/components/auth/SubmitButton';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login: contextLogin } = useAuth();
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    try {
      const res = await login({ email, password });

      contextLogin(res.data.accessToken);

      router.push("/");
    } catch (err: unknown) {
      if (isAxiosError<{ message?: string }>(err)) {
        setError(err.response?.data?.message ?? "Login failed");
        return;
      }
      setError("Login failed");
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-8 rounded-lg shadow-lg w-full max-w-sm"
      >
        <h2 className="text-2xl font-bold mb-6 text-center">Login</h2>

        {error && <p className="text-red-500 mb-4 text-sm">{error}</p>}

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
        <div className="relative mb-6">
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
     
        <SubmitButton str="Login" /> 
        <GoogleLoginButton />
      </form>
    </div>
  );
}
