export const AUTH = {
  LOGIN: '/auth/login',
  LOGOUT: '/auth/logout',
  REFRESH: '/auth/refresh',
} as const;

export const RESOURCES = {
  SEARCH: '/resources/search',
  BASE: '/resources',
  CHECK_DUPLICATE: '/resources/check-duplicate',
  FIND_SIMILAR: '/resources/find-similar',
} as const;

export const DATAIO = {
  IMPORT: '/dataio/import',
  EXPORT_PARTIAL: '/dataio/export/partial',
  EXPORT_ALL: '/dataio/export/all',
} as const;

export const DEPLOY = {
  BASE: '/deploy',
  HISTORY: '/deploy/history',
} as const;

export const USERS = {
  BASE: '/users',
} as const;

export const HISTORY = {
  WORK_LOGS: '/history/work-logs',
} as const;
