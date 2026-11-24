export function toFormBody(data: Record<string, unknown>): string {
  return Object.entries(data)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => {
      if (Array.isArray(value)) {
        return value.map((item, index) => `${encodeURIComponent(key)}[${index}]=${encodeURIComponent(String(item))}`).join('&')
      }
      return `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`
    })
    .join('&')
}
