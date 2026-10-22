import axiosClient from './api/axiosClient.js';

export const dictionaryService = {
  search: (params) => axiosClient.get('/dictionary/search', { params }),
  getKanji: (char) => axiosClient.get(`/dictionary/kanji/${char}`),
};
