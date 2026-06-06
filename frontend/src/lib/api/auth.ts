import { RegisterRequest, LoginRequest, AuthResponse } from "@/types";
import apiClient from "./client";
import { AxiosResponse } from "axios";


export const register = async (
  userData: RegisterRequest
): Promise<AxiosResponse<string>> => {
  return apiClient.post<string>("/auth/register", userData);
};

export const registerOrganizer = async (
  userData: RegisterRequest
): Promise<AxiosResponse<string>> => {
  return apiClient.post<string>("/auth/register-organizer", userData);
};

export const login = async (
  userData: LoginRequest
): Promise<AxiosResponse<AuthResponse>> => {
  return apiClient.post<AuthResponse>("/auth/login", userData);
};

export const logout = async (): Promise<AxiosResponse<string>> => {
  return apiClient.post<string>("/auth/logout");
}