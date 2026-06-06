"use client";
import React from "react";
import { FaGoogle } from "react-icons/fa";

export default function GoogleLoginButton() {
  const handleLogin = () => {
    window.location.href = `${process.env.NEXT_PUBLIC_BASE_URL}/oauth2/authorization/google`;
  };

  return (
    <div className="relative">
      <FaGoogle className="absolute left-3 top-1/2 transform -translate-y-1/2 text-white"></FaGoogle>
      <button
        type="button"
        onClick={handleLogin}
        className="w-full py-2 bg-SecondaryDark text-white rounded-lg hover:bg-PrimaryDark transition"
      >
        Sign in with Google
      </button>
    </div>
  );
}