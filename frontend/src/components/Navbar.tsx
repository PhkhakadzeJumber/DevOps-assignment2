"use client";

import { useAuth } from "@/context/AuthContext";
import Link from "next/link";
import Logout from "./auth/Logout";

export default function Navbar() {
  const { isAuthInitialized, isLoggedIn, role } = useAuth();
  const isOrganizer = role === "ROLE_ORGANIZER" || role === "ORGANIZER";
  
  return (
    <nav className="text-PrimaryDark p-4 flex justify-between">
      <Link href="/"><div className="font-bold">TicketPlatform</div></Link>
      <div className="space-x-4">
        {!isAuthInitialized ? null : isLoggedIn ?
        (
          
          <div className="flex items-center space-x-4">
            {isOrganizer && <Link href="/organizer/events" className="whitespace-nowrap">My Events</Link>}
            <Logout />
          </div>  
        ) : 
        (
          <>
            <Link href="/auth/register">Register</Link>
            <Link href="/auth/login">Login</Link>
          </>
        )}
      </div>
    </nav>
  );
}
