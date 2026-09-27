const API = axios.create({
    baseURL: "/api",
});

const genreApi = {
    list() {
        return API.get("/genres");
    },
    detail(id) {
        return API.get(`/genres/${id}/movies`);
    },
};

const movieApi = {
    list() {
        return API.get("/movies");
    },
    detail(id) {
        return API.get(`/movies/${id}`);
    },
    create(data) {
        return API.post("/movies", data);
    },
    update(id, data) {
        return API.put(`/movies/${id}`, data);
    },
    remove(id) {
        return API.delete(`/movies/${id}`);
    },
};
