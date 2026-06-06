"use client";

import {
  CategoryDto,
  EventRequest,
  LocationDto,
} from "@/types";
import { apiDateTimeToInputDateTime, inputDateTimeToApiLocalDateTime } from "@/lib/formatters";
import { FormEvent, useMemo, useState } from "react";

type EventEditorFormProps = {
  initialValue?: Partial<EventRequest>;
  submitLabel: string;
  onSubmit: (payload: EventRequest) => Promise<void>;
};

type EventFormState = {
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

function parseCategories(raw: string): CategoryDto[] {
  return raw
    .split(",")
    .map((name) => name.trim())
    .filter(Boolean)
    .map((name) => ({ name }));
}

function toLocation(state: EventFormState): LocationDto {
  return {
    name: state.locationName.trim(),
    address: state.address.trim(),
    city: state.city.trim(),
    latitude: Number(state.latitude),
    longitude: Number(state.longitude),
  };
}

export default function EventEditorForm({
  initialValue,
  submitLabel,
  onSubmit,
}: EventEditorFormProps) {
  const [formState, setFormState] = useState<EventFormState>({
    title: initialValue?.title ?? "",
    description: initialValue?.description ?? "",
    date: apiDateTimeToInputDateTime(initialValue?.date),
    categories: (initialValue?.categories ?? []).map((c) => c.name).join(", "),
    locationName: initialValue?.location?.name ?? "",
    address: initialValue?.location?.address ?? "",
    city: initialValue?.location?.city ?? "",
    latitude:
      initialValue?.location?.latitude !== undefined
        ? String(initialValue.location.latitude)
        : "",
    longitude:
      initialValue?.location?.longitude !== undefined
        ? String(initialValue.location.longitude)
        : "",
  });
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  const locationInvalid = useMemo(() => {
    return (
      Number.isNaN(Number(formState.latitude)) ||
      Number.isNaN(Number(formState.longitude))
    );
  }, [formState.latitude, formState.longitude]);

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");

    if (locationInvalid) {
      setError("Latitude and longitude must be valid numbers.");
      return;
    }

    const payload: EventRequest = {
      title: formState.title.trim(),
      description: formState.description.trim(),
      date: formState.date
        ? inputDateTimeToApiLocalDateTime(formState.date)
        : undefined,
      categories: parseCategories(formState.categories),
      location: toLocation(formState),
    };

    try {
      setSaving(true);
      await onSubmit(payload);
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "Failed to save event.";
      setError(message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 rounded border p-4">
      <h2 className="text-xl font-semibold">{submitLabel}</h2>
      {error && <p className="text-red-600">{error}</p>}

      <input
        className="w-full rounded border p-2"
        placeholder="Event title"
        value={formState.title}
        onChange={(e) =>
          setFormState((prev) => ({ ...prev, title: e.target.value }))
        }
        required
      />

      <textarea
        className="w-full rounded border p-2"
        placeholder="Description"
        value={formState.description}
        onChange={(e) =>
          setFormState((prev) => ({ ...prev, description: e.target.value }))
        }
        required
      />

      <input
        type="datetime-local"
        className="w-full rounded border p-2"
        value={formState.date}
        onChange={(e) =>
          setFormState((prev) => ({ ...prev, date: e.target.value }))
        }
      />

      <input
        className="w-full rounded border p-2"
        placeholder="Categories (comma separated)"
        value={formState.categories}
        onChange={(e) =>
          setFormState((prev) => ({ ...prev, categories: e.target.value }))
        }
      />

      <input
        className="w-full rounded border p-2"
        placeholder="Location name"
        value={formState.locationName}
        onChange={(e) =>
          setFormState((prev) => ({ ...prev, locationName: e.target.value }))
        }
        required
      />

      <input
        className="w-full rounded border p-2"
        placeholder="Address"
        value={formState.address}
        onChange={(e) =>
          setFormState((prev) => ({ ...prev, address: e.target.value }))
        }
        required
      />

      <input
        className="w-full rounded border p-2"
        placeholder="City"
        value={formState.city}
        onChange={(e) =>
          setFormState((prev) => ({ ...prev, city: e.target.value }))
        }
        required
      />

      <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
        <input
          type="number"
          step="any"
          className="w-full rounded border p-2"
          placeholder="Latitude"
          value={formState.latitude}
          onChange={(e) =>
            setFormState((prev) => ({ ...prev, latitude: e.target.value }))
          }
          required
        />

        <input
          type="number"
          step="any"
          className="w-full rounded border p-2"
          placeholder="Longitude"
          value={formState.longitude}
          onChange={(e) =>
            setFormState((prev) => ({ ...prev, longitude: e.target.value }))
          }
          required
        />
      </div>

      <button
        type="submit"
        disabled={saving}
        className="rounded bg-blue-600 px-4 py-2 text-white disabled:opacity-60"
      >
        {saving ? "Saving..." : submitLabel}
      </button>
    </form>
  );
}
