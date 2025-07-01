import axios from 'axios';

const apiUrl = import.meta.env.VITE_API_URL;

const axiosConf = axios.create({
  baseURL: apiUrl ?  apiUrl : 'http://localhost:8081',
  withCredentials: true
});

export default axiosConf;