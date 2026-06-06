export const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleDateString(undefined, {
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function inputDateTimeToApiLocalDateTime(inputValue: string): string {
  return inputValue.replace("T", " ");
}

export function apiDateTimeToInputDateTime(apiValue?: string): string {
  if (!apiValue) return "";

  const normalized = apiValue.replace(" ", "T");

  if (normalized.length >= 16) {
    return normalized.slice(0, 16);
  }

  return normalized;
}
