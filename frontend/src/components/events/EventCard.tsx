import React from "react";
import Image from "next/image";
import Link from "next/link";
import { formatDate } from "@/lib/formatters";
import { Event } from "@/types";

interface EventCardProps {
  event: Event;
};

export default function EventCard({ event }: EventCardProps) {
  const imageUrl = event.thumbnailUrl
    ? `${process.env.NEXT_PUBLIC_BASE_URL}${event.thumbnailUrl}`
    : undefined;

  return (
    <div className="rounded-lg p-4 flex flex-col">
      <Link
        href={`/events/${event.id}`}
        className="mt-2 text-blue-500"
      >
        {imageUrl && (
          <Image
            src={imageUrl}
            alt={event.title}
            width={320}
            height={160}
            className="rounded mb-2 w-80 h-40 object-cover"
          />
        )}
        <h2 className="font-bold text-lg text-gray-600">{event.title}</h2>
        <p className="text-sm text-gray-600">
          {formatDate(event.date)}
        </p>
        <p className="text-sm text-blue-500">{event.location.name}</p>
      </Link>
    </div>
  );
}
