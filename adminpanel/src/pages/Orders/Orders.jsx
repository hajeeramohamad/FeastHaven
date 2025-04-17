import React, { useEffect, useState } from "react";
import axios from "axios";
import { assets } from "../../assets/assets.js";

const Orders = () => {
  const [data, setData] = useState([]);

  const fetchOrders = async () => {
    try {
      const response = await axios.get("http://localhost:8080/api/orders/all");
      setData(response.data);
    } catch (error) {
      console.error("Failed to fetch orders:", error);
    }
  };

  const updateStatus = async (event, orderId) => {
    try {
      const response = await axios.patch(
        `http://localhost:8080/api/orders/status/${orderId}?status=${event.target.value}`
      );
      if (response.status === 200) {
        await fetchOrders();
      }
    } catch (error) {
      console.error("Failed to update status:", error);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  return (
    <div className="container">
      <div className="py-5 row justify-content-center">
        <div className="col-11 card">
          <table className="table table-responsive">
            <thead>
              <tr>
                <th>Image</th>
                <th>Items</th>
                <th>Amount</th>
                <th>Items Count</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {data.length === 0 ? (
                <tr>
                  <td colSpan="5" className="text-center">
                    No orders found.
                  </td>
                </tr>
              ) : (
                data.map((order, index) => (
                  <tr key={index}>
                    <td>
                      <img
                        src={assets.delivery}
                        alt="Order Item"
                        height={48}
                        width={48}
                      />
                    </td>
                    <td>
                      {Array.isArray(order.orderedItems) &&
                        order.orderedItems.map((item, index) => (
                          <span key={index}>
                            {item.name} x {item.quantity}
                            {index === order.orderedItems.length - 1
                              ? ""
                              : ", "}
                          </span>
                        ))}
                    </td>
                    <td>
                      &#x20B9;
                      {typeof order.amount === "number"
                        ? order.amount.toFixed(2)
                        : "0.00"}
                    </td>
                    <td>
                      Items:{" "}
                      {Array.isArray(order.orderedItems)
                        ? order.orderedItems.length
                        : 0}
                    </td>
                    <td className="fw-bold text-capitalize">
                      &#x25cf; {order.orderStatus}
                    </td>
                    <td>
                      <select
                        className="form-control"
                        onChange={(event) => updateStatus(event, order.id)}
                        value={order.orderStatus}
                      >
                        <option value="Food preparing">Food preparing</option>
                        <option value="Out for delivery">
                          Out for delivery
                        </option>
                        <option value="Delivered">Delivered</option>
                      </select>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Orders;
