const AUTH_KEY = "movieStoreUser";

const getUser = () => {
  try {
    return JSON.parse(sessionStorage.getItem(AUTH_KEY));
  } catch {
    return null;
  }
};

const setUser = (user) => sessionStorage.setItem(AUTH_KEY, JSON.stringify(user));

const clearUser = () => sessionStorage.removeItem(AUTH_KEY);

const requireRole = (role, loginPath) => {
  const user = getUser();
  if (!user || user.role !== role) {
    window.location.href = loginPath;
    return null;
  }
  return user;
};
