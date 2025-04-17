import axios from "axios";
import { fetchFoodList } from "../service/foodService";
import { createContext, useState, useEffect } from "react";
import {
  addToCart,
  removeQtyFromCart,
  getCartData,
  deleteFromCart,
} from "../service/cartService";

export const StoreContext = createContext(null);

export const StoreContextprovider = (props) => {
  const [foodList, setFoodList] = useState([]);
  const [quantities, setQuantities] = useState({});
  const [token, setToken] = useState("");

  const increaseQty = async (foodId) => {
    setQuantities((prev) => ({ ...prev, [foodId]: (prev[foodId] || 0) + 1 }));
    await addToCart(foodId, token);
  };

  const decreaseQty = async (foodId) => {
    setQuantities((prev) => ({
      ...prev,
      [foodId]: prev[foodId] > 0 ? prev[foodId] - 1 : 0,
    }));
    await removeQtyFromCart(foodId, token);
  };

  const deleteItem = async (foodId) => {
    await deleteFromCart(foodId, token);
    setQuantities((prev) => {
      const updated = { ...prev };
      delete updated[foodId];
      return updated;
    });
  };

  const loadCartData = async (token) => {
    try {
      const items = await getCartData(token);
      console.log("Cart response:", items); // fixed the variable name

      if (items && typeof items === "object") {
        setQuantities(items);
      } else {
        setQuantities({});
        console.error("Cart data is missing or malformed");
      }
    } catch (error) {
      console.error("Error loading cart:", error);
      setQuantities({});
    }
  };
  const contextValue = {
    foodList,
    increaseQty,
    decreaseQty,
    deleteItem,
    quantities,
    setQuantities,
    token,
    setToken,
  };

  useEffect(() => {
    const storedToken = localStorage.getItem("token");

    if (storedToken) {
      setToken(storedToken);
      loadCartData(storedToken); // 👈 Call directly here!
    } else {
      setQuantities({});
    }

    const loadData = async () => {
      const data = await fetchFoodList();
      setFoodList(data);
    };

    loadData();
  }, []);

  useEffect(() => {
    if (token) {
      loadCartData(token);
    } else {
      setQuantities({});
    }
  }, [token]);

  return (
    <StoreContext.Provider value={contextValue}>
      {props.children}
    </StoreContext.Provider>
  );
};
