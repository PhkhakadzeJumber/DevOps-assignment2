"use client";

import { useAuth } from "@/context/AuthContext";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function AuthCallbackPage() {
  const router = useRouter();
  const { login } = useAuth();

  useEffect(() => {
    const completeLogin = async () => {
      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL;
        if (!apiUrl) {
          throw new Error("Missing NEXT_PUBLIC_API_URL");
        }

        const res = await fetch(`${apiUrl}/auth/refresh-token`, {
          method: "POST",
          credentials: "include",
        });

        if (!res.ok) throw new Error("Failed to refresh token");

        const data = await res.json(); // { accessToken: "..." }
        if (!data?.accessToken) throw new Error("No access token returned");

        login(data.accessToken);
        router.replace("/");
      } catch {
        router.replace("/auth/login?error=oauth_callback_failed");
      }
    };

    void completeLogin();
  }, [login, router]);

  return <p>Signing you in...</p>;
}
