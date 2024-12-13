package com.example.smart_agriculture_app;

import android.util.Log;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
public class MqttHelper {
    private static final String TAG = "MQTT";
    private static final String BROKER_HOST = "broker.emqx.io"; // Replace with your EMQX broker address
    private static final int BROKER_PORT = 8083; // Use 8883 for SSL
    private static final String USERNAME = "cropconnect1"; // Replace with your EMQX username
    private static final String PASSWORD = "cropconnect1"; // Replace with your EMQX password
    private static final String CLIENT_ID = "mobileappclient";
    private Mqtt5AsyncClient mqttClient;

    public MqttHelper(){
        // Initialize the MQTT client with WebSocket
        mqttClient = MqttClient.builder()
                .useMqttVersion5()
                .identifier(CLIENT_ID)
                .serverHost(BROKER_HOST)
                .serverPort(BROKER_PORT)
                .webSocketConfig() // Enable WebSocket
                .serverPath("/mqtt") // Path required for WebSocket (default for EMQX)
                .applyWebSocketConfig()
                .buildAsync();
    }

    // Connect to the MQTT broker
    public void connectToBroker(MqttConnectionListener listener) {
        mqttClient.connectWith()
                .simpleAuth()
                .username(USERNAME)
                .password(PASSWORD.getBytes()) // Convert password to bytes
                .applySimpleAuth()
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Failed to connect to broker", throwable);
                        if (listener != null) listener.onConnectionFailed(throwable);
                    } else {
                        Log.d(TAG, "Connected to EMQX broker successfully!");
                        if (listener != null) listener.onConnected();
                    }
                });
    }

    // Subscribe to a topic
    public void subscribeToTopic(String topic, MqttMessageListener listener) {
        mqttClient.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    String message = new String(publish.getPayloadAsBytes());
                    Log.d(TAG, "Received message from topic " + topic + ": " + message);

                    // Pass the message to the listener
                    if (listener != null) listener.onMessageReceived(topic, message);
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Failed to subscribe to topic: " + topic, throwable);
                    } else {
                        Log.d(TAG, "Subscribed to topic: " + topic);
                    }
                });
    }

    // Publish a message to a topic
    public void publishMessage(String topic, String message) {
        mqttClient.publishWith()
                .topic(topic)
                .payload(message.getBytes()) // Convert message to bytes
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .whenComplete((publish, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Failed to publish message to topic: " + topic, throwable);
                    } else {
                        Log.d(TAG, "Message published to topic: " + topic + ", message: " + message);
                    }
                });
    }

    // Disconnect from the broker
    public void disconnectFromBroker() {
        mqttClient.disconnect()
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Failed to disconnect from broker", throwable);
                    } else {
                        Log.d(TAG, "Disconnected from broker successfully");
                    }
                });
    }

    // Interface for connection events
    public interface MqttConnectionListener {
        void onConnected();
        void onConnectionFailed(Throwable throwable);
    }

    // Interface for receiving messages
    public interface MqttMessageListener {
        void onMessageReceived(String topic, String message);
    }

}
