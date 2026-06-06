"use client";

import Image from "next/image";
import Link from "next/link";
import {
  addTicketType,
  deleteEvent,
  deleteEventTicketType,
  getEventById,
  getEventTicketTypes,
  updateEvent,
  updateEventTicketType,
  uploadEventThumbnail,
} from "@/lib/api/events";
import { Event, EventUpdateRequest, TicketTypeRequest, TicketTypeResponse } from "@/types";
import { FormEvent, useEffect, useRef, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { apiDateTimeToInputDateTime, inputDateTimeToApiLocalDateTime } from "@/lib/formatters";

type EditableEventState = {
  title: string;
  description: string;
  date: string;
  categories: string;
  locationName: string;
  address: string;
  city: string;
  latitude: string;
  longitude: string;
};

type TicketTypeFormState = {
  name: string;
  description: string;
  price: string;
  quantityTotal: string;
  maxPurchase: string;
  saleStartTime: string;
  saleEndTime: string;
};

const defaultEditableEventState: EditableEventState = {
  title: "",
  description: "",
  date: "",
  categories: "",
  locationName: "",
  address: "",
  city: "",
  latitude: "",
  longitude: "",
};

const defaultTicketTypeState: TicketTypeFormState = {
  name: "",
  description: "",
  price: "",
  quantityTotal: "",
  maxPurchase: "",
  saleStartTime: "",
  saleEndTime: "",
};

function toEditableState(event: Event): EditableEventState {
  return {
    title: event.title,
    description: event.description,
    date: apiDateTimeToInputDateTime(event.date),
    categories: event.categories.map((c) => c.name).join(", "),
    locationName: event.location.name,
    address: event.location.address,
    city: event.location.city,
    latitude: String(event.location.latitude),
    longitude: String(event.location.longitude),
  };
}

function parseCategories(raw: string): { name: string }[] {
  return raw
    .split(",")
    .map((name) => name.trim())
    .filter(Boolean)
    .map((name) => ({ name }));
}

function ticketTypeToForm(ticketType: TicketTypeResponse): TicketTypeFormState {
  return {
    name: ticketType.name,
    description: ticketType.description,
    price: String(ticketType.price),
    quantityTotal: String(ticketType.quantityTotal),
    maxPurchase: String(ticketType.maxPurchase),
    saleStartTime: apiDateTimeToInputDateTime(ticketType.saleStartTime),
    saleEndTime: apiDateTimeToInputDateTime(ticketType.saleEndTime),
  };
}

function ticketFormToPayload(state: TicketTypeFormState): TicketTypeRequest {
  return {
    name: state.name.trim(),
    description: state.description.trim(),
    price: Number(state.price),
    quantityTotal: Number(state.quantityTotal),
    maxPurchase: Number(state.maxPurchase),
    saleStartTime: inputDateTimeToApiLocalDateTime(state.saleStartTime),
    saleEndTime: inputDateTimeToApiLocalDateTime(state.saleEndTime),
  };
}

export default function OrganizerEventManagePage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const eventId = params.id;
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const [event, setEvent] = useState<Event | null>(null);
  const [editable, setEditable] = useState<EditableEventState>(defaultEditableEventState);
  const [ticketTypes, setTicketTypes] = useState<TicketTypeResponse[]>([]);
  const [ticketTypeFormById, setTicketTypeFormById] = useState<Record<number, TicketTypeFormState>>(
    {}
  );
  const [newTicketTypeState, setNewTicketTypeState] = useState<TicketTypeFormState>(defaultTicketTypeState);
  const [showAddTicketTypeForm, setShowAddTicketTypeForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploadingImage, setUploadingImage] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError("");
        const [eventResponse, ticketTypeResponse] = await Promise.all([
          getEventById(eventId),
          getEventTicketTypes(eventId),
        ]);
        setEvent(eventResponse);
        setEditable(toEditableState(eventResponse));
        setTicketTypes(ticketTypeResponse);
        setTicketTypeFormById(
          ticketTypeResponse.reduce<Record<number, TicketTypeFormState>>((acc, ticketType) => {
            acc[ticketType.id] = ticketTypeToForm(ticketType);
            return acc;
          }, {})
        );
      } catch {
        setError("Failed to fetch event.");
      } finally {
        setLoading(false);
      }
    };

    void fetchData();
  }, [eventId]);

  const refreshEventAndTicketTypes = async () => {
    const [eventResponse, ticketTypeResponse] = await Promise.all([
      getEventById(eventId),
      getEventTicketTypes(eventId),
    ]);
    setEvent(eventResponse);
    setEditable(toEditableState(eventResponse));
    setTicketTypes(ticketTypeResponse);
    setTicketTypeFormById(
      ticketTypeResponse.reduce<Record<number, TicketTypeFormState>>((acc, ticketType) => {
        acc[ticketType.id] = ticketTypeToForm(ticketType);
        return acc;
      }, {})
    );
  };

  const imageUrl = event?.thumbnailUrl
    ? `${process.env.NEXT_PUBLIC_BASE_URL}${event.thumbnailUrl}`
    : undefined;

  const locationUrl =
    event &&
    `https://www.google.com/maps/search/?api=1&query=${editable.latitude || event.location.latitude},${
      editable.longitude || event.location.longitude
    }`;

  const handleSaveEvent = async () => {
    if (!event) return;

    setError("");
    setMessage("");
    setSaving(true);

    try {
      const payload: EventUpdateRequest = {
        title: editable.title.trim(),
        description: editable.description.trim(),
        date: editable.date
          ? inputDateTimeToApiLocalDateTime(editable.date)
          : undefined,
        categories: parseCategories(editable.categories),
        location: {
          name: editable.locationName.trim(),
          address: editable.address.trim(),
          city: editable.city.trim(),
          latitude: Number(editable.latitude),
          longitude: Number(editable.longitude),
        },
      };

      await updateEvent(eventId, payload);
      await refreshEventAndTicketTypes();
      setMessage("Event updated.");
    } catch {
      setError("Failed to update event.");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    const confirmed = window.confirm("Delete this event?");
    if (!confirmed) return;

    setError("");
    await deleteEvent(eventId);
    router.push("/organizer/events");
  };

  const handleImageClick = () => {
    fileInputRef.current?.click();
  };

  const handleImagePicked = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploadingImage(true);
    setError("");
    setMessage("");
    try {
      await uploadEventThumbnail(eventId, file);
      await refreshEventAndTicketTypes();
      setMessage("Banner updated.");
    } catch {
      setError("Failed to upload banner.");
    } finally {
      setUploadingImage(false);
      e.target.value = "";
    }
  };

  const handleNewTicketTypeSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setMessage("");

    try {
      const payload = ticketFormToPayload(newTicketTypeState);
      await addTicketType(eventId, payload);
      setNewTicketTypeState(defaultTicketTypeState);
      setShowAddTicketTypeForm(false);
      await refreshEventAndTicketTypes();
      setMessage("Ticket type added.");
    } catch {
      setError("Failed to add ticket type.");
    }
  };

  const handleTicketTypeChange = (
    ticketTypeId: number,
    key: keyof TicketTypeFormState,
    value: string
  ) => {
    setTicketTypeFormById((prev) => ({
      ...prev,
      [ticketTypeId]: {
        ...prev[ticketTypeId],
        [key]: value,
      },
    }));
  };

  const handleUpdateTicketType = async (ticketTypeId: number) => {
    const state = ticketTypeFormById[ticketTypeId];
    if (!state) return;

    setError("");
    setMessage("");
    try {
      const payload = ticketFormToPayload(state);
      await updateEventTicketType(eventId, ticketTypeId, payload);
      await refreshEventAndTicketTypes();
      setMessage("Ticket type updated.");
    } catch {
      setError("Failed to update ticket type.");
    }
  };

  const handleDeleteTicketType = async (ticketTypeId: number) => {
    const confirmed = window.confirm("Delete this ticket type?");
    if (!confirmed) return;

    setError("");
    setMessage("");
    try {
      await deleteEventTicketType(eventId, ticketTypeId);
      await refreshEventAndTicketTypes();
      setMessage("Ticket type deleted.");
    } catch {
      setError("Failed to delete ticket type.");
    }
  };

  if (loading) {
    return <p className="mt-8 text-center">Loading event...</p>;
  }

  if (!event) {
    return <p className="mt-8 text-center text-red-500">Event not found.</p>;
  }

  return (
    <section className="max-w-4/5 mx-auto p-4 space-y-6">
      <div className="flex justify-between items-center">
        <Link href="/organizer/events" className="text-blue-600">
          Back to My Events
        </Link>
        <button
          type="button"
          onClick={handleDelete}
          className="rounded bg-red-600 px-4 py-2 text-white"
        >
          Delete Event
        </button>
      </div>

      {message && <p className="text-green-700">{message}</p>}
      {error && <p className="text-red-600">{error}</p>}

      <div className="relative">
        {imageUrl ? (
          <Image
            src={imageUrl}
            alt={editable.title || event.title}
            width={1200}
            height={400}
            className="w-full max-h-64 object-cover rounded mb-4"
          />
        ) : (
          <div className="w-full h-64 bg-gray-200 rounded mb-4 flex items-center justify-center text-gray-600">
            No banner image
          </div>
        )}
        <button
          type="button"
          onClick={handleImageClick}
          disabled={uploadingImage}
          className="absolute top-3 right-3 rounded bg-black/70 px-3 py-2 text-white text-sm"
        >
          {uploadingImage ? "Uploading..." : "Change Banner"}
        </button>
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          className="hidden"
          onChange={handleImagePicked}
        />
      </div>

      <div className="mb-4 flex flex-col md:flex-row md:justify-between gap-6">
        <div className="md:w-4/5 md:mr-6 space-y-3">
          <input
            className="w-full text-3xl font-bold border rounded p-2"
            value={editable.title}
            onChange={(e) => setEditable((prev) => ({ ...prev, title: e.target.value }))}
          />
          <textarea
            className="w-full min-h-32 border rounded p-2"
            value={editable.description}
            onChange={(e) => setEditable((prev) => ({ ...prev, description: e.target.value }))}
          />
          <input
            className="w-full border rounded p-2"
            placeholder="Categories (comma separated)"
            value={editable.categories}
            onChange={(e) => setEditable((prev) => ({ ...prev, categories: e.target.value }))}
          />
        </div>

        <div className="md:w-1/5 text-right flex flex-col gap-2">
          <input
            type="datetime-local"
            className="border rounded p-2"
            value={editable.date}
            onChange={(e) => setEditable((prev) => ({ ...prev, date: e.target.value }))}
          />
          <input
            className="border rounded p-2"
            placeholder="Location name"
            value={editable.locationName}
            onChange={(e) => setEditable((prev) => ({ ...prev, locationName: e.target.value }))}
          />
          <input
            className="border rounded p-2"
            placeholder="Address"
            value={editable.address}
            onChange={(e) => setEditable((prev) => ({ ...prev, address: e.target.value }))}
          />
          <input
            className="border rounded p-2"
            placeholder="City"
            value={editable.city}
            onChange={(e) => setEditable((prev) => ({ ...prev, city: e.target.value }))}
          />
          <input
            type="number"
            step="any"
            className="border rounded p-2"
            placeholder="Latitude"
            value={editable.latitude}
            onChange={(e) => setEditable((prev) => ({ ...prev, latitude: e.target.value }))}
          />
          <input
            type="number"
            step="any"
            className="border rounded p-2"
            placeholder="Longitude"
            value={editable.longitude}
            onChange={(e) => setEditable((prev) => ({ ...prev, longitude: e.target.value }))}
          />
          {locationUrl && (
            <a href={locationUrl} target="_blank" rel="noreferrer" className="text-blue-500 text-sm">
              Open location in maps
            </a>
          )}
          <button
            type="button"
            onClick={handleSaveEvent}
            disabled={saving}
            className="w-full mt-2 bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 disabled:opacity-60"
          >
            {saving ? "Saving..." : "Save Changes"}
          </button>
        </div>
      </div>

      <section className="space-y-4 rounded border p-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-semibold">Ticket Types</h2>
          <button
            type="button"
            onClick={() => setShowAddTicketTypeForm((prev) => !prev)}
            className="rounded bg-blue-600 px-3 py-2 text-white"
          >
            {showAddTicketTypeForm ? "Cancel" : "Add Ticket Type"}
          </button>
        </div>

        {showAddTicketTypeForm && (
          <form onSubmit={handleNewTicketTypeSubmit} className="space-y-3 rounded border border-blue-300 bg-blue-50 p-4">
            <h3 className="font-medium">New Ticket Type</h3>
            <input
              className="w-full rounded border p-2"
              placeholder="Name"
              value={newTicketTypeState.name}
              onChange={(e) =>
                setNewTicketTypeState((prev) => ({ ...prev, name: e.target.value }))
              }
              required
            />
            <textarea
              className="w-full rounded border p-2"
              placeholder="Description"
              value={newTicketTypeState.description}
              onChange={(e) =>
                setNewTicketTypeState((prev) => ({ ...prev, description: e.target.value }))
              }
              required
            />
            <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
              <input
                type="number"
                min="0"
                step="0.01"
                className="rounded border p-2"
                placeholder="Price"
                value={newTicketTypeState.price}
                onChange={(e) =>
                  setNewTicketTypeState((prev) => ({ ...prev, price: e.target.value }))
                }
                required
              />
              <input
                type="number"
                min="1"
                className="rounded border p-2"
                placeholder="Quantity total"
                value={newTicketTypeState.quantityTotal}
                onChange={(e) =>
                  setNewTicketTypeState((prev) => ({ ...prev, quantityTotal: e.target.value }))
                }
                required
              />
              <input
                type="number"
                min="1"
                className="rounded border p-2"
                placeholder="Max purchase"
                value={newTicketTypeState.maxPurchase}
                onChange={(e) =>
                  setNewTicketTypeState((prev) => ({ ...prev, maxPurchase: e.target.value }))
                }
                required
              />
            </div>
            <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
              <input
                type="datetime-local"
                className="rounded border p-2"
                value={newTicketTypeState.saleStartTime}
                onChange={(e) =>
                  setNewTicketTypeState((prev) => ({ ...prev, saleStartTime: e.target.value }))
                }
                required
              />
              <input
                type="datetime-local"
                className="rounded border p-2"
                value={newTicketTypeState.saleEndTime}
                onChange={(e) =>
                  setNewTicketTypeState((prev) => ({ ...prev, saleEndTime: e.target.value }))
                }
                required
              />
            </div>
            <button type="submit" className="rounded bg-blue-600 px-4 py-2 text-white">
              Create Ticket Type
            </button>
          </form>
        )}

        {ticketTypes.length === 0 ? (
          <p className="text-gray-600">No ticket types yet.</p>
        ) : (
          <div className="space-y-4">
            {ticketTypes.map((ticketType) => {
              const formState = ticketTypeFormById[ticketType.id];
              if (!formState) return null;

              return (
                <div key={ticketType.id} className="space-y-3 rounded border p-3">
                  <div className="flex items-center justify-between">
                    <h3 className="font-medium">{ticketType.name}</h3>
                    <button
                      type="button"
                      onClick={() => void handleDeleteTicketType(ticketType.id)}
                      className="rounded bg-red-600 px-3 py-1 text-white"
                    >
                      Delete
                    </button>
                  </div>
                  <input
                    className="w-full rounded border p-2"
                    value={formState.name}
                    onChange={(e) =>
                      handleTicketTypeChange(ticketType.id, "name", e.target.value)
                    }
                  />
                  <textarea
                    className="w-full rounded border p-2"
                    value={formState.description}
                    onChange={(e) =>
                      handleTicketTypeChange(ticketType.id, "description", e.target.value)
                    }
                  />
                  <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      className="rounded border p-2"
                      value={formState.price}
                      onChange={(e) =>
                        handleTicketTypeChange(ticketType.id, "price", e.target.value)
                      }
                    />
                    <input
                      type="number"
                      min="1"
                      className="rounded border p-2"
                      value={formState.quantityTotal}
                      onChange={(e) =>
                        handleTicketTypeChange(ticketType.id, "quantityTotal", e.target.value)
                      }
                    />
                    <input
                      type="number"
                      min="1"
                      className="rounded border p-2"
                      value={formState.maxPurchase}
                      onChange={(e) =>
                        handleTicketTypeChange(ticketType.id, "maxPurchase", e.target.value)
                      }
                    />
                  </div>
                  <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                    <input
                      type="datetime-local"
                      className="rounded border p-2"
                      value={formState.saleStartTime}
                      onChange={(e) =>
                        handleTicketTypeChange(ticketType.id, "saleStartTime", e.target.value)
                      }
                    />
                    <input
                      type="datetime-local"
                      className="rounded border p-2"
                      value={formState.saleEndTime}
                      onChange={(e) =>
                        handleTicketTypeChange(ticketType.id, "saleEndTime", e.target.value)
                      }
                    />
                  </div>
                  <button
                    type="button"
                    onClick={() => void handleUpdateTicketType(ticketType.id)}
                    className="rounded bg-gray-900 px-4 py-2 text-white"
                  >
                    Save Ticket Type
                  </button>
                </div>
              );
            })}
          </div>
        )}
      </section>
    </section>
  );
}
