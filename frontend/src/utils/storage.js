export const storage = {
  get: (key) => window.localStorage.getItem(key),
  set: (key, val) => window.localStorage.setItem(key, val),
  remove: (key) => window.localStorage.removeItem(key),
};
