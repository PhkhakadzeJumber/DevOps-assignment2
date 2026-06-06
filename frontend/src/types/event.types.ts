export interface Event {
  id: number;
  title: string;
  description: string;
  createdAt: string;
  date: string;
  thumbnailUrl?: string;
  organizer: {
    id: number;
    username: string;
    email: string;
    phoneNumber?: string | null;
    role: string;
    createdAt: string;
  };
  categories: { name: string }[];
  location: {
    name: string;
    address: string;
    city: string;
    latitude: number;
    longitude: number;
  };
}

export interface CategoryDto {
  name: string;
}

export interface LocationDto {
  name: string;
  address: string;
  city: string;
  latitude: number;
  longitude: number;
}

export interface EventRequest {
  title: string;
  description: string;
  date?: string;
  categories?: CategoryDto[];
  location: LocationDto;
}

export interface EventUpdateRequest {
  title?: string;
  description?: string;
  date?: string;
  categories?: CategoryDto[];
  location?: LocationDto;
}

export interface TicketTypeRequest {
  name: string;
  description: string;
  price: number;
  quantityTotal: number;
  maxPurchase: number;
  saleStartTime: string;
  saleEndTime: string;
}

export interface TicketTypeUpdateRequest {
  name?: string;
  description?: string;
  price?: number;
  quantityTotal?: number;
  maxPurchase?: number;
  saleStartTime?: string;
  saleEndTime?: string;
}

export interface TicketTypeResponse {
  id: number;
  name: string;
  description: string;
  price: number;
  quantityTotal: number;
  quantityReserved: number;
  quantitySold: number;
  maxPurchase: number;
  availableQuantity: number;
  saleStartTime: string;
  saleEndTime: string;
}

export interface MessageResponse {
  message: string;
}
