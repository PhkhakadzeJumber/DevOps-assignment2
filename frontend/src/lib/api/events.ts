import apiClient from "./client";
import {
  Event,
  EventRequest,
  EventUpdateRequest,
  MessageResponse,
  TicketTypeRequest,
  TicketTypeResponse,
  TicketTypeUpdateRequest,
} from "@/types";

export const getEvents = async (): Promise<Event[]> => {
  const response = await apiClient.get<Event[]>(`/events`);
  return response.data;
};

export const getEventById = async (eventId: string): Promise<Event> => {
  const response = await apiClient.get<Event>(`/events/${eventId}`);
  return response.data;
};

export const getMyEvents = async (): Promise<Event[]> => {
  const response = await apiClient.get<Event[]>(`/events/my`);
  return response.data;
};

export const createEvent = async (eventData: EventRequest): Promise<Event> => {
  const response = await apiClient.post<Event>(`/events`, eventData);
  return response.data;
};

export const updateEvent = async (
  eventId: string,
  eventData: EventUpdateRequest
): Promise<Event> => {
  const response = await apiClient.patch<Event>(`/events/${eventId}`, eventData);
  return response.data;
};

export const deleteEvent = async (eventId: string): Promise<MessageResponse> => {
  const response = await apiClient.delete<MessageResponse>(`/events/${eventId}`);
  return response.data;
};

export const addTicketType = async (
  eventId: string,
  ticketTypeData: TicketTypeRequest
): Promise<TicketTypeResponse> => {
  const response = await apiClient.post<TicketTypeResponse>(
    `/events/${eventId}/ticket-type`,
    ticketTypeData
  );
  return response.data;
};

export const getEventTicketTypes = async (
  eventId: string
): Promise<TicketTypeResponse[]> => {
  const response = await apiClient.get<TicketTypeResponse[]>(
    `/events/${eventId}/ticket-types`
  );
  return response.data;
};

export const updateEventTicketType = async (
  eventId: string,
  ticketTypeId: number,
  payload: TicketTypeUpdateRequest
): Promise<TicketTypeResponse> => {
  const response = await apiClient.patch<TicketTypeResponse>(
    `/events/${eventId}/ticket-types/${ticketTypeId}`,
    payload
  );
  return response.data;
};

export const deleteEventTicketType = async (
  eventId: string,
  ticketTypeId: number
): Promise<MessageResponse> => {
  const response = await apiClient.delete<MessageResponse>(
    `/events/${eventId}/ticket-types/${ticketTypeId}`
  );
  return response.data;
};

export const uploadEventThumbnail = async (
  eventId: string,
  imageFile: File
): Promise<MessageResponse> => {
  const formData = new FormData();
  formData.append("image", imageFile);

  const response = await apiClient.post<MessageResponse>(
    `/events/${eventId}/thumbnail`,
    formData
  );
  return response.data;
};
