import { getEvents } from "@/lib/api/events";
import EventCard from "@/components/events/EventCard";
import { Event } from "@/types";

export default async function HomePage() {
  let events: Event[] = [];
  try {
    events = await getEvents();
  } catch (error) {
    console.error("Failed to fetch events:", error);
  }

  return (
    <main className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Upcoming Events</h1>
      {events.length === 0 ? (
        <p>No events available at the moment.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {events.map((event) => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      )}
    </main>
  );
}