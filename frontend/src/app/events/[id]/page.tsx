import { getEventById } from "@/lib/api/events";
import { Event } from "@/types";
import EventDetail from "@/components/events/EventDetail";

interface PageProps {
  params: Promise<{ id: string }>;
}

export default async function EventPage({ params }: PageProps) {
  let event: Event | null = null;
  const { id } = await params;

  try {
    event = await getEventById(id);
  } catch (err) {
    console.error("Failed to fetch event:", err);
  }

  if (!event) {
    return <p className="text-center mt-8 text-red-500">Event not found</p>;
  }

  return <EventDetail event={event} />;
}
