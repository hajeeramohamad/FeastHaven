import axios from "axios";

const API_url = `${import.meta.env.VITE_API_URL}/api`;

export const registerUser = async (data) => {
  try {
    const response = await axios.post(API_url + "/register", data);
    return response;
  } catch (error) {
    throw error;
  }
};

export const login = async (data) => {
  try {
    const response = await axios.post(API_url + "/login", data);
    return response;
  } catch (error) {
    throw error;
  }
};
