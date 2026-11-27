const TOKEN_KEY = 'access_token';

function hasLocalStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined';
}

export const tokenUtils = {
  getToken: () => (hasLocalStorage() ? window.localStorage.getItem(TOKEN_KEY) : null),
  setToken: (token) => {
    if (hasLocalStorage() && token) {
      window.localStorage.setItem(TOKEN_KEY, token);
    }
  },
  removeToken: () => {
    if (hasLocalStorage()) {
      window.localStorage.removeItem(TOKEN_KEY);
    }
  },
};
