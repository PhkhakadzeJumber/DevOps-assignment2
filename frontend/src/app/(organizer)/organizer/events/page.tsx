"use client";

import { getMyEvents } from "@/lib/api/events";
import { Event } from "@/types";
import Link from "next/link";
import { useEffect, useState } from "react";


export default function OrganizerEventsPage() {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setLoading(true);
        const myEvents = await getMyEvents();
        setEvents(myEvents);
      } catch {
        setError("Failed to fetch your events.");
      } finally {
        setLoading(false);
      }
    };

    void fetchEvents();
  }, []);

  if (loading) {
    return <p className="mt-8 text-center">Loading your events...</p>;
  }

  if (error) {
    return <p className="mt-8 text-center text-red-500">{error}</p>;
  }

  return (
    <section className="container mx-auto px-4 py-8">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">My Events</h1>
        <Link
          href="/organizer/events/new"
          className="rounded bg-blue-600 px-4 py-2 text-white"
        >
          Create Event
        </Link>
      </div>

      {events.length === 0 ? (
        <p className="text-red-500">You do not have any events yet.</p>
      ) : (
        <div className="space-y-3">
          {events.map((event) => (
            <div
              key={event.id}
              className="flex flex-col gap-2 rounded border p-4 md:flex-row md:items-center md:justify-between"
            >
              <div>
                <h2 className="text-lg font-semibold">{event.title}</h2>
                <p className="text-sm text-gray-600">
                  {new Date(event.date).toLocaleString()}
                </p>
              </div>
              <div className="flex gap-3">
                <Link
                  href={`/events/${event.id}`}
                  className="rounded border px-3 py-1"
                >
                  Public View
                </Link>
                <Link
                  href={`/organizer/events/${event.id}`}
                  className="rounded bg-gray-900 px-3 py-1 text-white"
                >
                  Manage
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
