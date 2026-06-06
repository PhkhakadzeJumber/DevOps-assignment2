"use client";

import EventEditorForm from "@/components/organizer/EventEditorForm";
import { createEvent } from "@/lib/api/events";
import { EventRequest } from "@/types";
import { useRouter } from "next/navigation";

export default function NewOrganizerEventPage() {
  const router = useRouter();

  const handleCreate = async (payload: EventRequest) => {
    const createdEvent = await createEvent(payload);
    router.push(`/organizer/events/${createdEvent.id}`);
  };

  return (
    <section className="container mx-auto max-w-3xl px-4 py-8">
      <EventEditorForm submitLabel="Create Event" onSubmit={handleCreate} />
    </section>
  );
}
