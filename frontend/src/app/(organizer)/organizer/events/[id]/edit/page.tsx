"use client";

import EventEditorForm from "@/components/organizer/EventEditorForm";
import { getEventById, updateEvent } from "@/lib/api/events";
import { Event, EventRequest } from "@/types";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function OrganizerEventEditPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const eventId = params.id;

  const [event, setEvent] = useState<Event | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchEvent = async () => {
      try {
        setLoading(true);
        const eventResponse = await getEventById(eventId);
        setEvent(eventResponse);
      } catch {
        setError("Failed to fetch event.");
      } finally {
        setLoading(false);
      }
    };

    void fetchEvent();
  }, [eventId]);

  const handleUpdate = async (payload: EventRequest) => {
    await updateEvent(eventId, payload);
    router.push(`/organizer/events/${eventId}`);
  };

  if (loading) {
    return <p className="mt-8 text-center">Loading event...</p>;
  }

  if (!event) {
    return <p className="mt-8 text-center text-red-500">{error || "Event not found."}</p>;
  }

  return (
    <section className="container mx-auto max-w-3xl px-4 py-8">
      <EventEditorForm
        submitLabel="Update Event"
        initialValue={{
          title: event.title,
          description: event.description,
          date: event.date,
          categories: event.categories,
          location: event.location,
        }}
        onSubmit={handleUpdate}
      />
    </section>
  );
}
