"use client";
import { useAuth } from "@/context/AuthContext";
import { logout } from "@/lib/api/auth";
import { useRouter } from "next/navigation";
import React from "react";

export default function Logout() {
  const router = useRouter();
  const { logout: contextLogout } = useAuth();
  
  const handleLogout = async () => {
    try {
      await logout();
    } finally {
      contextLogout();
      router.push("/");
    }
  };

  return (
    <button
      type="button"
      onClick={handleLogout}
      className="w-full p-2 text-gray-800 rounded-lg hover:border-2 border-red-800 transition"
    >
      logout
    </button>
  );
}
