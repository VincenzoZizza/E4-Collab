import axios from 'axios';

const axiosConf = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true
});

export default axiosConf;