package in.hajeera.FeastHaven.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import in.hajeera.FeastHaven.entity.OrderEntity;
import in.hajeera.FeastHaven.io.OrderRequest;
import in.hajeera.FeastHaven.io.OrderResponse;
import in.hajeera.FeastHaven.repository.CartRepository;
import in.hajeera.FeastHaven.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private CartRepository cartRepository;


    @Value("${razorpay_key}")
    private String RAZORPAY_KEY;
    @Value("${razorpay_secret}")
    private String RAZORPAY_SECRET;

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Override
    public OrderResponse createOrderWithPayment(OrderRequest request) {

        OrderEntity newOrder = convertToEntity(request);

        // Checking if the Razorpay keys are fake or missing
        boolean isMockPayment = isFakeKeys(RAZORPAY_KEY, RAZORPAY_SECRET);

        if (!isMockPayment) {
            // Trying real Razorpay integration
            try {
                RazorpayClient razorpayClient = new RazorpayClient(RAZORPAY_KEY, RAZORPAY_SECRET);
                JSONObject orderRequest = new JSONObject();
                orderRequest.put("amount", (int)(newOrder.getAmount() * 100)); // Amount in paise
                orderRequest.put("currency", "INR");
                orderRequest.put("payment_capture", 1);

                Order razorpayOrder = razorpayClient.orders.create(orderRequest);
                newOrder.setRazorpayOrderId(razorpayOrder.get("id"));
                newOrder.setPaymentStatus("PENDING");
            } catch (Exception e) {
                logger.error("Error during real Razorpay integration: {}", e.getMessage());
            }
        } else {
            // Fallback to mock payment logic when keys are fake
            newOrder.setRazorpayOrderId("mock_" + UUID.randomUUID().toString().substring(0, 14));
            newOrder.setPaymentStatus("MOCK_PENDING");
        }

        String loggedInUserId = userService.findByUserId();
        newOrder.setUserId(loggedInUserId);
        newOrder = orderRepository.save(newOrder);

        return convertToResponse(newOrder);
    }

    @Override
    public void verifyPayment(Map<String, String> paymentData, String status) {
        String razorpayOrderId = paymentData.get("razorpay_order_id");
        OrderEntity existingOrder = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        // This is only for MOCK Razorpay
        if (isFakeKeys(RAZORPAY_KEY, RAZORPAY_SECRET)) {
            existingOrder.setPaymentStatus("PAID");
            existingOrder.setRazorpaySignature(paymentData.getOrDefault("razorpay_signature","test signature"));
            existingOrder.setRazorpayPaymentId(paymentData.getOrDefault("razorpay_payment_id","test payment id"));
        } else {
            // Real Razorpay integration
            existingOrder.setPaymentStatus(status);
            existingOrder.setRazorpaySignature(paymentData.get("razorpay_signature"));
            existingOrder.setRazorpayPaymentId(paymentData.get("razorpay_payment_id"));
        }

        orderRepository.save(existingOrder);
        if("paid".equalsIgnoreCase(status)){
            cartRepository.deleteByUserId(existingOrder.getUserId());
        }
    }

    @Override
    public void removeOrder(String orderId) {
        orderRepository.deleteById(orderId);
    }

    @Override
    public List<OrderResponse> getOrdersOfAllUsers() {
        List<OrderEntity> list = orderRepository.findAll();
        return list.stream().map(entity -> convertToResponse(entity)).collect(Collectors.toList());
    }

    @Override
    public void updateOrderStatus(String orderId, String status) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        entity.setOrderStatus(status);
        orderRepository.save(entity);
    }

    @Override
    public List<OrderResponse> getUserOrders() {
        String loggedInUserId = userService.findByUserId();
        List<OrderEntity> list = orderRepository.findByUserId(loggedInUserId);
        return list.stream().map(entity -> convertToResponse(entity)).collect(Collectors.toList());
    }

    // Helper function to check if the keys are fake or missing
    private boolean isFakeKeys(String razorpayKey, String razorpaySecret) {
        return razorpayKey == null || razorpaySecret == null ||
                razorpayKey.trim().equals("fake-key") || razorpaySecret.trim().equals("fake-secret");
    }


    private OrderResponse convertToResponse(OrderEntity newOrder) {
        return OrderResponse.builder()
                .id(newOrder.getId())
                .amount(newOrder.getAmount())
                .userAddress(newOrder.getUserAddress())
                .userId(newOrder.getUserId())
                .razorpayOrderId(newOrder.getRazorpayOrderId())
                .paymentStatus(newOrder.getPaymentStatus())
                .orderStatus(newOrder.getOrderStatus())
                .email(newOrder.getEmail())
                .phoneNumber(newOrder.getPhoneNumber())
                .orderedItems(newOrder.getOrderedItems())
                .build();
    }

    private OrderEntity convertToEntity(OrderRequest request) {
        return OrderEntity.builder()
                .userAddress(request.getUserAddress())
                .amount(request.getAmount())
                .orderedItems(request.getOrderedItems())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .orderStatus(request.getOrderStatus())
                .build();
    }
}
